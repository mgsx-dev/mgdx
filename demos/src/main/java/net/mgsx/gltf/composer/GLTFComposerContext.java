package net.mgsx.gltf.composer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.graphics.cameras.BlenderCamera;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltf.composer.core.Composition;
import net.mgsx.gltf.composer.utils.ComposerUtils;
import net.mgsx.gltf.composer.utils.Overlay;
import net.mgsx.gltf.composer.utils.PBRRenderTargetsMultisample;
import net.mgsx.gltf.ibl.io.AWTFileSelector;
import net.mgsx.gltf.ibl.io.FileSelector;
import net.mgsx.gltf.scene.Skybox;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.ibl.IBL;

public class GLTFComposerContext {
	
	// stored settings
	public Composition compo = new Composition();
	
	// runtime objects
	public final FileSelector fileSelector = new AWTFileSelector();
	
	public Skin skin;
	public Stage stage;
	/** to be used for post processing */
	public SpriteBatch batch;
	/** used for overlays */
	public ShapeRenderer shapes;

	public PBRShaderConfig colorShaderConfig;
	public DepthShader.Config depthShaderConfig;
	public SceneManager sceneManager;
	public Skybox skyBox;
	public IBL ibl;
	
	public DirectionalLightEx keyLight = new DirectionalLightEx();
	
	public PBRRenderTargetsMultisample fbo;
	public FrameBuffer ldrFbo;

	public SceneAsset asset;
	public Scene scene;
	public final BoundingBox sceneBounds = new BoundingBox();
	
	public BlenderCamera cameraManager;

	// Editor live options
	public boolean showSelectedNodeOnly = false;
	public Node cameraAttachment;
	public int msaa = 0;
	public int pixelZoom = 1;
	public final Overlay overlay = new Overlay();
	
	// Profiling
	public GLProfiler profiler;
	public boolean vsync, fsync;
	public int ffps;

	// State changes
	public boolean sceneJustChanged = false;
	public boolean compositionJustChanged = false;;

	private boolean shadersValid = false;
	private boolean fboValid = false;

	// Cache
	private final ObjectMap<String, Texture> textureCache = new ObjectMap<String, Texture>();

	public GLTFComposerContext() {
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		shapes = new ShapeRenderer();
	}
	
	public void invalidateShaders(){
		shadersValid = false;
	}
	public void invalidateFBO(){
		fboValid = false;
	}
	
	public void validate(){
		if(!fboValid){
			invalidateShaders();
			fboValid = true;
			fbo.reset();
			ldrFbo = FrameBufferUtils.ensureScreenSize(ldrFbo, GLFormat.RGB8, true);
		}
		if(!shadersValid){
			shadersValid = true;

			colorShaderConfig.numDirectionalLights = 1;
			colorShaderConfig.numPointLights = 0;
			colorShaderConfig.numSpotLights = 0;
			
			if(asset != null){
				colorShaderConfig.numBones = asset.maxBones;
				depthShaderConfig.numBones = asset.maxBones;
			}else{
				colorShaderConfig.numBones = 0;
				depthShaderConfig.numBones = 0;
			}
			
			depthShaderConfig.defaultCullFace = 0; //GL20.GL_BACK;
			
			if(colorShaderConfig.vertexShader == null){
				colorShaderConfig.vertexShader = Gdx.files.classpath("net/mgsx/gltf/shaders/gdx-pbr.vs.glsl").readString();
			}
			if(colorShaderConfig.fragmentShader == null){
				colorShaderConfig.glslVersion = null;
//				colorShaderConfig.fragmentShader = Gdx.files.classpath("net/mgsx/gltf/shaders/gdx-pbr.fs.glsl").readString();
				colorShaderConfig.fragmentShader = Gdx.files.classpath("shaders/gdx-pbr-patch-hdr.fs.glsl").readString();
			}
			
			sceneManager.setShaderProvider(new PBRShaderProvider(colorShaderConfig));
			sceneManager.setDepthShaderProvider(new PBRDepthShaderProvider(depthShaderConfig));
			
			// TODO remove all lights added by a scene (workaround)
			sceneManager.environment.remove(DirectionalLightsAttribute.Type);
			sceneManager.environment.remove(PointLightsAttribute.Type);
			sceneManager.environment.remove(SpotLightsAttribute.Type);
			sceneManager.environment.add(keyLight);
			
			if(skyBox != null){
				sceneManager.setSkyBox(null);
				skyBox.dispose();
			}
			if(ibl != null && ibl.environmentCubemap != null && compo.skyBoxEnabled){
				createSkybox();
				sceneManager.setSkyBox(skyBox);
			}
		}
	}
	
	public void createSkybox(){
		skyBox = new Skybox(ibl.getEnvironmentCubemap(), colorShaderConfig.manualSRGB, colorShaderConfig.manualGammaCorrection);
		
		// enable blending and diffuse factor to enable opacity and ambient factor
		skyBox.environment.set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));
		skyBox.environment.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
		
		// apply same ambient factor as scene manager.
		ComposerUtils.syncSkyboxAmbientFactor(this);
	}
	public void setComposition(Composition c) {
		compo = c;
		
		
		// apply camera
		cameraManager.switchTo(true);
		cameraManager.setTarget(c.camera.target);
		c.camera.configure(cameraManager.getPerspectiveCamera());
		
		// apply light
		c.keyLight.configure(keyLight);
		
		ComposerUtils.setSkyboxOpacity(this, compo.skyBoxColor.a);
		ComposerUtils.setAmbientFactor(this, compo.ambiantStrength);
		ComposerUtils.setEmissiveIntensity(this, compo.emissiveIntensity);
		ComposerUtils.enableFog(this, compo.fogEnabled);
		
		cameraAttachment = null;
		
		//
		ibl = c.ibl;
		if(ibl != null){
			applyIBL();
		}
		
		if(c.sceneAssets.size > 0){
			setScene(c.sceneAssets.first());
		}else{
			setScene(null);
		}
		sceneJustChanged = true;
		
		compositionJustChanged = true;
		
		invalidateFBO();
		invalidateShaders();
	}
	public void setScene(SceneAsset newAsset) {
		if(scene != null) sceneManager.removeScene(scene);
		if(asset != null){
			asset.dispose();
		}
		asset = newAsset;
		if(asset != null){
			scene = new Scene(asset.scene);
			sceneManager.addScene(scene);
			scene.modelInstance.calculateBoundingBox(sceneBounds);
		}
	}
	public void applyIBL() {
		if(ibl.environmentCubemap != null){
			ibl.environmentCubemap.bind();
			Gdx.gl.glGenerateMipmap(GL20.GL_TEXTURE_CUBE_MAP);
			ibl.environmentCubemap.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
		}
		
		ibl.apply(sceneManager);
		if(ibl.environmentCubemap != null){
			if(skyBox == null){
				createSkybox();
				sceneManager.setSkyBox(skyBox);
			}else{
				skyBox.set(ibl.getEnvironmentCubemap());
			}
		}else{
			if(skyBox != null){
				sceneManager.setSkyBox(null);
				skyBox.dispose();
			}
		}
		invalidateShaders();
	}

	public Texture textureCache(String path) {
		Texture texture = textureCache.get(path);
		if(texture == null) textureCache.put(path, texture = new Texture(path));
		return texture;
	}

}
