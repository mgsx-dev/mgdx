package net.mgsx.gltf.composer;

import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.MgdxGame.Settings;
import net.mgsx.gdx.graphics.cameras.BlenderCamera;
import net.mgsx.gdx.scenes.scene2d.ui.Frame.FrameStyle;
import net.mgsx.gdx.scenes.scene2d.ui.TabPane;
import net.mgsx.gdx.scenes.scene2d.ui.TabPane.TabPaneStyle;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.modules.CameraModule;
import net.mgsx.gltf.composer.modules.CompositionModule;
import net.mgsx.gltf.composer.modules.IBLModule;
import net.mgsx.gltf.composer.modules.LightingModule;
import net.mgsx.gltf.composer.modules.MiscModule;
import net.mgsx.gltf.composer.modules.PostProcessingModule;
import net.mgsx.gltf.composer.modules.RenderModule;
import net.mgsx.gltf.composer.modules.SceneModule;
import net.mgsx.gltf.composer.modules.SystemModule;
import net.mgsx.gltf.composer.utils.ComposerRayCast;
import net.mgsx.gltf.composer.utils.ComposerUtils;
import net.mgsx.gltf.composer.utils.PBRRenderTargetsMultisample;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltfx.GLFormat;

public class GLTFComposer extends ScreenAdapter {
	
	public static final Array<Supplier<GLTFComposerModule>> addonsFactory = new Array<Supplier<GLTFComposerModule>>();
	
	public final Array<GLTFComposerModule> modules = new Array<GLTFComposerModule>();
	
	public final GLTFComposerContext ctx = new GLTFComposerContext();
	
	private TabPane tabPane;
	
	private boolean tabOn = true;

	private Table root;

	private Table content;

	private final SystemModule systemModule;
	
	private final IntIntMap moduleToTabIndex = new IntIntMap();
	
	public GLTFComposer(Settings settings, boolean hdpiDetected) {
		
		ctx.vsync = settings.useVSync;
		ctx.fsync = settings.fps > 0;
		ctx.ffps = settings.fps;
		
		Skin skin = ctx.skin = new Skin(Gdx.files.internal("skins/composer-skin.json"));
		
		FrameStyle frameStyle = new FrameStyle();
		frameStyle.headerBackgroundLeft = skin.getDrawable("frame-top-left");
		frameStyle.headerBackgroundRight = skin.getDrawable("frame-top-right");
		frameStyle.bodyBackground = skin.getDrawable("frame-bottom");
		skin.add("default", frameStyle, FrameStyle.class);
		
		// PATCH
		TextureRegion r = skin.getRegion("white");
		float u = (r.getU() + r.getU2())/2;
		float v = (r.getV() + r.getV2())/2;
		r.setRegion(u, v, u, v);
		
		String[] icons = new String[]{"icon-cube",  "icon-shade", "icon-human", "icon-grad", 
				"icon-tree", "icon-lab", "icon-shader",
				"icon-file",  "icon-light", "icon-orbit", "icon-camera", "icon-wrench"};
		for(String icon : icons){
			skin.add(icon, skin.getRegion(icon + "-16"), TextureRegion.class);
		}
		
		ctx.profiler = new GLProfiler(Gdx.graphics);
		ctx.profiler.setListener(GLErrorListener.LOGGING_LISTENER);

		ScreenViewport viewport = new ScreenViewport();
		if(hdpiDetected) viewport.setUnitsPerPixel(0.5f);
		ctx.stage = new Stage(viewport);
		ctx.cameraManager = new BlenderCamera(Vector3.Zero, 5f, Buttons.LEFT);
		ctx.cameraManager.rayCastHandler = new ComposerRayCast(ctx);
		
		ctx.colorShaderConfig = PBRShaderProvider.createDefaultConfig();
		ctx.depthShaderConfig = PBRDepthShaderProvider.createDefaultConfig();
		
		ctx.colorShaderConfig.manualGammaCorrection = false;
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		
		// basic
		ctx.fbo = new PBRRenderTargetsMultisample(ctx.msaa);
		ctx.fbo.addColors();
		ctx.fbo.setDepth(GLFormat.DEPTH24);
		ctx.invalidateFBO();
		
		ctx.keyLight.direction.set(1, -2, 1);
		ctx.keyLight.baseColor.fromHsv(0, 0f, 1f);
		ctx.keyLight.intensity = 3f;
		ctx.sceneManager.environment.add(ctx.keyLight);
		
		ComposerUtils.updateShadowBias(ctx, ctx.compo.shadowBias);
		
		Table t = root = new Table(skin);
		Table c = content = new Table(skin);
		c.setTouchable(Touchable.enabled);
		TabPaneStyle style = new TabPaneStyle();
		style.tabButtonStyle = skin.get("tab", TextButtonStyle.class);
		style.panesBackground = skin.getDrawable("fade-tabpane-back");
		tabPane = new TabPane(style);
		c.add(tabPane).grow().row();
		t.add(c).growY().expandX().left();
		t.setFillParent(true);
		ctx.stage.addActor(t);
		
		// add modules
		addModule(new CompositionModule(), "icon-file");
		addModule(new SceneModule(), "icon-tree");
		addModule(new CameraModule(), "icon-camera");
		addModule(new IBLModule(), "icon-cube");
		addModule(new LightingModule(), "icon-light");
		addModule(new RenderModule(ctx), "icon-shader");
		addModule(new PostProcessingModule(), "icon-grad");
		addModule(new MiscModule(ctx), "icon-lab");
		addModule(systemModule = new SystemModule(), "icon-wrench");
		for(Supplier<GLTFComposerModule> f : addonsFactory){
			addModule(f.get(), "icon-lab");
		}
		
		tabPane.setCurrentIndex(0);
	}

