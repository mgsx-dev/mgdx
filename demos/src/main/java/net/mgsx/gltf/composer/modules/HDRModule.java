package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltfx.GLFormat;
import net.mgsx.gltfx.mrt.RenderTargets;

public class HDRModule implements GLTFComposerModule
{
	private ToneMappingModule toneMappingModule = new ToneMappingModule();
	private BloomModule bloomModule = new BloomModule();
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table t = UI.table(skin);
		t.defaults().growX();

		t.add(bloomModule.initUI(ctx, skin)).row();
		
		t.add(toneMappingModule.initUI(ctx, skin)).row();
		
		return t;
	}
	
	@Override
	public void show(GLTFComposerContext ctx) {
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		ctx.colorShaderConfig.manualGammaCorrection = false;
		ctx.colorShaderConfig.vertexShader = null;
		ctx.colorShaderConfig.fragmentShader = null;
		ctx.invalidateShaders();
		ctx.fbo.clear();
		ctx.fbo.setDepth(false);
		ctx.fbo.replaceLayer(RenderTargets.COLORS, GLFormat.RGB16);
		ctx.invalidateFBO();
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		ctx.sceneManager.renderShadows();
		ctx.sceneManager.renderMirror();
		ctx.sceneManager.renderTransmission();
		ctx.fbo.ensureScreenSize();
		ctx.fbo.begin();
		ScreenUtils.clear(ctx.compo.clearColor, true);
		ctx.sceneManager.renderColors();
		ctx.fbo.end();
		
		// post process
		
		// TODO need to render to first layer only ! or need another FBO and compose later with tone mapping ?
		// render bloom
		bloomModule.render(ctx, ctx.fbo.getFrameBuffer());
		
		// render final with tone mapping (HDR to LDR)
		ctx.ldrFbo = FrameBufferUtils.ensureScreenSize(ctx.ldrFbo, GLFormat.RGB8);
		ctx.ldrFbo.begin();
		toneMappingModule.render(ctx, ctx.fbo.getFrameBuffer().getColorBufferTexture());
		ctx.ldrFbo.end();
	}

}
