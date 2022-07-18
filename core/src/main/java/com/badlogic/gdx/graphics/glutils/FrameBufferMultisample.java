package com.badlogic.gdx.graphics.glutils;

import java.nio.Buffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.IntArray;

import net.mgsx.gdx.Mgdx;

public class FrameBufferMultisample extends FrameBuffer
{
	private int samples;
	
	private int framebufferHandleMS;
	private int depthbufferHandleMS;
	private int stencilbufferHandleMS;
	private int depthStencilPackedBufferHandleMS;

	private IntArray renderBufferHandles = new IntArray();
	
	public FrameBufferMultisample(FrameBufferBuilder builder, int samples) {
		this.bufferBuilder = builder;
		this.samples = samples;
		build();
	}
	
	public void setSamples(int samples){
		if(samples != this.samples){
			dispose();
			build();
		}
	}
	public int getSamples() {
		return samples;
	}
	
	
	@Override
	public void begin() {
		super.begin();
		if(samples > 1){
			// ScreenUtils.clear(0, 0, 0, 0, true);
		}
	}
	
	@Override
	public void bind() {
		if(samples > 1){
			Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandleMS);
		}else{
			super.bind();
		}
	}
	
	@Override
	public void end() {
		super.end();
		if(samples > 1){
			// blit to non MS FBO
			Gdx.gl.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebufferHandleMS);
			Gdx.gl.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebufferHandle);
			Gdx.gl30.glBlitFramebuffer(
					0, 0, bufferBuilder.width, bufferBuilder.height, 
					0, 0, bufferBuilder.width, bufferBuilder.height, 
					GL20.GL_COLOR_BUFFER_BIT, GL20.GL_NEAREST);
			Gdx.gl.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
			Gdx.gl.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		}
	}
	
	@Override
	protected void build() {
		
		super.build();
		
		GL20 gl = Gdx.gl20;

		framebufferHandleMS = gl.glGenFramebuffer();
		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandleMS);

		int width = bufferBuilder.width;
		int height = bufferBuilder.height;

		if (bufferBuilder.hasDepthRenderBuffer) {
			depthbufferHandleMS = gl.glGenRenderbuffer();
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthbufferHandleMS);
			Mgdx.gl31.glRenderbufferStorageMultisample(GL20.GL_RENDERBUFFER, samples, bufferBuilder.depthRenderBufferSpec.internalFormat, width, height);
		}

		if (bufferBuilder.hasStencilRenderBuffer) {
			stencilbufferHandleMS = gl.glGenRenderbuffer();
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, stencilbufferHandleMS);
			Mgdx.gl31.glRenderbufferStorageMultisample(GL20.GL_RENDERBUFFER, samples, bufferBuilder.stencilRenderBufferSpec.internalFormat, width, height);
		}

		if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
			depthStencilPackedBufferHandleMS = gl.glGenRenderbuffer();
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthStencilPackedBufferHandleMS);
			Mgdx.gl31.glRenderbufferStorageMultisample(GL20.GL_RENDERBUFFER, samples, bufferBuilder.packedStencilDepthRenderBufferSpec.internalFormat, width,
				height);
		}
		
		

		int colorTextureCounter = 0;
		for (FrameBufferTextureAttachmentSpec attachmentSpec : bufferBuilder.textureAttachmentSpecs) {
			if (attachmentSpec.isColorTexture()) {
				// create color buffer
				int colorBufferHandle = Gdx.gl.glGenRenderbuffer();
				renderBufferHandles.add(colorBufferHandle);
				Gdx.gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, colorBufferHandle);
				Mgdx.gl31.glRenderbufferStorageMultisample(GL20.GL_RENDERBUFFER, samples, attachmentSpec.internalFormat, width, height);
				
				// attach render buffer
				Gdx.gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0 + colorTextureCounter, GL20.GL_RENDERBUFFER, colorBufferHandle);

				colorTextureCounter++;
			} // TODO depth and stencil as attachment !?
		}

		IntBuffer buffer = BufferUtils.newIntBuffer(colorTextureCounter);
		for (int i = 0; i < colorTextureCounter; i++) {
			buffer.put(GL30.GL_COLOR_ATTACHMENT0 + i);
		}
		((Buffer)buffer).position(0);
		Gdx.gl30.glDrawBuffers(colorTextureCounter, buffer);

		if (bufferBuilder.hasDepthRenderBuffer) {
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_RENDERBUFFER, depthbufferHandleMS);
		}

		if (bufferBuilder.hasStencilRenderBuffer) {
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER, stencilbufferHandleMS);
		}

		if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER, depthStencilPackedBufferHandleMS);
		}

		gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);

		int result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);

		if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED && bufferBuilder.hasDepthRenderBuffer && bufferBuilder.hasStencilRenderBuffer
			&& (Gdx.graphics.supportsExtension("GL_OES_packed_depth_stencil")
				|| Gdx.graphics.supportsExtension("GL_EXT_packed_depth_stencil"))) {
			if (bufferBuilder.hasDepthRenderBuffer) {
				gl.glDeleteRenderbuffer(depthbufferHandleMS);
				depthbufferHandleMS = 0;
			}
			if (bufferBuilder.hasStencilRenderBuffer) {
				gl.glDeleteRenderbuffer(stencilbufferHandleMS);
				stencilbufferHandleMS = 0;
			}
			if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
				gl.glDeleteRenderbuffer(depthStencilPackedBufferHandleMS);
				depthStencilPackedBufferHandleMS = 0;
			}

			depthStencilPackedBufferHandleMS = gl.glGenRenderbuffer();
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthStencilPackedBufferHandleMS);
			gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL_DEPTH24_STENCIL8_OES, width, height);
			gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);

			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_RENDERBUFFER,
				depthStencilPackedBufferHandleMS);
			gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER,
				depthStencilPackedBufferHandleMS);
			result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);
		}

		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);

		if (result != GL20.GL_FRAMEBUFFER_COMPLETE) {
			if (hasDepthStencilPackedBuffer) {
				gl.glDeleteBuffer(depthStencilPackedBufferHandleMS);
			} else {
				if (bufferBuilder.hasDepthRenderBuffer) gl.glDeleteRenderbuffer(depthbufferHandleMS);
				if (bufferBuilder.hasStencilRenderBuffer) gl.glDeleteRenderbuffer(stencilbufferHandleMS);
			}

			gl.glDeleteFramebuffer(framebufferHandleMS);

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
	public void dispose () {
		
		super.dispose();

		for (int i=0 ; i<renderBufferHandles.size ; i++) {
			Gdx.gl.glDeleteBuffer(renderBufferHandles.get(i));
		}
		renderBufferHandles.clear();

		if (hasDepthStencilPackedBuffer) {
			Gdx.gl.glDeleteRenderbuffer(depthStencilPackedBufferHandle);
		} else {
			if (bufferBuilder.hasDepthRenderBuffer) Gdx.gl.glDeleteRenderbuffer(depthbufferHandle);
			if (bufferBuilder.hasStencilRenderBuffer) Gdx.gl.glDeleteRenderbuffer(stencilbufferHandle);
		}

		Gdx.gl.glDeleteFramebuffer(framebufferHandle);
	}

}
