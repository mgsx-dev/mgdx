package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltfx.GLFormat;
import net.mgsx.gltfx.mrt.PBRRenderTargets;
import net.mgsx.gltfx.mrt.RenderTargets;
import net.mgsx.gltfx.mrt.RenderTargets.Usage;

public class MRTModule implements GLTFComposerModule {

	private Usage usage = PBRRenderTargets.COLORS;
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table t = UI.table(skin);
		t.defaults().growX();
		
		Array<Usage> usages = new Array<RenderTargets.Usage>();
		usages.add(PBRRenderTargets.COLORS);
		usages.add(PBRRenderTargets.BASE_COLOR);
		usages.add(PBRRenderTargets.EMISSIVE);
		usages.add(PBRRenderTargets.GLOBAL_POSITION);
		usages.add(PBRRenderTargets.LOCAL_POSITION);
		usages.add(PBRRenderTargets.NORMAL);
		usages.add(PBRRenderTargets.ORM);
		usages.add(PBRRenderTargets.DIFFUSE);
		usages.add(PBRRenderTargets.SPECULAR);
		usages.add(PBRRenderTargets.TRANSMISSION);
		
		// TODO add more : diffuse / specular / lights (all)
		// TODO some debug : F0 F90, FDG, etc...
		
		
		// TODO not sure
//		usages.add(PBRRenderTargets.DEPTH);
//		usages.add(PBRRenderTargets.STENCIL);
		
		
		t.add("layer");
		t.add(UI.selector(skin, usages, usage, u->u.alias, u->{
			changeLayer(ctx, u);
		}));
		t.row();

		return t;
	}
	
	private void changeLayer(GLTFComposerContext ctx, Usage usage){
		
		this.usage = usage;
		
		ctx.fbo.clear();
		ctx.fbo.setDepth(false);
		
		
		ctx.fbo.replaceLayer(usage, GLFormat.RGBA8);
//		ctx.fbo.replaceLayer(PBRRenderTargets.GLOBAL_POSITION, GLFormat.RGB16);
//		ctx.fbo.replaceLayer(PBRRenderTargets.NORMAL, GLFormat.RGB16);
		ctx.invalidateFBO();
		
		ctx.colorShaderConfig.vertexShader = null;
		ctx.fbo.configure(ctx.colorShaderConfig);
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		ctx.colorShaderConfig.manualGammaCorrection = false;
		ctx.colorShaderConfig.transmissionSRGB = SRGB.NONE;
		ctx.invalidateShaders();
	}
	
	@Override
	public void show(GLTFComposerContext ctx) {
		changeLayer(ctx, PBRRenderTargets.COLORS);
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		
		ctx.sceneManager.renderShadows();
		ctx.sceneManager.renderTransmission();
		
		ctx.fbo.ensureScreenSize();
		ctx.fbo.begin();
		ctx.sceneManager.setSkyBox(null);
		ScreenUtils.clear(Color.CLEAR, true);
		ctx.sceneManager.renderColors();
		ctx.sceneManager.setSkyBox(ctx.skyBox);
		ctx.fbo.end();
		
		// render layer to LDR fbo
		Texture layer = ctx.fbo.getTexture(usage);
		if(layer != null){
			ctx.batch.disableBlending();
			FrameBufferUtils.blit(ctx.batch, layer, ctx.ldrFbo);
			ctx.batch.enableBlending();
		}
	}
	
}
