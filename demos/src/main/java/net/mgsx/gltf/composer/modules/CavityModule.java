package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.graphics.GLFormat;
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
	private final static int MODE_NONE = 0;
	private final static int MODE_SCREEN = 1;
	private final static int MODE_WORLD = 2;
	private final static int MODE_BOTH = 3;
	private int mode = MODE_BOTH;
	
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
		
		cavity.screenEnabled = (mode & MODE_SCREEN) != 0;
		cavity.worldEnabled = (mode & MODE_WORLD) != 0;
		if(mode != MODE_NONE){
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
		
		table.add(UI.selector(skin, new String[]{"None", "Screen", "World", "Both"}, mode, v->mode = v)).row();
		
		UI.slider(table, "Screen Ridge", 0f, 2f, cavity.screenRidge, value->cavity.screenRidge = value);
		UI.slider(table, "Screen Valley",0f, 2f, cavity.screenValley, value->cavity.screenValley = value);
		UI.slider(table, "World Ridge", 0f, 2.5f, cavity.worldRidge, value->cavity.worldRidge = value);
		UI.slider(table, "World Valley",0f, 2.5f, cavity.worldValley, value->cavity.worldValley = value);
		UI.slideri(table, "World Samples",1, 64, cavity.worldSamples, value->cavity.worldSamples = value);
		UI.slider(table, "World Distance",0f, 1f, cavity.worldDistance, value->cavity.worldDistance = value);
		UI.slider(table, "World Attenuation",0f, 100f, cavity.worldAttenuation, value->cavity.worldAttenuation = value);

		return table;
	}

	
}
