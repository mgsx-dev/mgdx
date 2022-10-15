package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.scenes.scene2d.ui.ColorBox;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gfx.OutlineDepth;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltfx.GLFormat;

public class OutlineDepthModule implements GLTFComposerModule
{
	private OutlineDepth outlineDepth = new OutlineDepth(true);
	private FrameBuffer fboDepth;
	private boolean enabled;
	
	public void render(GLTFComposerContext ctx, FrameBuffer fbo) {
		if(enabled){
			
			fboDepth = FrameBufferUtils.ensureScreenSize(fboDepth, GLFormat.RGBA8, true);
			fboDepth.begin();
			ScreenUtils.clear(0,0,0,0, true);
			ctx.sceneManager.setSkyBox(null);
			ctx.sceneManager.renderDepth();
			ctx.sceneManager.setSkyBox(ctx.skyBox);
			fboDepth.end();
			
			if(fbo != null) fbo.begin();
			outlineDepth.render(ctx.batch, fboDepth.getColorBufferTexture(), ctx.sceneManager.camera);
			if(fbo != null) fbo.end();
		}
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Frame frame = UI.frameToggle("Outlines", skin, enabled, v->enabled=v);
		Table t = frame.getContentTable();
		UI.sliderTable(t, "size", 1e-3f, 1e3f, outlineDepth.size, ControlScale.LOG, v->outlineDepth.size=v);
		UI.sliderTable(t, "depth min", 1e-3f, 1e3f, outlineDepth.depthMin, ControlScale.LOG, v->outlineDepth.depthMin=v);
		UI.sliderTable(t, "depth max", 1e-3f, 1e3f, outlineDepth.depthMax, ControlScale.LOG, v->outlineDepth.depthMax=v);
		UI.sliderTable(t, "falloff", 1e-3f, 1e3f, outlineDepth.distanceFalloff, ControlScale.LOG, v->outlineDepth.distanceFalloff=v);
		t.add(new ColorBox("inner", outlineDepth.insideColor, true, skin)).row();
		t.add(new ColorBox("outer", outlineDepth.outsideColor, true, skin)).row();
		return frame;
	}
	
}
