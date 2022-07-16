package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gfx.BlurCascadeGaussian;
import net.mgsx.gfx.BrighnessExtractShader;

// TODO move to core ?
public class Bloom {
	private final BlurCascadeGaussian blur;
	private final BrighnessExtractShader bloomExtract;
	
	public static class BloomOptions {
		public float blurMix = .5f, bloomRate = 1f, threshold = 1f, maxBrightness = 30f;
		
		public float stages = 7.5f;
	}
	
	public Bloom() {
		blur = new BlurCascadeGaussian(GLFormat.RGB16);
		bloomExtract = new BrighnessExtractShader(false, true);
	}
	
	public void apply(FrameBuffer fbo, SpriteBatch batch, BloomOptions settings){
		bloomExtract.bind();
		bloomExtract.setThresholdRealistic(settings.threshold);
		bloomExtract.setMaxBrightness(settings.maxBrightness);
		
		blur.blurMix = settings.blurMix;
		Texture bloomTexture = blur.render(fbo.getColorBufferTexture(), settings.stages, bloomExtract);
		
		fbo.begin();
		batch.enableBlending();
		Gdx.gl.glBlendColor(1, 1, 1, settings.bloomRate);
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