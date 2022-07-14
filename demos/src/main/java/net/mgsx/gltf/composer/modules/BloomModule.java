package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gfx.BlurCascadeGaussian;
import net.mgsx.gfx.BrighnessExtractShader;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class BloomModule implements GLTFComposerModule
{
	// TODO move to core ?
	private static class Bloom {
		private final BlurCascadeGaussian blur;
		private final BrighnessExtractShader bloomExtract;
		
		public float blurMix, bloomRate, threshold, maxBrightness;
		
		public float stages = 7.5f;
		
		public Bloom() {
			blur = new BlurCascadeGaussian(GLFormat.RGB16);
			bloomExtract = new BrighnessExtractShader(false, true);
			bloomRate = 1f;
			blurMix = .5f;
			threshold = 1;
			maxBrightness = 30f;
		}
		
		public void apply(FrameBuffer fbo, SpriteBatch batch){
			bloomExtract.bind();
			bloomExtract.setThresholdRealistic(threshold);
			bloomExtract.setMaxBrightness(maxBrightness);
			
			blur.blurMix = blurMix;
			Texture bloomTexture = blur.render(fbo.getColorBufferTexture(), stages, bloomExtract);
			
			fbo.begin();
			batch.enableBlending();
			Gdx.gl.glBlendColor(1, 1, 1, bloomRate);
			batch.setBlendFunction(GL20.GL_CONSTANT_ALPHA, GL20.GL_ONE);
			batch.begin();
			batch.draw(bloomTexture, 0, 0, 1, 1, 0, 0, 1, 1);
			batch.end();
			fbo.end();
			
			// restore
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			Gdx.gl.glBlendColor(0,0,0,0);
		}
	}

	
	private boolean enabled = false;
	
	private Bloom bloom = new Bloom();
	
	public BloomModule() {
		bloom.bloomRate = 1f;
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Frame frame = UI.frameToggle("Bloom", skin, enabled, value->enabled=value);
		Table t = frame.getContentTable();
		
		UI.slider(t, "threshold", 1e-3f, 1e2f, bloom.threshold, ControlScale.LOG, value->bloom.threshold=value);
		UI.slider(t, "clip", 1f, 1e3f, bloom.maxBrightness, ControlScale.LOG, value->bloom.maxBrightness=value);
		UI.slider(t, "stages", 0, 12, bloom.stages, value->bloom.stages=value);
		UI.slider(t, "blur size", 0, 1, bloom.blurMix, value->bloom.blurMix=value);
		UI.slider(t, "mix rate", .001f, 10, bloom.bloomRate, ControlScale.LOG, value->bloom.bloomRate=value);

		return frame;
	}

	public void render(SpriteBatch batch, FrameBuffer fbo) {
		if(enabled){
			bloom.apply(fbo, batch);
		}
	}
}
