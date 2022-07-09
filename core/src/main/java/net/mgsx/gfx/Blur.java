package net.mgsx.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gdx.utils.ShaderProgramUtils;

public class Blur implements Disposable {
	private FrameBuffer fboA, fboB;
	private ShaderProgram blurShader;
	
	public float blurDistance = 1f;
	public float blurScale = 1f;
	public int blurPasses = 1;
	private final GLFormat format;
	
	public Blur(boolean alphaClip) {
		this(alphaClip, GLFormat.RGBA8);
	}
	public Blur(boolean alphaClip, GLFormat format) {
		this.format = format;
		String options = "";
		if(alphaClip) options += "#define ALPHA_CLIP\n";
		if(format.type == GL20.GL_FLOAT){
			options += "#define NO_CLIP\n";
		}
		blurShader = new ShaderProgram(
				Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(),
				options + Gdx.files.classpath("shaders/blur.fs.glsl").readString());
		ShaderProgramUtils.check(blurShader);
	}

	@Override
	public void dispose() {
		fboA.dispose();
		fboB.dispose();
		blurShader.dispose();
	}
	
	public Texture render(SpriteBatch batch, Texture texture){
		fboA = FrameBufferUtils.ensureScreenSize(fboA, format);
		fboB = FrameBufferUtils.ensureScreenSize(fboB, format);
		
		// capture
		FrameBufferUtils.blit(batch, texture, fboA);
		
		// blur
		batch.setShader(blurShader);
		blurShader.bind();
		
		blurShader.setUniformf("u_fade", blurScale);
		
		for(int blurPass = 0 ; blurPass<blurPasses ; blurPass++){
			blurShader.setUniformf("u_dir", blurDistance / fboA.getWidth(), 0);
			FrameBufferUtils.blit(batch, fboA.getColorBufferTexture(), fboB);
			
			blurShader.setUniformf("u_dir", 0, blurDistance / fboA.getHeight());
			FrameBufferUtils.blit(batch, fboB.getColorBufferTexture(), fboA);
		}
		
		return getOutput();
	}

	public Texture getOutput() {
		return fboA.getColorBufferTexture();
	}
	
}
