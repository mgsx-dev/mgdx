package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.GL32;

public class GL32BlendingAdvancedSketch extends ScreenAdapter
{
	private Texture texture;
	private SpriteBatch batch;

	public GL32BlendingAdvancedSketch() {
		// https://www.khronos.org/registry/OpenGL-Refpages/es3/html/glBlendEquation.xhtml
		texture = new Texture(Gdx.files.classpath("libgdx128.png"));
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
	}
	
	@Override
	public void render(float delta) {
		ScreenUtils.clear(Color.CLEAR);
		batch.begin();
		
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.draw(texture, 0, 0, 1, 1);
		
		batch.flush();
		
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glBlendEquation(GL32.GL_HSL_HUE);
		batch.draw(texture, 0, 0, .5f, .5f);
	
		batch.end();
		
		Gdx.gl.glBlendEquation(GL20.GL_FUNC_ADD);
		
		Mgdx.gl32.glBlendBarrier();
	}
}
