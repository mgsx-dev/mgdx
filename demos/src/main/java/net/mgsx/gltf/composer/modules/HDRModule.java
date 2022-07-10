package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene.PBRRenderTargets;
import net.mgsx.gltf.scene.RenderTargets;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class HDRModule implements GLTFComposerModule
{
	private SpriteBatch batch;
	
	private ToneMappingModule toneMappingModule = new ToneMappingModule();
	private BloomModule bloomModule = new BloomModule();
	
	
	public HDRModule() {
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table t = UI.table(skin);
		
		t.add(bloomModule.initUI(ctx, skin)).fill().row();
		
		t.add(toneMappingModule.initUI(ctx, skin)).fill().row();
		
		return t;
	}
	
	@Override
	public void show(GLTFComposerContext ctx) {
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		ctx.colorShaderConfig.manualGammaCorrection = false;
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
		ctx.fbo.ensureScreenSize();
		ctx.fbo.begin();
		ScreenUtils.clear(ctx.clearColor, true);
		ctx.sceneManager.renderColors();
		ctx.fbo.end();
		
		// post process
		
		// TODO need to render to first layer only ! or need another FBO and compose later with tone mapping ?
		// render bloom
		bloomModule.render(batch, ctx.fbo.getFrameBuffer());
		
		// render final with tone mapping (HDR to LDR)
		toneMappingModule.render(batch, ctx.fbo.getTexture(PBRRenderTargets.COLORS));
	}

}
