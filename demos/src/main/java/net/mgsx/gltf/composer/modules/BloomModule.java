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
import net.mgsx.gfx.BlurCascade;
import net.mgsx.gfx.BlurCascade.BlurMixMode;
import net.mgsx.gfx.BrighnessExtractShader;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.composer.utils.UI.ControlScale;
import net.mgsx.gltf.composer.utils.UI.Frame;

public class BloomModule implements GLTFComposerModule
{
	// TODO move to core ?
	private static class Bloom {
		private final BlurCascade blur;
		private final BrighnessExtractShader bloomExtract;
		
		public float blurMix, bloomRate, threshold;
		
		public Bloom() {
			blur = new BlurCascade(GLFormat.RGB16, 32);
			bloomExtract = new BrighnessExtractShader();
			bloomRate = 1f;
			blurMix = .3f;
			threshold = 1;
		}
		
		public void apply(FrameBuffer fbo, SpriteBatch batch){
			bloomExtract.bind();
			bloomExtract.setThresholdRealistic(threshold);
			blur.setMixFunc(BlurMixMode.ADD, blurMix);
			Texture bloomTexture = blur.render(fbo.getColorBufferTexture(), Integer.MAX_VALUE, bloomExtract);
			
			fbo.begin();
			batch.enableBlending();
			Gdx.gl.glBlendColor(1, 1, 1, bloomRate);
			batch.setBlendFunction(GL20.GL_CONSTANT_ALPHA, GL20.GL_ONE);
			batch.begin();
			batch.draw(bloomTexture, 0, 0, 1, 1, 0, 0, 1, 1);
			batch.end();
			fbo.end();
		}
	}

	
	private boolean enabled = true;
	
	private Bloom bloom = new Bloom();
	
	public BloomModule() {
		bloom.bloomRate = .5f;
		bloom.blurMix = .5f;
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Frame frame = UI.frameToggle("Bloom", skin, enabled, value->enabled=value);
		Table t = frame.getContentTable();
		
		UI.slider(t, "threshold", 1e-3f, 1e2f, bloom.threshold, ControlScale.LOG, value->bloom.threshold=value);
		UI.slider(t, "blur size", 0, 1, bloom.blurMix, value->bloom.blurMix=value);
		UI.slider(t, "mix rate", 0, 1, bloom.bloomRate, value->bloom.bloomRate=value);

		return frame;
	}

	public void render(SpriteBatch batch, FrameBuffer fbo) {
		if(enabled){
			bloom.apply(fbo, batch);
		}
	}
}
