package net.mgsx.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.graphics.GLFormat;

public class BlurCascadeGaussian implements Disposable
{
	private BlurShader blur;
	private Array<FrameBuffer> fbos = new Array<FrameBuffer>();
	private GLFormat format;
	private SpriteBatch batch;
	public float blurMix;
	private FrameBuffer bloomStack;

	public BlurCascadeGaussian(GLFormat format) {
		this.format = format;
		blur = new BlurShader(false, false);
		batch = new SpriteBatch();
	}
	
	public Texture render(Texture inputTexture, int requestedStages, ShaderProgram initialShader){
		ensureFBOs(inputTexture, requestedStages);
		
		inputTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		// Try with the first one
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.disableBlending();
		batch.setColor(Color.WHITE);
		batch.begin();
		{
			FrameBuffer dst = fbos.first();
			dst.begin();
			batch.setShader(initialShader);
			batch.draw(inputTexture, 0, 0, 1, 1, 0, 0, 1, 1);
			batch.flush();
		}
		batch.setShader(blur);
		blur.bind();
		blur.setFade(1f);
		
		Texture last = fbos.first().getColorBufferTexture();
		
		float blurWidth = 2;
		
		for(int i=0 ; i < fbos.size-1 ; i+=2){
			
			{
				Texture src = fbos.get(i).getColorBufferTexture();
				FrameBuffer dst = fbos.get(i+1);
				
				float dirX = blurWidth / dst.getWidth();
				blur.setDirection(dirX, 0);
				
				dst.begin();
				batch.draw(src, 0, 0, 1, 1, 0, 0, 1, 1);
				batch.flush();
				dst.end();
			}
			
			{
				Texture src = fbos.get(i+1).getColorBufferTexture();
				FrameBuffer dst = fbos.get(i+2);
				
				float dirY = blurWidth / dst.getHeight();
				blur.setDirection(0, dirY);
				
				dst.begin();
				batch.draw(src, 0, 0, 1, 1, 0, 0, 1, 1);
				batch.flush();
				dst.end();
				
				last = dst.getColorBufferTexture();
			}
		}
		
		// TODO should be done in one draw call (binding all textures)
		
		bloomStack.begin();
		ScreenUtils.clear(Color.CLEAR);
		batch.enableBlending();
		batch.setBlendFunction(GL20.GL_CONSTANT_ALPHA, GL20.GL_ONE);
		batch.setShader(null);
		
		for(int i=2 ; i<fbos.size ; i+=2){
			
			float t = (float)(i-2) / (float)(fbos.size);


			// TODO parameter to control small/big layer ratio
			
			float mix = (1 - t) * (1 - t) * t * 4;
			
			mix = (1-t) * t * 4;
			
			
			Gdx.gl.glBlendColor(0,0,0, mix / requestedStages);
			Texture src = fbos.get(i).getColorBufferTexture();
			batch.draw(src, 0, 0, 1, 1, 0, 0, 1, 1);
			batch.flush();
		}
		bloomStack.end();
		last = bloomStack.getColorBufferTexture();
		
		batch.end();
		
		Gdx.gl.glBlendColor(0,0,0, 0);
		
		return last;
	}

	private void ensureFBOs(Texture inputTexture, int requestedStages) {
		if(fbos.size == 0){
			createFBOs(inputTexture, requestedStages);
		}
		FrameBuffer top = fbos.first();
		if(top.getWidth() != inputTexture.getWidth() || top.getHeight() != inputTexture.getHeight()){
			createFBOs(inputTexture, requestedStages);
		}
		if(fbos.size != requestedStages*2+1){
			createFBOs(inputTexture, requestedStages);
		}
	}

	private void createFBOs(Texture inputTexture, int requestedStages) {
		disposeFBOs();
		
		int width = inputTexture.getWidth();
		int height = inputTexture.getHeight();
		
		// Fisrt for capture
		{
			FrameBufferBuilder b = new FrameBufferBuilder(width, height);
			b.addColorTextureAttachment(format.internalFormat, format.format, format.type);
			FrameBuffer fbo = b.build();
			fbo.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
			fbos.add(fbo);
		}
		// last compose
		{
			FrameBufferBuilder b = new FrameBufferBuilder(width, height);
			b.addColorTextureAttachment(format.internalFormat, format.format, format.type);
			FrameBuffer fbo = b.build();
			fbo.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
			bloomStack = fbo;
		}
		
		for(int i=0 ; i<requestedStages ; i++){
			for(int j=0 ; j<2 ; j++){
				FrameBufferBuilder b = new FrameBufferBuilder(width, height);
				b.addColorTextureAttachment(format.internalFormat, format.format, format.type);
				FrameBuffer fbo = b.build();
				fbo.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
				fbos.add(fbo);
			}
			width /= 2;
			height /= 2;
			if(width < 1 || height < 1) break;
		}
		
	}
	private void disposeFBOs() {
		for(FrameBuffer fbo : fbos){
			fbo.dispose();
		}
		fbos.clear();
	}

	@Override
	public void dispose() {
		disposeFBOs();
		blur.dispose();
	}

}
