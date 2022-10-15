package net.mgsx.gltf.composer.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.IntArray;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.glutils.FlexFrameBuffer;
import net.mgsx.gltfx.mrt.PBRRenderTargets;

public class PBRRenderTargetsMultisample extends PBRRenderTargets
{
	private FlexFrameBuffer fboMS;
	private int samples;
	private IntArray renderBufferHandles = new IntArray();
	
	public PBRRenderTargetsMultisample(int samples) {
		super();
		this.samples = samples;
	}

	@Override
	public FrameBuffer getFrameBuffer() {
		return fbo;
	}
	
	@Override
	public void begin() {
		if(samples > 1){
			fboMS.begin(fbo.getWidth(), fbo.getHeight());
		}else{
			super.begin();
		}
	}
	
	@Override
	public void end() {
		if(samples > 1){
			fboMS.end();
			// blit to non MS FBO
			Gdx.gl.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fboMS.getFramebufferHandle());
			Gdx.gl.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fbo.getFramebufferHandle());
			Gdx.gl30.glBlitFramebuffer(
					0, 0, fbo.getWidth(), fbo.getHeight(), 
					0, 0, fbo.getWidth(), fbo.getHeight(), 
					GL20.GL_COLOR_BUFFER_BIT, GL20.GL_NEAREST);
			Gdx.gl.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
			Gdx.gl.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		}else{
			super.end();
		}
	}
	
	@Override
	public void dispose() {
		disposeFboMS();
		super.dispose();
	}
	
	private void disposeFboMS(){
		if(fboMS != null){
			fboMS.dispose();
			fboMS = null;
		}
		for(int i=0 ; i<renderBufferHandles.size ; i++){
			Gdx.gl.glDeleteRenderbuffer(renderBufferHandles.get(i));
		}
		renderBufferHandles.clear();
	}
	
	@Override
	public boolean ensureSize(int width, int height) {
		boolean changed = super.ensureSize(width, height);
		if(fboMS == null && samples > 1){
			buildFboMS(width, height);
		}
		return changed;
	}
	
	private void buildFboMS(int width, int height){
		if(fboMS != null) fboMS.dispose();
		
		fboMS = new FlexFrameBuffer();
		fboMS.bind();
		for(int index=0 ; index<layers.size ; index++){
			Layer layer = layers.get(index);
			
			// create color buffer
			int colorBufferHandle = Gdx.gl.glGenRenderbuffer();
			renderBufferHandles.add(colorBufferHandle);
			Gdx.gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, colorBufferHandle);
			Mgdx.gl31.glRenderbufferStorageMultisample(GL20.GL_RENDERBUFFER, samples, layer.format.internalFormat, width, height);
			
			// attach render buffer
			Gdx.gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0 + index, GL20.GL_RENDERBUFFER, colorBufferHandle);
		}
		
		if(depthFormat != null){
			// create depth buffer
			int depthbufferHandle = Gdx.gl.glGenRenderbuffer();
			renderBufferHandles.add(depthbufferHandle);
			Gdx.gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, depthbufferHandle);
			Mgdx.gl31.glRenderbufferStorageMultisample(GL20.GL_RENDERBUFFER, samples, depthFormat.internalFormat, width, height);
			
			// attach depth buffer
			Gdx.gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_RENDERBUFFER, depthbufferHandle);
		}
		
		fboMS.compile();
		
		Gdx.gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
		Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
		Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);
	}
	
	@Override
	protected FrameBuffer buildFBO(int width, int height) {
		if(samples > 1){
			// create MS
			buildFboMS(width, height);
		}
		return super.buildFBO(width, height);
	}

	public void setSamples(int samples) {
		if(samples <= 1) samples = 0;
		if(samples != this.samples){
			this.samples = samples;
			disposeFboMS();
		}
	}
}
