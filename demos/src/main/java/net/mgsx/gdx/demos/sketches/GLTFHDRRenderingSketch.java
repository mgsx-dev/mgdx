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
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader.Config;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.gltf.utils.IBL;
import net.mgsx.gdx.graphics.cameras.BlenderCamera;
import net.mgsx.gdx.utils.ShaderProgramUtils;
import net.mgsx.gfx.BlurCascade;
import net.mgsx.gfx.GLFormat;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

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
	private ShaderProgram toneMapping;
	private Scene scene;
	private BlurCascade blur;
	private ShaderProgram bloomExtract;
	private Vector3 threshold = new Vector3();
	
	public GLTFHDRRenderingSketch() {
		
		PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
		config.manualGammaCorrection = false;
		config.manualSRGB = SRGB.ACCURATE;
		config.numBones = 0;
		config.numDirectionalLights = 1;
		config.numPointLights = 0;
		config.numSpotLights = 0;
		
		Config depthConfig = PBRDepthShaderProvider.createDefaultConfig();
		depthConfig.numBones = 0;
		
		sceneManager = new SceneManager(new PBRShaderProvider(config), new PBRDepthShaderProvider(depthConfig));
		
		batch = new SpriteBatch();
		
		cameraManager = new BlenderCamera(Vector3.Zero, .05f, Input.Buttons.LEFT); // XXX BoomBox
		
		ibl = new IBL();
		ibl.load("textures/demo2", "png", false);
		ibl.apply(sceneManager);
		
		// TODO generate IBL as HDR (float maps) or load KTX2 files
		ibl.loadHDRI(Gdx.files.classpath("textures/demo2/table_mountain_2_4k.hdr"));
		
		skyBox = new SceneSkybox(ibl.getEnvironmentCubemap(), SRGB.FAST, false);
		
		sceneManager.setSkyBox(skyBox);
		
		sunLight = new DirectionalLightEx().set(Color.WHITE, new Vector3(1,-2,-1), 3);
		
		sceneManager.environment.add(sunLight);
		
		toneMapping = new ShaderProgram(
			Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(), 
			"#define EXPOSURE\n" +
			"#define GAMMA_CORRECTION 2.2\n" +
			Gdx.files.classpath("shaders/tone-mapping.fs.glsl").readString());
		ShaderProgramUtils.check(toneMapping);
		
		GLFormat format = new GLFormat();
		format.format = GL30.GL_RGBA;
		format.internalFormat = GL30.GL_RGBA32F;
		format.type = GL30.GL_FLOAT;
		
		blur = new BlurCascade(format, 32);
		
		bloomExtract = new ShaderProgram(
			Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(), 
			"#define EXPOSURE\n" +
			Gdx.files.classpath("shaders/bloom-extract.fs.glsl").readString());
		ShaderProgramUtils.check(bloomExtract);
		
		// asset = new GLTFLoader().load(Gdx.files.internal("models/cubes.gltf"));
		// asset = new GLTFLoader().load(Gdx.files.internal("models/BoomBox/glTF/BoomBox.gltf"));
		loadGLTFModel(Gdx.files.internal("models/BoomBox/glTF/BoomBox.gltf"));
	}
	
	private void loadFile(FileHandle file) {
		if(file.extension().toLowerCase().equals("gltf")){
			loadGLTFModel(file);
		}
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
		
		// XXX Boost emissive factor
		if(scene != null){
			for(Material m : scene.modelInstance.materials){
				if(m.has(PBRTextureAttribute.EmissiveTexture)){
					
					ColorAttribute em = m.get(ColorAttribute.class, PBRColorAttribute.Emissive);
					if(em != null){
						em.color.r = em.color.g = em.color.b = 10;
					}
				}
				
			}
		}
		
		float ix = Gdx.input.getX() / (float)Gdx.graphics.getWidth();
		float iy = Gdx.input.getY() / (float)Gdx.graphics.getHeight();
		
		cameraManager.update(delta);
		sceneManager.camera = cameraManager.getCamera();

		sunLight.baseColor.fromHsv(15, .5f, 1);
		sunLight.intensity = 50;
		sunLight.direction.set(1,-.2f,1);
		
		sceneManager.setAmbientLight(1f);
		
		sceneManager.update(delta);
		
		sceneManager.renderShadows();
		
		ensureFBO(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		
		fbo.begin();
		ScreenUtils.clear(Color.CLEAR, true);
		sceneManager.renderColors();
		fbo.end();
		
		toneMapping.bind();
		int u_exposure = toneMapping.getUniformLocation("u_exposure");
		if(u_exposure >= 0){
			toneMapping.setUniformf("u_exposure", ix * ix);
		}
		
		bloomExtract.bind();
		bloomExtract.setUniformf("u_threshold", threshold.set(0.2126f, 0.7152f, 0.0722f).scl(1f));
		// bloomExtract.setUniformf("u_falloff", 2f);
		
		// TODO use light energy (rgb != 1)
		Texture bloom = blur.render(fbo.getColorBufferTexture(), 100, bloomExtract);
		fbo.begin();
		batch.setColor(1,1,1,1);
		batch.setShader(null);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.enableBlending();
		Gdx.gl.glBlendColor(1, 1, 1, .1f); // TODO use that for cascade !
		batch.setBlendFunction(GL20.GL_CONSTANT_ALPHA, GL20.GL_ONE);
		batch.begin();
		batch.draw(bloom, 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
		fbo.end();
		
		batch.setColor(Color.WHITE);
		batch.setShader(toneMapping);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
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
