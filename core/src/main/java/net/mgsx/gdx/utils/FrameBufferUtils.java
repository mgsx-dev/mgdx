package net.mgsx.gdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.mgsx.gdx.assets.CommonAssets;
import net.mgsx.gdx.graphics.GLFormat;

public class FrameBufferUtils {
	public static FrameBuffer create(GLFormat format){
		return create(format, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
	}
	public static FrameBuffer create(GLFormat format, int width, int height) {
		FrameBufferBuilder b = new FrameBufferBuilder(width, height);
		b.addColorTextureAttachment(format.internalFormat, format.format, format.type);
		return b.build();
	}
	
	public static FrameBuffer ensureScreenSize(FrameBuffer fbo, GLFormat format) {
		return ensureSize(fbo, format, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
	}
	public static FrameBuffer ensureSize(FrameBuffer fbo, GLFormat format, FrameBuffer match) {
		return ensureSize(fbo, format, match.getWidth(), match.getHeight());
	}
	public static FrameBuffer ensureSize(FrameBuffer fbo, GLFormat format, GLTexture texture) {
		return ensureSize(fbo, format, texture.getWidth(), texture.getHeight());
	}
	public static FrameBuffer ensureSize(FrameBuffer fbo, GLFormat format, int width, int height) {
		if(fbo == null || fbo.getWidth() != width || fbo.getHeight() != height){
			if(fbo != null) fbo.dispose();
			fbo = create(format);
		}
		return fbo;
	}
	public static void blit(SpriteBatch batch, Texture input, FrameBuffer output) {
		output.begin();
		blit(batch, input);
		output.end();
	}
	public static void blit(SpriteBatch batch, Texture input) {
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.begin();
		batch.draw(input, 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
	}
	public static void blit(SpriteBatch batch, Texture input, FrameBuffer output, ShaderProgram shader) {
		batch.setShader(shader);
		blit(batch, input, output);
		batch.setShader(null);
	}
	public static void blit(SpriteBatch batch, ShaderProgram shader, FrameBuffer output) {
		blit(batch, CommonAssets.whitePixel, output);
	}

}