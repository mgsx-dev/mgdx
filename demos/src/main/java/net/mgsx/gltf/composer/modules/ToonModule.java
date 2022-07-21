package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene.PBRRenderTargets;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class ToonModule implements GLTFComposerModule
{
	private OutlineDepthModule outline = new OutlineDepthModule();
	private boolean ceilEnabled = true;

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table t = UI.table(skin);
		t.defaults().fill();
		
		Frame ceilFrame = UI.frameToggle("Ceil shading", skin, ceilEnabled, v->ceilEnabled=v);
		t.add(ceilFrame).row();
		
		t.add(outline.initUI(ctx, skin)).growX().row();
		return t;
	}
	
	@Override
	public void show(GLTFComposerContext ctx) {
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		ctx.colorShaderConfig.manualGammaCorrection = true;
		ctx.colorShaderConfig.glslVersion = "#version 330\n";
		ctx.colorShaderConfig.vertexShader = null;
		ctx.colorShaderConfig.fragmentShader = Gdx.files.classpath("shaders/gdx-ceil.fs.glsl").readString();
		ctx.invalidateShaders();

		ctx.fbo.clear();
		ctx.fbo.setDepth(false);
		ctx.fbo.replaceLayer(PBRRenderTargets.COLORS, GLFormat.RGBA8);
		ctx.invalidateFBO();
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		ctx.sceneManager.renderShadows();

		ctx.fbo.ensureScreenSize();
		ctx.fbo.begin();
		ScreenUtils.clear(ctx.compo.clearColor, true);
		if(ceilEnabled){
			ctx.sceneManager.renderColors();
		}
		ctx.fbo.end();

		outline.render(ctx, ctx.fbo.getFrameBuffer());
		
		ctx.batch.disableBlending();
		FrameBufferUtils.blit(ctx.batch, ctx.fbo.getColorBufferTexture(), ctx.ldrFbo);
		ctx.batch.enableBlending();
	}
}
