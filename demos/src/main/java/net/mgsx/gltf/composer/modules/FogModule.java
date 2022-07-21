package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.ColorBox;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.ComposerUtils;

public class FogModule implements GLTFComposerModule
{
	private Table controls;

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = new Table(skin);
		updateUI(ctx, skin);
		return controls;
	}
	
	private void updateUI(GLTFComposerContext ctx, Skin skin){
		controls.clear();
		Frame frame = UI.frameToggle("Fog", skin, ctx.compo.fogEnabled, v->ComposerUtils.enableFog(ctx, v));
		Table t = frame.getContentTable();
		ColorBox colorBox = new ColorBox("Fog color", ()->ctx.compo.fog.color, true, skin);
		UI.change(colorBox, e->ComposerUtils.applyFog(ctx));
		t.add("color").right();
		t.add(colorBox).left().row();
		UI.sliderTable(t, "near", 1e-3f, 1, ctx.compo.fog.near, ControlScale.LOG, v->{ctx.compo.fog.near = v; ComposerUtils.applyFog(ctx);});
		UI.sliderTable(t, "far", 1e-3f, 1, ctx.compo.fog.far, ControlScale.LOG, v->{ctx.compo.fog.far = v; ComposerUtils.applyFog(ctx);});
		UI.sliderTable(t, "exponent", 0, 1, ctx.compo.fog.exponent, ControlScale.LIN, v->{ctx.compo.fog.exponent = v; ComposerUtils.applyFog(ctx);});
		controls.add(frame).growX();
	}
	
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		if(ctx.compositionJustChanged){
			updateUI(ctx, ctx.skin);
		}
	}
}
