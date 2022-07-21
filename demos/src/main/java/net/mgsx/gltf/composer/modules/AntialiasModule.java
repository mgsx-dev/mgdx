package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.GLUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class AntialiasModule implements GLTFComposerModule
{
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Frame frame = UI.frame("Multi-sampling", skin);
		Table tb = frame.getContentTable();
		{
			tb.add("samples");
			int maxPoT = GLUtils.getMaxSamplesPoT();
			Array<Integer> items = new Array<Integer>();
			for(int i=0 ; i<=maxPoT ; i++){
				items.add(1 << i);
			}
			tb.add(UI.selector(skin, items, ctx.msaa, v->(v == 1 ? "none" : v+"x"), v->{ctx.msaa=v; ctx.fbo.setSamples(v);}));
			tb.row();
		}
		{
			tb.add("debug zoom");
			Array<Integer> items = new Array<Integer>();
			for(int i=0 ; i<=6 ; i++){
				items.add(1 << i);
			}
			tb.add(UI.selector(skin, items, ctx.pixelZoom, v->(v == 1 ? "none" : v+"x"), v->{ctx.pixelZoom=v;}));
			tb.row();
		}
		return frame;
	}

}
