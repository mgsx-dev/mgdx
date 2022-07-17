package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.core.Composition.CameraConfig;
import net.mgsx.gltf.composer.utils.ComposerUtils;

public class CameraModule implements GLTFComposerModule {

	private Table controls;
	private String lastConfig;
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = UI.table(skin);
		buildUI(ctx);
		return controls;
	}
	
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		if(ctx.sceneJustChanged){
			buildUI(ctx);
		}
		if(ctx.cameraAttachment != null){
			Camera camera = ctx.cameraManager.getCamera();
			camera.position.setZero().mul(ctx.cameraAttachment.globalTransform);
			camera.up.set(0,1,0).mul(ctx.cameraAttachment.globalTransform).sub(camera.position).nor();
			camera.direction.set(0,0,-1).mul(ctx.cameraAttachment.globalTransform).sub(camera.position).nor();
			camera.update();
		}
	}
	
	public void buildUI(GLTFComposerContext ctx){
		controls.clear();
		{
			Frame frame = UI.frame("camera", ctx.skin);
			controls.add(frame).row();
			Table t = frame.getContentTable();
			
			UI.slider(t, "FOV", 0, 180, ctx.cameraManager.getPerspectiveCamera().fieldOfView, v->{
				ctx.cameraManager.getPerspectiveCamera().fieldOfView=v;
				ctx.cameraManager.getPerspectiveCamera().update();
			});
			UI.slider(t, "near", 1e-3f, 1e3f, ctx.cameraManager.getCamera().near, ControlScale.LOG, v->{
				ctx.cameraManager.getPerspectiveCamera().near = v;
				ctx.cameraManager.getOrthographicCamera().near = v;
				ctx.cameraManager.getCamera().update();
			});
			UI.slider(t, "far", 1e-3f, 1e5f, ctx.cameraManager.getCamera().far, ControlScale.LOG, v->{
				ctx.cameraManager.getPerspectiveCamera().far = v;
				ctx.cameraManager.getOrthographicCamera().far = v;
				ctx.cameraManager.getCamera().update();
			});
			t.add(UI.trig(ctx.skin, "Fit to scene", ()->{
				ComposerUtils.fitCameraToScene(ctx);
				buildUI(ctx);
			})).row();
			
		}
		
		// Animated or not cameras
		boolean enableSceneCams = false; // XXX not necessary
		if(ctx.scene != null && enableSceneCams){
			Frame frame = UI.frame("scene cameras", ctx.skin);
			controls.add(frame).row();
			if(ctx.scene.cameras.size > 0){
				Table t = frame.getContentTable();
				Array<String> cams = new Array<String>();
				for(Entry<Node, Camera> entry : ctx.scene.cameras){
					cams.add(entry.key.id);
				}
				cams.sort();
				cams.insert(0, "");
				t.add(UI.selector(ctx.skin, cams, ctx.cameraAttachment != null ? ctx.cameraAttachment.id : cams.first(), item->item, item->{
					if(item.length() > 0) ComposerUtils.attachCamera(ctx, item);
					else ComposerUtils.attachCamera(ctx, null);
				})).row();
			}else{
				frame.add("no camera");
			}
		}

		// Select box de presets
		{
			Frame frame = UI.frame("saved views", ctx.skin);
			controls.add(frame).row();
			Table t = frame.getContentTable();
			t.add(UI.editor(ctx.skin, ctx.compo.views, lastConfig, ()->
			new CameraConfig().set(ctx.cameraManager.getPerspectiveCamera(), ctx.cameraManager.getPerspectiveTarget()), 
			name->{
				if(name != null){ 
					CameraConfig config = ctx.compo.views.get(name);
					config.configure(ctx.cameraManager.getPerspectiveCamera());
					ctx.cameraManager.setTarget(config.target);
					if(!name.equals(lastConfig)){
						lastConfig = name;
						buildUI(ctx);
					}
				}else{
					lastConfig = null;
				}
			})).row();
		}
	}
}
