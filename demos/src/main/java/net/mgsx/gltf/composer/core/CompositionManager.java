package net.mgsx.gltf.composer.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader.Config;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gltf.composer.core.Composition.CameraConfig;
import net.mgsx.gltf.composer.core.Composition.ToneMappingOptions.ToneMappingMode;
import net.mgsx.gltf.scene.Skybox;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.ShaderParser;
import net.mgsx.gltfx.GLFormat;
import net.mgsx.gltfx.gfx.Bloom;
import net.mgsx.gltfx.gfx.ToneMappingShader;
import net.mgsx.gltfx.mrt.PBRRenderTargets;

public class CompositionManager {
	public SceneManager sceneManager;
	public final PBRRenderTargets fbo;
	private SpriteBatch batch;
	private Composition compo;
	private Bloom bloom = new Bloom();
	private ToneMappingShader toneMapping;
	private final ToneMappingShader.Reinhard reinhardMode = new ToneMappingShader.Reinhard(true);
	private final ToneMappingShader.Exposure exposureMode = new ToneMappingShader.Exposure(true);
	private final ToneMappingShader.GammaCompression gammaMode = new ToneMappingShader.GammaCompression(true);
	private final DirectionalLightEx keyLight = new DirectionalLightEx();
	public final PerspectiveCamera camera = new PerspectiveCamera();
	private Skybox skyBox;

	/**
	 * Create an empty composition manager. You need to load a composition
	 */
	public CompositionManager() {
		fbo = new PBRRenderTargets();
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
	}
	
	public void setComposition(Composition compo) {
		this.compo = compo;
		
		fbo.clear();
		if(compo.hdr){
			fbo.addLayer(PBRRenderTargets.COLORS, GLFormat.RGBA16);
			fbo.setDepth(GLFormat.DEPTH24);
		}else{
			fbo.addLayer(PBRRenderTargets.COLORS, GLFormat.RGBA8);
			fbo.setDepth(GLFormat.DEPTH24);
		}
		
		PBRShaderConfig colorConfig = PBRShaderProvider.createDefaultConfig();
		Config depthConfig = PBRDepthShaderProvider.createDefaultConfig();
		
		boolean mrtRequired = false; // TODO when using SSAO / SSR... later
		
		if(mrtRequired){
			fbo.configure(colorConfig);
		}else if(compo.hdr){
			colorConfig.fragmentShader = ShaderParser.parse(Gdx.files.classpath("shaders/pbr/pbr.fs.glsl"));
		}else{
//			colorShaderConfig.fragmentShader = Gdx.files.classpath("net/mgsx/gltf/shaders/gdx-pbr.fs.glsl").readString();
		}
		
		colorConfig.numDirectionalLights = 1;
		colorConfig.numPointLights = 0;
		colorConfig.numSpotLights = 0;

		int maxBones = 0;
		for(SceneAsset asset : compo.sceneAssets){
			maxBones = Math.max(maxBones, asset.maxBones);
		}
		
		colorConfig.numBones = maxBones;
		depthConfig.numBones = maxBones;
		if(compo.hdr){
			colorConfig.manualGammaCorrection = false;
			colorConfig.manualSRGB = SRGB.FAST;
		}else{
			colorConfig.manualGammaCorrection = true;
			colorConfig.manualSRGB = SRGB.FAST;
		}
		
		sceneManager = new SceneManager(new PBRShaderProvider(colorConfig), new PBRDepthShaderProvider(depthConfig));
		
		sceneManager.camera = camera;
		
		for(SceneAsset asset : compo.sceneAssets){
			sceneManager.addScene(new Scene(asset.scene));
		}
		
		if(compo.ibl != null){
			compo.ibl.apply(sceneManager);
			if(compo.skyBoxEnabled){
				skyBox = new Skybox(compo.ibl.environmentCubemap, colorConfig.manualSRGB, colorConfig.manualGammaCorrection);
				sceneManager.setSkyBox(skyBox);
			}else{
				sceneManager.setSkyBox(null);
			}
		}
		
		compo.keyLight.configure(keyLight);
		sceneManager.environment.add(keyLight);
		
		if(compo.toneMapping != null){
			if(compo.toneMapping.mode == ToneMappingMode.REINHARD){
				toneMapping = reinhardMode;
			}
			else if(compo.toneMapping.mode == ToneMappingMode.EXPOSURE){
				toneMapping = exposureMode;
				toneMapping.bind();
				exposureMode.setExposure(compo.toneMapping.exposure);
			}
			else if(compo.toneMapping.mode == ToneMappingMode.GAMMA_COMPRESSION){
				toneMapping = gammaMode;
				toneMapping.bind();
				gammaMode.setLuminosity(compo.toneMapping.luminosity);
				gammaMode.setContrast(compo.toneMapping.contrast);
			}
		}
		
		compo.camera.configure(camera);
	}
	
	public void update(float delta) {
		sceneManager.update(delta);
	}
	
	public void render(){
		sceneManager.updateViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		sceneManager.renderShadows();
		
		fbo.ensureScreenSize();
		fbo.begin();
		ScreenUtils.clear(compo.clearColor, true);
		sceneManager.renderColors();
		fbo.end();
		
		bloom.apply(fbo.getFrameBuffer(), batch, compo.bloom);
		
		if(toneMapping != null){
			batch.setShader(toneMapping);
		}
		
		Texture texture = fbo.getColorBufferTexture();
		
		batch.begin();
		batch.draw(texture, 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
		
		batch.setShader(null);
	}

	public void setView(CameraConfig config) {
		config.configure(camera);
	}

}
