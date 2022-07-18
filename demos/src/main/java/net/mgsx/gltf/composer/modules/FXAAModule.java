package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gfx.FXAAShader;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class FXAAModule implements GLTFComposerModule
{
	private FrameBuffer fboAA;
	private FXAAShader shader;

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Frame frame = UI.frameToggle("FXAA", skin, ctx.compo.fxaaEnabled, v->ctx.compo.fxaaEnabled = v);
		Table table = frame.getContentTable();
		UI.sliderTable(table, "min", 1e-3f, 1, ctx.compo.fxaa.reduceMin, ControlScale.LOG, v->ctx.compo.fxaa.reduceMin=v);
		UI.sliderTable(table, "mul", 1e-3f, 1, ctx.compo.fxaa.reduceMul, ControlScale.LOG, v->ctx.compo.fxaa.reduceMul=v);
		UI.sliderTable(table, "max", 1, 1e2f, ctx.compo.fxaa.spanMax, ControlScale.LOG, v->ctx.compo.fxaa.spanMax=v);
		return frame;
	}
	
	public Texture render(GLTFComposerContext ctx, Texture texture){
		if(ctx.compo.fxaaEnabled){
			if(shader == null) shader = new FXAAShader(false);
			shader.bind();
			shader.setSize(texture.getWidth(), texture.getHeight());
			shader.setConfig(ctx.compo.fxaa.reduceMin, ctx.compo.fxaa.reduceMul, ctx.compo.fxaa.spanMax);
			fboAA = FrameBufferUtils.ensureSize(fboAA, GLFormat.RGB16, texture.getWidth(), texture.getHeight(), false);
			ctx.batch.disableBlending();
			FrameBufferUtils.blit(ctx.batch, texture, fboAA, shader);
			ctx.batch.enableBlending();
			return fboAA.getColorBufferTexture();
		}else{
			return texture;
		}
	}
}
