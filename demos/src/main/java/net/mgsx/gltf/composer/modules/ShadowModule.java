package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.ComposerUtils;

public class ShadowModule implements GLTFComposerModule
{
	private Table controls;

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = new Table(skin);
		controls.defaults().grow();
		updateUI(ctx, skin);
		return controls;
	}
	private void updateUI(GLTFComposerContext ctx, Skin skin){
		controls.clear();
		
		Frame frame = UI.frameToggle("Shadows", skin, ctx.compo.shadows, value->{ctx.compo.shadows = value; ComposerUtils.recreateLight(ctx);});
		Table t = frame.getContentTable();
		
		Array<Integer> shadowSizes = new Array<Integer>();
		for(int i=8 ; i<=12 ; i++) shadowSizes.add(1<<i);
		t.add(UI.selector(skin, shadowSizes, ctx.compo.shadowSize, v->v+"x"+v, v->{ctx.compo.shadowSize = v; ComposerUtils.updateShadowSize(ctx);})).row();
		
		UI.slider(t, "Shadow bias", 1e-3f, 1f, ctx.compo.shadowBias, ControlScale.LOG, value->ComposerUtils.updateShadowBias(ctx, value));

		controls.add(frame);
	}
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		if(ctx.compositionJustChanged){
			updateUI(ctx, ctx.skin);
		}
	}
	
}