	private void addModule(GLTFComposerModule module, String iconName){
		int index = modules.size;
		modules.add(module);
		Actor actor = module.initUI(ctx, ctx.skin);
		if(actor != null){
			moduleToTabIndex.put(index, moduleToTabIndex.size);
			Button bt = new Button(ctx.skin, "tab");
			bt.add(new Image(ctx.skin, iconName)).pad(10);
			tabPane.addPane(bt, actor);
		}
	}
	
	@Override
	public void show() {
		Mgdx.inputs.fileDropListener = files->filesDropped(files);
		Gdx.input.setInputProcessor(new InputMultiplexer(ctx.stage, ctx.cameraManager.getInputs()));
	}
	
	@Override
	public void hide() {
		Mgdx.inputs.fileDropListener = null;
		Gdx.input.setInputProcessor(null);
	}
	
	@Override
	public void resize(int width, int height) {
		ctx.cameraManager.resize(width, height);
		ctx.stage.getViewport().update(width, height, true);
	}
	
	public void filesDropped(Array<FileHandle> files) {
		if(files.size > 1){
			UI.popup(ctx.stage, ctx.skin, "Error", "multiple files not supported");
		}else{
			FileHandle file = files.first();
			for(int i=modules.size-1 ; i>=0 ; i--){
				if(modules.get(i).handleFile(ctx, file)){
					int tabIndex = moduleToTabIndex.get(i, -1);
					if(tabIndex >= 0){
						tabPane.setCurrentIndex(tabIndex);
					}
					return;
				}
			}
			UI.popup(ctx.stage, ctx.skin, "Error", "file extension not supported");
		}
	}
	
	private void toggleTab(){
		tabOn = !tabOn;
		if(tabOn){
			root.setVisible(true);
			root.setFillParent(false);
			root.setBounds(root.getX(), 0, ctx.stage.getWidth(), ctx.stage.getHeight());
			root.clearActions();
			root.addAction(Actions.sequence(
				Actions.moveTo(0, 0, .3f, Interpolation.pow2Out),
				Actions.run(()->root.setFillParent(true))));
		}else{
			float w = content.getWidth();
			root.clearActions();
			root.setFillParent(false);
			root.addAction(Actions.sequence(
				Actions.moveTo(-w, 0, .3f, Interpolation.pow2In),
				Actions.run(()->root.setVisible(false))));
		}
	}
	
	@Override
	public void render(float delta) {
		// TODO which one (N is Blender like)
		if(Gdx.input.isKeyJustPressed(Input.Keys.TAB) || Gdx.input.isKeyJustPressed(Input.Keys.N)){
			toggleTab();
		}
		
		ctx.validate();
		
		systemModule.beginProfiling(ctx);
		
		ctx.cameraManager.update(delta);
		ctx.sceneManager.camera = ctx.cameraManager.getCamera();
		
		ctx.sceneManager.update(delta);
		
		for(int i=0 ; i<modules.size ; i++){
			modules.get(i).update(ctx, delta);
		}
		
		for(int i=0 ; i<modules.size ; i++){
			modules.get(i).render(ctx);
		}
		
		systemModule.endProfiling(ctx);
		
		ctx.stage.getViewport().apply();
		ctx.stage.act();
		ctx.stage.draw();
		
		ctx.sceneJustChanged = false;
		ctx.compositionJustChanged = false;
	}

	
}
