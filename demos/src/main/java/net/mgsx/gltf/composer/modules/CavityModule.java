package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.Frame;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gfx.Cavity;
import net.mgsx.gfx.NoiseCache;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene.PBRRenderTargets;

public class CavityModule implements GLTFComposerModule
{
	private final static int MODE_NONE = 0;
	private final static int MODE_SCREEN = 1;
	private final static int MODE_WORLD = 2;
	private final static int MODE_BOTH = 3;
	private int mode = MODE_BOTH;
	private boolean enabled;
	
	private Cavity cavity = new Cavity();
	private FrameBuffer noise;
	
	public CavityModule(GLTFComposerContext ctx) {
		enable(ctx, false);
	}
	private void enable(GLTFComposerContext ctx, boolean enabled) {
		this.enabled = enabled;
		if(enabled){
			ctx.fbo.replaceLayer(PBRRenderTargets.BASE_COLOR, GLFormat.RGBA8);
			ctx.fbo.replaceLayer(PBRRenderTargets.GLOBAL_POSITION, GLFormat.RGB16);
			ctx.fbo.replaceLayer(PBRRenderTargets.NORMAL, GLFormat.RGB16);
			ctx.invalidateFBO();
		}
	}
	
	public void render(SpriteBatch batch, PBRRenderTargets fbo) {
		cavity.screenEnabled = (mode & MODE_SCREEN) != 0;
		cavity.worldEnabled = (mode & MODE_WORLD) != 0;
		if(mode != MODE_NONE && enabled){
			if(noise == null){
				noise = new FrameBuffer(Format.RGBA8888, 1024, 1024, false);
				NoiseCache.createGradientNoise(batch, noise, 1f);
			}
			cavity.render(batch, fbo.getFrameBuffer(),
					fbo.getTexture(PBRRenderTargets.BASE_COLOR),
					fbo.getTexture(PBRRenderTargets.GLOBAL_POSITION),
					fbo.getTexture(PBRRenderTargets.NORMAL),
					noise.getColorBufferTexture());
			
			FrameBufferUtils.blit(batch, noise.getColorBufferTexture());
			
		}
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Frame frame = UI.frameToggle("Cavity", skin, enabled, v->enable(ctx, v));
		Table table = frame.getContentTable();
		
		table.add(UI.selector(skin, new String[]{"Screen", "World", "Both"}, mode-1, v->mode = v+1)).row();
		
		UI.slider(table, "Screen Ridge", 0f, 2f, cavity.screenRidge, value->cavity.screenRidge = value);
		UI.slider(table, "Screen Valley",0f, 2f, cavity.screenValley, value->cavity.screenValley = value);
		UI.slider(table, "World Ridge", 0f, 2.5f, cavity.worldRidge, value->cavity.worldRidge = value);
		UI.slider(table, "World Valley",0f, 2.5f, cavity.worldValley, value->cavity.worldValley = value);
		UI.slideri(table, "World Samples",1, 64, cavity.worldSamples, value->cavity.worldSamples = value);
		UI.slider(table, "World Distance",0f, 1f, cavity.worldDistance, value->cavity.worldDistance = value);
		UI.slider(table, "World Attenuation",0f, 100f, cavity.worldAttenuation, value->cavity.worldAttenuation = value);

		UI.enableRecursive(table, enabled);
		
		return frame;
	}

	
}
