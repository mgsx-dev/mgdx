package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class BloomModule implements GLTFComposerModule
{
	private Bloom bloom = new Bloom();
	private Table controls;
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = new Table();
		updateUI(ctx, skin);
		return controls;
	}
	private void updateUI(GLTFComposerContext ctx, Skin skin) {
		controls.clear();
		controls.defaults().growX();
		Frame frame = UI.frameToggle("Bloom", skin, ctx.compo.bloomEnabled, value->ctx.compo.bloomEnabled=value);
		Table t = frame.getContentTable();
		
		UI.slider(t, "threshold", 1e-3f, 1e2f, ctx.compo.bloom.threshold, ControlScale.LOG, value->ctx.compo.bloom.threshold=value);
		UI.slider(t, "clip", 1f, 1e3f, ctx.compo.bloom.maxBrightness, ControlScale.LOG, value->ctx.compo.bloom.maxBrightness=value);
		UI.slider(t, "stages", 0, 12, ctx.compo.bloom.stages, value->ctx.compo.bloom.stages=value);
		UI.slider(t, "blur size", 0, 1, ctx.compo.bloom.blurMix, value->ctx.compo.bloom.blurMix=value);
		UI.slider(t, "mix rate", .001f, 10, ctx.compo.bloom.bloomRate, ControlScale.LOG, value->ctx.compo.bloom.bloomRate=value);
		
		controls.add(frame);
	}

	public void render(GLTFComposerContext ctx, FrameBuffer fbo) {
		if(ctx.compositionJustChanged){
			updateUI(ctx, ctx.skin);
		}
		
		if(ctx.compo.bloomEnabled){
			bloom.apply(fbo, ctx.batch, ctx.compo.bloom);
		}
	}

}
