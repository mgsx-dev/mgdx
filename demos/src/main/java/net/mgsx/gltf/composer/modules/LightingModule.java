package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.mgsx.gdx.scenes.scene2d.ui.ColorBox;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;

public class LightingModule implements GLTFComposerModule
{
	private Table controls;

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = UI.table(skin);
		updateUI(ctx, skin);
		return controls;
	}
	
	private void updateUI(GLTFComposerContext ctx, Skin skin){
		controls.clear();
		
		// TODO fill light and back light (rim light)
		
		controls.add(new ColorBox("Key light", ()->ctx.keyLight.baseColor, false, skin)).row();
		
		UI.slider(controls, "Key light", 0.01f, 100f, ctx.keyLight.intensity, ControlScale.LOG, value->ctx.keyLight.intensity=value);

		// key light orientation picker
		ClickListener listener = new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Ray ray = ctx.cameraManager.getCamera().getPickRay(Gdx.input.getX(), Gdx.input.getY());
				ctx.keyLight.direction.set(ray.direction).scl(-1);
				ctx.stage.removeCaptureListener(this);
			}
		};
				
		controls.add(UI.trig(skin, "Pick sun position from skybox", ()->{
			ctx.stage.addCaptureListener(listener);
		})).row();
		
	}
	
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		if(ctx.compositionJustChanged){
			updateUI(ctx, ctx.skin);
		}
		// update shadow light
		if(ctx.keyLight instanceof DirectionalShadowLight){
			DirectionalShadowLight shadowLight = (DirectionalShadowLight)ctx.keyLight;
			shadowLight.setBounds(ctx.sceneBounds);
		}
	}
}
