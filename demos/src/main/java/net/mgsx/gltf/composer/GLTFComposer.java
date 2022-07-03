package net.mgsx.gltf.composer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.cameras.BlenderCamera;
import net.mgsx.gdx.scenes.scene2d.ui.TabPane;
import net.mgsx.gdx.scenes.scene2d.ui.TabPane.TabPaneStyle;
import net.mgsx.gltf.composer.modules.CameraModule;
import net.mgsx.gltf.composer.modules.FileModule;
import net.mgsx.gltf.composer.modules.HDRModule;
import net.mgsx.gltf.composer.modules.IBLModule;
import net.mgsx.gltf.composer.modules.ModelModule;
import net.mgsx.gltf.composer.modules.SceneModule;
import net.mgsx.gltf.composer.modules.SkinningModule;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

public class GLTFComposer extends ScreenAdapter {
	public final Array<GLTFComposerModule> modules = new Array<GLTFComposerModule>();
	
	public final GLTFComposerContext ctx = new GLTFComposerContext();
	
	private TabPane tabPane;

	public GLTFComposer() {
		Skin skin = ctx.skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
		
		// PATCH
		TextureRegion r = skin.getRegion("white");
		float u = (r.getU() + r.getU2())/2;
		float v = (r.getV() + r.getV2())/2;
		r.setRegion(u, v, u, v);
		
		skin.add("icon-cube",  UI.iconRegion("skins/icons.png", 0, 16 * 0, 16, 16), TextureRegion.class);
		skin.add("icon-shade", UI.iconRegion("skins/icons.png", 0, 16 * 1, 16, 16), TextureRegion.class);
		skin.add("icon-human", UI.iconRegion("skins/icons.png", 0, 16 * 2, 16, 16), TextureRegion.class);
		skin.add("icon-file",  UI.iconRegion("skins/icons.png", 0, 16 * 3, 16, 16), TextureRegion.class);
		skin.add("icon-light", UI.iconRegion("skins/icons.png", 0, 16 * 4, 16, 16), TextureRegion.class);
		skin.add("icon-orbit", UI.iconRegion("skins/icons.png", 0, 16 * 5, 16, 16), TextureRegion.class);
		skin.add("icon-camera", UI.iconRegion("skins/icons.png", 0, 16 * 6, 16, 16), TextureRegion.class);
		
		
		ctx.stage = new Stage(new ScreenViewport());
		ctx.cameraManager = new BlenderCamera(Vector3.Zero, 5f);
		
		ctx.colorShaderConfig = PBRShaderProvider.createDefaultConfig();
		ctx.depthShaderConfig = PBRDepthShaderProvider.createDefaultConfig();
		
		ctx.colorShaderConfig.manualGammaCorrection = false;
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		
		ctx.sceneManager = new SceneManager();
		
		ctx.keyLight.direction.set(1, -2, 1);
		ctx.keyLight.baseColor.fromHsv(0, 0f, 1f);
		ctx.keyLight.intensity = 3f;
		ctx.sceneManager.environment.add(ctx.keyLight);
		
		Table t = new Table(skin);
		Table c = new Table(skin);
		float lum = .1f;
		c.setBackground(skin.newDrawable("white", lum,lum,lum, .8f));
		c.setTouchable(Touchable.enabled);
		TabPaneStyle style = new TabPaneStyle();
		style.tabButtonStyle = skin.get("toggle", TextButtonStyle.class);
		tabPane = new TabPane(style);
		c.add(tabPane).row();
		c.add().expandY().row();
		t.add(c).growY().expandX().left();
		t.setFillParent(true);
		ctx.stage.addActor(t);
		
		// add modules
		addModule(new FileModule(), "icon-file");
		addModule(new ModelModule(), "icon-cube");
		addModule(new SceneModule(), "icon-file");
		addModule(new SkinningModule(), "icon-human");
		addModule(new IBLModule(), "icon-orbit");
		addModule(new HDRModule(), "icon-light");
		addModule(new CameraModule(), "icon-camera");
		
		tabPane.setCurrentIndex(0);
	}

	private void addModule(GLTFComposerModule module, String iconName){
		modules.add(module);
		Actor actor = module.initUI(ctx, ctx.skin);
		if(actor != null){
			tabPane.addPane(new ImageButton(ctx.skin.getDrawable(iconName)), actor);
		}
	}
	
	@Override
	public void show() {
		Mgdx.inputs.fileDropListener = files->fileDropped(files.first());
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
	
	public boolean fileDropped(FileHandle file) {
		for(int i=modules.size-1 ; i>=0 ; i--){
			if(modules.get(i).handleFile(ctx, file)){
				return true;
			}
		}
		// TODO display message (not supported)
		return false;
	}
	
	@Override
	public void render(float delta) {
		ctx.validate();
		
		ctx.cameraManager.update(delta);
		ctx.sceneManager.camera = ctx.cameraManager.getCamera();
		
		ctx.sceneManager.update(delta);
		
		for(int i=modules.size-1 ; i>=0 ; i--){
			modules.get(i).render(ctx);
		}
		
		ctx.stage.getViewport().apply();
		ctx.stage.act();
		ctx.stage.draw();
		
		ctx.sceneJustChanged = false;
	}

	
}
