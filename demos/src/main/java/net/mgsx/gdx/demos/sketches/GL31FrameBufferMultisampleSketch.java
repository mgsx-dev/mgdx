package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.Mgdx;

public class GL31FrameBufferMultisampleSketch extends ScreenAdapter
{
	private static class FrameBufferMS implements Disposable
	{
		// public Texture colorTexture;
		public int framebufferHandle;
		public int width, height;
		private int colorBufferHandle;
		
		public FrameBufferMS(Format format, int width, int height, int samples) {
			this.width = width;
			this.height = height;
			
			// create render buffer
			colorBufferHandle = Gdx.gl.glGenRenderbuffer();
			Gdx.gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, colorBufferHandle);
			Mgdx.gl31.glRenderbufferStorageMultisample(GL20.GL_RENDERBUFFER, samples, GL30.GL_RGBA8, width, height);
			
			// create frame buffer
			framebufferHandle = Gdx.gl.glGenFramebuffer();
			Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
			
			// attach render buffer
			Gdx.gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_RENDERBUFFER, colorBufferHandle);

			int result = Gdx.gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);

			Gdx.gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
			Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
			Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);
			
			int err = Gdx.gl.glGetError();
			System.out.println(err);
			
			if (result != GL20.GL_FRAMEBUFFER_COMPLETE) {
				throw new GdxRuntimeException("error");
			}
		}
		
		public void begin () {
			bind();
			setFrameBufferViewport();
		}
		protected void setFrameBufferViewport () {
			Gdx.gl20.glViewport(0, 0, width, height);
		}

		public void end () {
			end(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		}

		public void end (int x, int y, int width, int height) {
			unbind();
			Gdx.gl20.glViewport(x, y, width, height);
		}
		public void bind () {
			Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
		}
		public static void unbind () {
			Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);
		}
		public int getHeight () {
			return height;
		}

		public int getWidth () {
			return width;
		}
		public int getFramebufferHandle() {
			return framebufferHandle;
		}

		@Override
		public void dispose() {
			// TODO dispose all
			
		}
	}
	
	private FrameBuffer fbo;
	private FrameBufferMS fboMS;
	private SpriteBatch batch;
	private ShapeRenderer shapes;

	public GL31FrameBufferMultisampleSketch() {
		fboMS = new FrameBufferMS(Format.RGBA8888, 64, 64, 4);
		fbo = new FrameBuffer(Format.RGBA8888, 64, 64, false);
		fbo.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		batch = new SpriteBatch();
		shapes = new ShapeRenderer();
	}
	
	@Override
	public void render(float delta) {
		
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);

		// render a line into the non multisample FBO and display it
		fbo.begin();
		ScreenUtils.clear(Color.CLEAR);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		shapes.begin(ShapeType.Line);
		shapes.line(0, 0, 1, .3f);
		shapes.end();
		fbo.end();
		
		batch.begin();
		batch.draw(fbo.getColorBufferTexture(), 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();

		// render a line into the multisample FBO, blit to the other FBO and display it
		fboMS.begin();
		ScreenUtils.clear(Color.CLEAR);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		shapes.begin(ShapeType.Line);
		shapes.line(0, 0, 1, .3f);
		shapes.end();
		fboMS.end();
		
		Gdx.gl.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fboMS.getFramebufferHandle());
		Gdx.gl.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fbo.getFramebufferHandle());
		Gdx.gl30.glBlitFramebuffer(
			0, 0, fboMS.getWidth(), fboMS.getHeight(), 
			0, 0, fbo.getWidth(), fbo.getHeight(), 
			GL20.GL_COLOR_BUFFER_BIT, GL20.GL_NEAREST);
		Gdx.gl.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
		Gdx.gl.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		
		batch.begin();
		batch.draw(fbo.getColorBufferTexture(), 1, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
		
	}
}
