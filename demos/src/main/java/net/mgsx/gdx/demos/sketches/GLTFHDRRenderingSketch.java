package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader.Config;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.graphics.cameras.BlenderCamera;
import net.mgsx.gfx.BlurCascade;
import net.mgsx.gfx.BlurCascade.BlurMixMode;
import net.mgsx.gfx.BrighnessExtractShader;
import net.mgsx.gfx.ToneMappingShader;
import net.mgsx.gltf.GLTFMaterialUtils;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.ibl.IBL;

public class GLTFHDRRenderingSketch extends ScreenAdapter
{
	private SceneAsset asset;
	private SceneManager sceneManager;
	private FrameBuffer fbo;
	private SpriteBatch batch;
	private BlenderCamera cameraManager;
	private IBL ibl;
	private SceneSkybox skyBox;
	private DirectionalLightEx sunLight;
	private ToneMappingShader.Exposure toneMapping;
	private Scene scene;
	private BlurCascade blur;
	private BrighnessExtractShader bloomExtract;
	boolean useBloom = true;
	
	public GLTFHDRRenderingSketch() {
		
		// scene
		cameraManager = new BlenderCamera(Vector3.Zero, 5f, Input.Buttons.LEFT);
		sunLight = new DirectionalLightEx().set(Color.WHITE, new Vector3(1,-2,-1), 3);
		ibl = IBL.fromHDR(Gdx.files.internal("textures/demo2/table_mountain_2_4k.hdr"), true);
		skyBox = new SceneSkybox(ibl.getEnvironmentCubemap(), SRGB.FAST, false);
		createSceneManager(0);
		
		// post processing
		toneMapping = new ToneMappingShader.Exposure(true);
		blur = new BlurCascade(GLFormat.RGB32, 32);
		bloomExtract = new BrighnessExtractShader();
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);

		// load defaults
		loadGLTFModel(Gdx.files.internal("models/cubes.gltf"));
	}
	
	private void createSceneManager(int numBones){
		PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
		config.manualGammaCorrection = false;
		config.manualSRGB = SRGB.ACCURATE;
		config.numBones = numBones;
		config.numDirectionalLights = 1;
		config.numPointLights = 0;
		config.numSpotLights = 0;
		
		Config depthConfig = PBRDepthShaderProvider.createDefaultConfig();
		depthConfig.numBones = numBones;
		
		if(sceneManager != null) sceneManager.dispose();
		
		sceneManager = new SceneManager(new PBRShaderProvider(config), new PBRDepthShaderProvider(depthConfig));
		sceneManager.environment.add(sunLight);
		sceneManager.setSkyBox(skyBox);
		ibl.apply(sceneManager);
	}
	
	private void loadFile(FileHandle file) {
		if(file.extension().toLowerCase().equals("gltf")){
			loadGLTFModel(file);
		}else if(file.extension().toLowerCase().equals("hdr")){
			loadHdrFile(file);
		}
	}
	
	private void loadHdrFile(FileHandle file) {
		if(ibl != null){
			ibl.dispose();
		}
		ibl = IBL.fromHDR(file, false);
		ibl.apply(sceneManager);
		skyBox.set(ibl.getEnvironmentCubemap());
	}

	private void loadGLTFModel(FileHandle file) {
		if(asset != null){
			asset.dispose();
		}
		if(scene != null){
			sceneManager.removeScene(scene);
		}
		asset = new GLTFLoader().load(file);
		scene = new Scene(asset.scene);
		sceneManager.addScene(scene);
	}

	@Override
	public void show() {
		Gdx.graphics.setTitle("Drop glTF and HDRI files(*.gltf, *.hdr)");
		Gdx.input.setInputProcessor(new InputMultiplexer(Gdx.input.getInputProcessor(), cameraManager.getInputs()));
		Mgdx.inputs.fileDropListener = files->loadFile(files.first());
	}
	
	@Override
	public void hide() {
		Gdx.input.setInputProcessor(((InputMultiplexer)Gdx.input.getInputProcessor()).getProcessors().first());
		Mgdx.inputs.fileDropListener = null;
	}
	
	@Override
	public void resize(int width, int height) {
		cameraManager.resize(width, height);
	}
	
	@Override
	public void render(float delta) {
		
		// inputs for debug control
		float ix = Gdx.input.getX() / (float)Gdx.graphics.getWidth();
		float iy = Gdx.input.getY() / (float)Gdx.graphics.getHeight();

		float bloomRate = iy;
		float exposure = ix*ix*2;
		float blurMix = .3f;
		
		// Boost emissive factor and force metalic roughness
		if(scene != null){
			GLTFMaterialUtils.forceEmissiveTextureFactor(scene, 510);
			GLTFMaterialUtils.forceMetallicRoughness(scene, 1, 0);
		}
		
		// update camera
		cameraManager.update(delta);

		// update lighting
		sunLight.baseColor.fromHsv(15, .5f, 1);
		sunLight.intensity = 3;
		sunLight.direction.set(1,-.20f,1.18f); // calibrated manually for table_mountain_2_4k.hdr
		sceneManager.setAmbientLight(1f);
		
		// update scene
		sceneManager.camera = cameraManager.getCamera();
		sceneManager.update(delta);
		
		// render scene into HDR frame buffer
		sceneManager.renderShadows();
		ensureFBO(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		fbo.begin();
		ScreenUtils.clear(Color.CLEAR, true);
		sceneManager.renderColors();
		fbo.end();
		
		// render bloom
		if(useBloom){
			bloomExtract.bind();
			bloomExtract.setThresholdRealistic(1f);
			blur.setMixFunc(BlurMixMode.ADD, blurMix);
			Texture blooTexturem = blur.render(fbo.getColorBufferTexture(), Integer.MAX_VALUE, bloomExtract);
			
			fbo.begin();
			batch.enableBlending();
			Gdx.gl.glBlendColor(1, 1, 1, bloomRate);
			batch.setBlendFunction(GL20.GL_CONSTANT_ALPHA, GL20.GL_ONE);
			batch.begin();
			batch.draw(blooTexturem, 0, 0, 1, 1, 0, 0, 1, 1);
			batch.end();
			fbo.end();
		}
		
		// render final with tone mapping (HDR to LDR)
		toneMapping.bind();
		toneMapping.setExposure(exposure);
		batch.setShader(toneMapping);
		batch.disableBlending();
		batch.begin();
		batch.draw(fbo.getColorBufferTexture(), 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
	}

	private void ensureFBO(int width, int height) {
		if(fbo == null || fbo.getWidth() != width || fbo.getHeight() != height){
			if(fbo != null) fbo.dispose();
			FrameBufferBuilder builder = new FrameBufferBuilder(width, height);
			
			builder.addColorTextureAttachment(GL30.GL_RGBA32F, GL30.GL_RGBA, GL30.GL_FLOAT);
			builder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24);
			
			fbo = builder.build();
		}
	}
}
