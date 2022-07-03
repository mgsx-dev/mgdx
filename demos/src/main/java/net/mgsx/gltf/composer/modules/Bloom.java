package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gfx.BlurCascade;
import net.mgsx.gfx.BlurCascade.BlurMixMode;
import net.mgsx.gfx.BrighnessExtractShader;

// TODO move to core ?
public class Bloom {
	private BlurCascade blur;
	private BrighnessExtractShader bloomExtract;
	
	public float blurMix, bloomRate;
	
	public Bloom() {
		blur = new BlurCascade(GLFormat.RGB32, 32);
		bloomExtract = new BrighnessExtractShader();
		bloomRate = 1f;
		blurMix = .3f;
	}
	
	public void apply(FrameBuffer fbo, SpriteBatch batch){
		bloomExtract.bind();
		bloomExtract.setThresholdRealistic(1f);
		blur.setMixFunc(BlurMixMode.ADD, blurMix);
		Texture blooTexture = blur.render(fbo.getColorBufferTexture(), Integer.MAX_VALUE, bloomExtract);
		
		fbo.begin();
		batch.enableBlending();
		Gdx.gl.glBlendColor(1, 1, 1, bloomRate);
		batch.setBlendFunction(GL20.GL_CONSTANT_ALPHA, GL20.GL_ONE);
		batch.begin();
		batch.draw(blooTexture, 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
		fbo.end();
	}
}
