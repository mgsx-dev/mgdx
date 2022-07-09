package net.mgsx.gfx;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gdx.utils.ShaderProgramUtils;

public class NoiseCache {
	
	public static void createGradientNoise(SpriteBatch batch, FrameBuffer output, float frequency){
		String options = "#define RENDER_GRADIENT\n";
		ShaderProgram shader = ShaderProgramUtils.createPixelShader("shaders/noise/noise3Dgrad.glsl", options);
		
		shader.bind();
		shader.setUniformf("u_frequency", frequency);
		shader.setUniformf("u_seed", 1f);
		
		FrameBufferUtils.blit(batch, shader, output);
		
		shader.dispose();
		
		output.getColorBufferTexture().setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		output.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}
}
