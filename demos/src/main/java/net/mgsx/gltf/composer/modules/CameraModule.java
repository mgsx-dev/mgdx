package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.ComposerUtils;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.composer.utils.UI.ControlScale;

public class CameraModule implements GLTFComposerModule {

	private Table controls;

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = new Table(skin);
		buildUI(ctx);
		return controls;
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		if(ctx.sceneJustChanged){
			buildUI(ctx);
		}
	}
	
	public void buildUI(GLTFComposerContext ctx){
		controls.clear();
		UI.slider(controls, "FOV", 0, 180, ctx.cameraManager.getPerspectiveCamera().fieldOfView, v->{
			ctx.cameraManager.getPerspectiveCamera().fieldOfView=v;
			ctx.cameraManager.getPerspectiveCamera().update();
		});
		UI.slider(controls, "near", 1e-3f, 1e3f, ctx.cameraManager.getCamera().near, ControlScale.LOG, v->{
			ctx.cameraManager.getPerspectiveCamera().near = v;
			ctx.cameraManager.getOrthographicCamera().near = v;
			ctx.cameraManager.getCamera().update();
		});
		UI.slider(controls, "far", 1e-3f, 1e5f, ctx.cameraManager.getCamera().far, ControlScale.LOG, v->{
			ctx.cameraManager.getPerspectiveCamera().far = v;
			ctx.cameraManager.getOrthographicCamera().far = v;
			ctx.cameraManager.getCamera().update();
		});
		controls.add(UI.trig(ctx.skin, "Fit to scene", ()->{
			ComposerUtils.fitCameraToScene(ctx);
			buildUI(ctx);
		}));
	}
}
