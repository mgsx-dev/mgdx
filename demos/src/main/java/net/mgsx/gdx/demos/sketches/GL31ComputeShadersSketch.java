package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.ComputeShader;
import net.mgsx.gdx.graphics.GL31;
import net.mgsx.gdx.utils.ShaderProgramUtils;

public class GL31ComputeShadersSketch extends ScreenAdapter
{
	private Texture texture;
	private ComputeShader computeShader;
	private SpriteBatch batch;
	private float time;

	public GL31ComputeShadersSketch() {
		
		batch = new SpriteBatch();
		
		texture = new Texture(64, 64, Format.RGBA8888);
		
		computeShader = new ComputeShader(Gdx.files.classpath("shaders/demo.cs.glsl").readString());
		ShaderProgramUtils.check(computeShader);
		
		Mgdx.gl31.glBindImageTexture(0, texture.getTextureObjectHandle(), 0, false, 0, GL31.GL_WRITE_ONLY, GL30.GL_RGBA8);
		
	}
	
	@Override
	public void render(float delta) {
		time += delta;
		
		computeShader.bind();
		computeShader.setUniformf("u_time", time);
		
		Mgdx.gl31.glDispatchCompute(texture.getWidth(), texture.getHeight(), 1);

		// Some other rendering command could be done here
		
		Mgdx.gl31.glMemoryBarrier(GL31.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
		
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.begin();
		batch.draw(texture, 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
	}
	
	@Override
	public void dispose() {
		texture.dispose();
		computeShader.dispose();
		batch.dispose();
	}
}
