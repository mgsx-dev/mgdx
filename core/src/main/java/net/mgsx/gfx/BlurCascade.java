package net.mgsx.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class BlurCascade implements Disposable
{
	private Array<FrameBuffer> fbos = new Array<FrameBuffer>();
	private int maxStages;
	private GLFormat format;
	private SpriteBatch batch;
	
	public BlurCascade(GLFormat format, int maxStages) {
		super();
		this.format = format;
		this.maxStages = maxStages;
		batch = new SpriteBatch();
		batch.setBlendFunctionSeparate(GL20.GL_ONE, GL20.GL_ONE, GL20.GL_ZERO, GL20.GL_ZERO);
	}

	public Texture render(Texture inputTexture){
		return render(inputTexture, maxStages);
	}
	public Texture render(Texture inputTexture, int stages){
		if(stages <= 0) return inputTexture;
		stages = MathUtils.clamp(stages, 1, fbos.size);
		
		ensureFBOs(inputTexture);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.disableBlending();
		batch.setColor(Color.WHITE);
		batch.begin();
		{
			FrameBuffer dst = fbos.first();
			dst.begin();
			batch.draw(inputTexture, 0, 0, 1, 1, 0, 0, 1, 1);
			batch.flush();
		}
		
		Texture src = fbos.first().getColorBufferTexture();
		for(int i=1 ; i<stages ; i++){
			FrameBuffer dst = fbos.get(i);
			dst.begin();
			batch.draw(src, 0, 0, 1, 1, 0, 0, 1, 1);
			batch.flush();
			src = dst.getColorBufferTexture();
		}
		batch.enableBlending();
		for(int i=stages-2 ; i>=0 ; i--){
			FrameBuffer dst = fbos.get(i);
			dst.begin();
			batch.draw(src, 0, 0, 1, 1, 0, 0, 1, 1);
			batch.flush();
			src = dst.getColorBufferTexture();
		}
		
		batch.end();
		fbos.first().end();
		
		return src;
	}
	
	private void ensureFBOs(Texture inputTexture) {
		if(fbos.size == 0){
			createFBOs(inputTexture);
		}
		FrameBuffer top = fbos.first();
		if(top.getWidth() != inputTexture.getWidth() || top.getHeight() != inputTexture.getHeight()){
			createFBOs(inputTexture);
		}
	}

	private void createFBOs(Texture inputTexture) {
		disposeFBOs();
		for(int i=0, width=inputTexture.getWidth(), height=inputTexture.getHeight() ; i<maxStages && width>0 && height>0 ; i++, width/=2, height/=2){
			FrameBufferBuilder b = new FrameBufferBuilder(width, height);
			b.addColorTextureAttachment(format.internalFormat, format.format, format.type);
			fbos.add(b.build());
			fbos.peek().getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
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
	}

}
