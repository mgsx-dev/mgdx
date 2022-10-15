package net.mgsx.gfx;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

import net.mgsx.gdx.graphics.glutils.TextureUtils;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltfx.GLFormat;
import net.mgsx.gltfx.ShaderProgramUtils;

public class Cavity implements Disposable
{
	private ShaderProgram cavityScreenShader;
	private ShaderProgram cavityWorldShader;
	private ShaderProgram cavityMixShader;
	
	private FrameBuffer fboScreen, fboWorld;
	
	public float screenRidge = 1f;
	public float screenValley = 1f;
	public float worldRidge = 1f;
	public float worldValley = 1f;
	public int worldSamples = 16;
	public float worldDistance = .2f;
	public float worldAttenuation = 1f;
	private Blur blur;
	public boolean screenEnabled = true;
	public boolean worldEnabled = true;
	

	public Cavity() {
		
		blur = new Blur(true);
		
		cavityScreenShader = ShaderProgramUtils.createPixelShader("shaders/cavity-screen.fs.glsl");
		cavityWorldShader = ShaderProgramUtils.createPixelShader("shaders/cavity-world.fs.glsl");
		cavityMixShader = ShaderProgramUtils.createPixelShader("shaders/cavity-mix.fs.glsl", "#define RIDGE_BURN\n");
	}
	
	public void render(SpriteBatch batch, FrameBuffer output, Texture baseColor, Texture position, Texture normals, Texture noise) {
		if(screenEnabled || worldEnabled){
			position.bind(2);
			normals.bind(1);
			baseColor.bind(0);
	
			batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);
	
			// screen cavity
			if(screenEnabled)
			{
				fboScreen = FrameBufferUtils.ensureSize(fboScreen, GLFormat.RGB8, output);
				
				cavityScreenShader.bind();
				cavityScreenShader.setUniformi("u_textureNormals", 1);
				cavityScreenShader.setUniformi("u_texturePosition", 2);
				
				float screenSampleDistance = 1f;
				cavityScreenShader.setUniformf("u_sampleDistance", screenSampleDistance / baseColor.getWidth(), screenSampleDistance / baseColor.getHeight());
				
				FrameBufferUtils.blit(batch, baseColor, fboScreen, cavityScreenShader);
			}
			
			// world
			if(worldEnabled)
			{
				fboWorld = FrameBufferUtils.ensureSize(fboWorld, GLFormat.RGB8, output);
				
				noise.bind(3);
				TextureUtils.setActive(0);
				
				cavityWorldShader.bind();
				cavityWorldShader.setUniformi("u_textureNormals", 1);
				cavityWorldShader.setUniformi("u_texturePosition", 2);
				cavityWorldShader.setUniformi("u_textureNoise", 3);
				
				float wd = worldDistance * 10;
				cavityWorldShader.setUniformf("u_sampleDistance", wd / output.getWidth(), wd / output.getHeight());
				cavityWorldShader.setUniformi("u_samples", worldSamples);
				
				FrameBufferUtils.blit(batch, baseColor, fboWorld, cavityWorldShader);
				
				blur.blurPasses = 2;
				blur.render(batch, fboWorld.getColorBufferTexture());
			}
	
			// mix
			blur.getOutput().bind(2);
			fboScreen.getColorBufferTexture().bind(1);
			baseColor.bind(0);
			
			cavityMixShader.bind();
			cavityMixShader.setUniformi("u_textureCavityScreen", 1);
			cavityMixShader.setUniformi("u_textureCavityWorld", 2);
			cavityMixShader.setUniformf("u_mix", screenRidge / 2 * (screenEnabled ? 1 : 0), screenValley / 2 * (screenEnabled ? 1 : 0), worldRidge / 2 * (worldEnabled ? 1 : 0), worldValley / 2 * (worldEnabled ? 1 : 0));
			
			FrameBufferUtils.blit(batch, baseColor, output, cavityMixShader);
			
			// restore
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
	}
	
	@Override
	public void dispose() {
		cavityMixShader.dispose();
		fboScreen.dispose();
		fboWorld.dispose();
		cavityScreenShader.dispose();
	}
}
