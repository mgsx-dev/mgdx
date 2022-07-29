package net.mgsx.gdx.demos.sketches;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.GL31;

public class GL32StreamPixels extends ScreenAdapter
{
	private Texture texture;
	private SpriteBatch batch;
	private boolean started, complete;
	private int pbo;
	private int width, height;
	private int bufferSize;
	private Texture texture2;
	
	public GL32StreamPixels() {
		texture = new Texture(Gdx.files.classpath("libgdx128.png"));
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
	}
	
	@Override
	public void render(float delta) 
	{
		if(texture2 != null){
			batch.begin();
			batch.draw(texture2, 0, 0, 1, 1);
			batch.end();
		}else{
			batch.begin();
			batch.draw(texture, 0, 0, 1, 1);
			batch.end();
		}
		
		if(!started){
			started = true;
			
			// create PBO
			width = Gdx.graphics.getBackBufferWidth();
			height = Gdx.graphics.getBackBufferHeight();
			bufferSize = width * height * 4;
			pbo = Gdx.gl.glGenBuffer();
			Gdx.gl.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, pbo);
			Mgdx.gl32.glBufferData(GL30.GL_PIXEL_PACK_BUFFER, bufferSize, null, GL30.GL_STATIC_READ);
			
			// capture pixels
			Gdx.gl.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, pbo);
			Mgdx.gl32.glReadnPixels(0, 0, width, height, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, bufferSize, null);
			
			Gdx.gl.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, 0);
		}
		else if(!complete){
			complete = true;
			
			// map buffer to client memory
			Gdx.gl.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, pbo);
			Buffer buf = Gdx.gl30.glMapBufferRange(GL30.GL_PIXEL_PACK_BUFFER, 0, bufferSize, GL31.GL_MAP_READ_BIT);
			
			ByteBuffer pixels = (ByteBuffer)buf;
			
			Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
			pixmap.getPixels().put(pixels);
			pixmap.getPixels().flip();

			Gdx.gl30.glUnmapBuffer(GL30.GL_PIXEL_PACK_BUFFER);
			Gdx.gl.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, 0);
			
			Gdx.gl.glDeleteBuffer(pbo);
			
			texture2 = new Texture(pixmap);
			pixmap.dispose();
		}
	}
}
