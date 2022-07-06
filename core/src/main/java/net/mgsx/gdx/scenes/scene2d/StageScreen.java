package net.mgsx.gdx.scenes.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

abstract public class StageScreen extends ScreenAdapter
{
	public Stage stage;
	protected Viewport viewport;
	
	public StageScreen() 
	{
		this(new ScreenViewport());
	}
	public StageScreen(Viewport viewport) 
	{
		this.viewport = viewport;
		stage = new Stage(viewport);
	}
	
	@Override
	public void show() {
		super.show();
		Gdx.input.setInputProcessor(stage);
	}
	
	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
		super.hide();
	}
	
	@Override
	public void render(float delta) {
//		ScreenUtils.clear(Color.CLEAR);
		
		viewport.apply();
		stage.act();
		stage.draw();
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}
	
	@Override
	public void dispose() {
		stage.dispose();
	}
}