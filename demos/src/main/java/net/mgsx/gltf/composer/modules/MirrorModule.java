package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.ComposerUtils;

public class MirrorModule implements GLTFComposerModule {

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
		Frame frame = UI.frameToggle("Mirror", skin, ctx.compo.mirrorEnabled, value->ComposerUtils.enableMirror(ctx, value));
		Table t = frame.getContentTable();
		
		UI.slider(t, "plane X", -1, 1, ctx.compo.mirror.normal.x, ControlScale.LIN, value->{ctx.compo.mirror.normal.x=value; ComposerUtils.applyMirror(ctx);});
		UI.slider(t, "plane Y", -1, 1, ctx.compo.mirror.normal.y, ControlScale.LIN, value->{ctx.compo.mirror.normal.y=value; ComposerUtils.applyMirror(ctx);});
		UI.slider(t, "plane Z", -1, 1, ctx.compo.mirror.normal.z, ControlScale.LIN, value->{ctx.compo.mirror.normal.z=value; ComposerUtils.applyMirror(ctx);});
		UI.slider(t, "plane O", -10, 10, ctx.compo.mirror.origin, ControlScale.LIN, value->{ctx.compo.mirror.origin=value; ComposerUtils.applyMirror(ctx);});
		UI.toggle(t, "clip", ctx.compo.mirror.clip, value->{ctx.compo.mirror.clip=value; ComposerUtils.applyMirror(ctx);});
		
		controls.add(frame);
	}
}
