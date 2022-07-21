package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gfx.Cavity;
import net.mgsx.gfx.NoiseCache;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene.PBRRenderTargets;
import net.mgsx.gltf.scene.RenderTargets;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class CavityModule implements GLTFComposerModule
{
	private Cavity cavity = new Cavity();
	private FrameBuffer noise;
	
	private RenderTargets.Usage colorTarget = PBRRenderTargets.BASE_COLOR; // TODO allow to use PBRRenderTargets.COLORS instead 
	
	@Override
	public void show(GLTFComposerContext ctx) {

		ctx.fbo.clear();
		ctx.fbo.setDepth(false);
		ctx.fbo.replaceLayer(colorTarget, GLFormat.RGBA8);
		ctx.fbo.replaceLayer(PBRRenderTargets.GLOBAL_POSITION, GLFormat.RGB16);
		ctx.fbo.replaceLayer(PBRRenderTargets.NORMAL, GLFormat.RGB16);
		ctx.invalidateFBO();

		ctx.colorShaderConfig.vertexShader = null;
		ctx.fbo.configure(ctx.colorShaderConfig);
		ctx.colorShaderConfig.manualSRGB = SRGB.NONE;
		ctx.colorShaderConfig.manualGammaCorrection = true;
		ctx.invalidateShaders();
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		
		ctx.sceneManager.renderShadows();
		ctx.fbo.ensureScreenSize();
		ctx.fbo.begin();
		ctx.sceneManager.setSkyBox(null);
		ScreenUtils.clear(ctx.compo.clearColor, true);
		ctx.sceneManager.renderColors();
		ctx.sceneManager.setSkyBox(ctx.skyBox);
		ctx.fbo.end();

		// post process
		ScreenUtils.clear(ctx.compo.clearColor, true);
		
		if(cavity.screenEnabled || cavity.worldEnabled){
			if(noise == null){
				noise = new FrameBuffer(Format.RGBA8888, 1024, 1024, false);
				NoiseCache.createGradientNoise(ctx.batch, noise, 1f);
			}
			// TODO need another FBO to avoid drawing inputs to the same output...
			// BASE_COLOR should be used for calculation
			// and COLORS to be mixed over
			
			cavity.render(ctx.batch, ctx.fbo.getFrameBuffer(),
					ctx.fbo.getTexture(colorTarget),
					ctx.fbo.getTexture(PBRRenderTargets.GLOBAL_POSITION),
					ctx.fbo.getTexture(PBRRenderTargets.NORMAL),
					noise.getColorBufferTexture());
			
			ctx.batch.disableBlending();
			FrameBufferUtils.blit(ctx.batch, ctx.fbo.getTexture(colorTarget), ctx.ldrFbo);
			ctx.batch.enableBlending();
		}else{
			ctx.batch.disableBlending();
			FrameBufferUtils.blit(ctx.batch, ctx.fbo.getTexture(colorTarget), ctx.ldrFbo);
			ctx.batch.enableBlending();
		}
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table table = UI.table(skin);
		table.defaults().growX();
		
		Frame scrFrame = UI.frameToggle("Screen", skin, cavity.screenEnabled, v->cavity.screenEnabled=v);
		Table scrTable = scrFrame.getContentTable();
		
		UI.sliderTable(scrTable, "ridge", 0f, 2f, cavity.screenRidge, value->cavity.screenRidge = value);
		UI.sliderTable(scrTable, "valley",0f, 2f, cavity.screenValley, value->cavity.screenValley = value);
		
		Frame wrdFrame = UI.frameToggle("World", skin, cavity.worldEnabled, v->cavity.screenEnabled=v);
		Table wrdTable = wrdFrame.getContentTable();
		
		UI.sliderTable(wrdTable, "ridge", 0f, 2.5f, cavity.worldRidge, value->cavity.worldRidge = value);
		UI.sliderTable(wrdTable, "valley",0f, 2.5f, cavity.worldValley, value->cavity.worldValley = value);
		UI.sliderTablei(wrdTable, "samples",1, 64, cavity.worldSamples, value->cavity.worldSamples = value);
		UI.sliderTable(wrdTable, "distance",0f, 1f, cavity.worldDistance, value->cavity.worldDistance = value);
		UI.sliderTable(wrdTable, "attenuation",0f, 100f, cavity.worldAttenuation, value->cavity.worldAttenuation = value);

		table.add(scrFrame).row();
		table.add(wrdFrame).row();
		
		return table;
	}

	
}
