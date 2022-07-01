package net.mgsx.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.utils.Disposable;

/**
 * Same concept as {@link com.badlogic.gdx.graphics.glutils.FrameBuffer} but with
 * a more close to the metal API. You have more responsibilitis but more flexibility.
 * 
 * @author mgsx
 *
 */
public class FlexFrameBuffer implements Disposable {

	private int framebufferHandle;

	public FlexFrameBuffer() {
		framebufferHandle = Gdx.gl.glGenFramebuffer();
	}
	
	public void bind(){
		Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
	}
	public void unbind(){
		Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);
	}
	
	public void setAttachment(int attachmentIndex, GLTexture texture){
		setAttachment(attachmentIndex, texture.getTextureObjectHandle(), 0, texture.glTarget);
	}
	public void setAttachment(int attachmentIndex, GLTexture texture, int level){
		setAttachment(attachmentIndex, texture.getTextureObjectHandle(), level, texture.glTarget);
	}
	public void setAttachment(int attachmentIndex, GLTexture texture, int level, int cubemapFace){
		setAttachment(attachmentIndex, texture.getTextureObjectHandle(), level, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + cubemapFace);
	}
	public void setAttachment(int index, int glTexture, int level, int glTarget){
		Gdx.gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0 + index, glTarget, glTexture, level);
	}

	public void compile(){
		int result = Gdx.gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);
		if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED){
			throw new IllegalStateException("Frame buffer couldn't be constructed: unsupported");
		}
		if (result != GL20.GL_FRAMEBUFFER_COMPLETE) {
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
				throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete attachment");
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS)
				throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete dimensions");
			if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
				throw new IllegalStateException("Frame buffer couldn't be constructed: missing attachment");
			if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED)
				throw new IllegalStateException("Frame buffer couldn't be constructed: unsupported combination of formats");
			throw new IllegalStateException("Frame buffer couldn't be constructed: unknown error " + result);
		}
	}

	@Override
	public void dispose() {
		Gdx.gl.glDeleteFramebuffer(framebufferHandle);
	}

	public void begin(int width, int height) {
		bind();
		setViewport(width, height);
	}
	
	public void end(){
		unbind();
		resetViewport();
	}

	public void setViewport(int width, int height) {
		Gdx.gl.glViewport(0, 0, width, height);
	}
	
	public void resetViewport(){
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
	}

}
