package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.mgsx.gdx.scenes.scene2d.ui.ColorBox;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.ComposerCode;
import net.mgsx.gltf.composer.utils.ComposerUtils;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;

public class LightingModule implements GLTFComposerModule
{
	private Table controls;
	
	private ShadowModule shadows = new ShadowModule();

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = UI.table(skin);
		updateUI(ctx, skin);
		return controls;
	}
	
	private void updateUI(GLTFComposerContext ctx, Skin skin){
		controls.clear();
		controls.defaults().fill();
		
		UI.header(controls, "Lighting");
		
		Frame wdFrame = UI.frame("Ambient", skin);
		Table wdTable = wdFrame.getContentTable();
		UI.slider(wdTable, "strength", 0, 3, ctx.compo.ambiantStrength, value->ComposerUtils.setAmbientFactor(ctx, value));
		controls.add(wdFrame).growX().row();

		// TODO fill light and back light (rim light)
		
		Frame klFrame = UI.frame("Key light", skin);
		Table klTable = klFrame.getContentTable();
		controls.add(klFrame).row();
		
		klTable.add("color");
		klTable.add(new ColorBox("key light", ()->ctx.keyLight.baseColor, false, skin)).expandX().left().row();
		
		UI.sliderTable(klTable, "intensity", 0.01f, 100f, ctx.keyLight.intensity, ControlScale.LOG, value->ctx.keyLight.intensity=value);

		// key light orientation picker
		ClickListener listener = new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Ray ray = ctx.cameraManager.getCamera().getPickRay(Gdx.input.getX(), Gdx.input.getY());
				ctx.keyLight.direction.set(ray.direction).scl(-1);
				ctx.stage.removeCaptureListener(this);
			}
		};
		
		klTable.add(UI.trig(skin, "pick sun position from skybox", ()->{
			ctx.stage.addCaptureListener(listener);
		})).colspan(3).row();
		
		klTable.add(UI.trig(skin, "copy light code", ()->{
			ComposerCode.toClipboard(ctx.keyLight);
		})).colspan(3).row();
		
		controls.add(shadows.initUI(ctx, skin)).row();
		
		
		Frame eFrame = UI.frame("Emissive", skin);
		Table eTable = eFrame.getContentTable();
		controls.add(eFrame).row();
		
		UI.slider(eTable, "strength", 0.01f, 100f, ctx.compo.emissiveIntensity, ControlScale.LOG, value->ComposerUtils.setEmissiveIntensity(ctx, value));
		
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
		shadows.update(ctx, delta);
	}
	@Override
	public void renderOverlay(GLTFComposerContext ctx) {
		shadows.renderOverlay(ctx);
	}
}
