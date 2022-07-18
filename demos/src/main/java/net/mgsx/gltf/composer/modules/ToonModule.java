package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBufferMultisample;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene.PBRRenderTargets;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class ToonModule implements GLTFComposerModule
{
	private OutlineDepthModule outline = new OutlineDepthModule();
	private SpriteBatch batch = new SpriteBatch();
	private FrameBufferMultisample fbo;
	private AntialiasModule antialiasModule = new AntialiasModule();
	private boolean outlineOnly;

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table t = UI.table(skin);
		t.add(UI.toggle(skin, "outline only", outlineOnly, v->outlineOnly=v)).row();
		t.add(antialiasModule.initUI(ctx, skin)).row();
		t.add(outline.initUI(ctx, skin)).row();
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
		ScreenUtils.clear(ctx.compo.clearColor, true);

		if(!outlineOnly){
			if(ctx.msaa > 1){
				fbo = FrameBufferUtils.ensureScreenSize(fbo, GLFormat.RGBA8, true, ctx.msaa);
				fbo.begin();
			}
			
			ScreenUtils.clear(ctx.compo.clearColor, true);
			ctx.sceneManager.render();
			
			if(ctx.msaa > 1){
				fbo.end();
				FrameBufferUtils.blit(batch, fbo.getColorBufferTexture());
			}
		}

		outline.render(ctx, null, batch);
		
		if(!outlineOnly && fbo != null){
			antialiasModule.render(ctx, fbo.getColorBufferTexture(), batch);
		}
	}
}
