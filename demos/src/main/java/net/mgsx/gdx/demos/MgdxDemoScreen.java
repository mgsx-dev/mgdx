package net.mgsx.gdx.demos;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MgdxDemoScreen extends Game implements Screen
{
	private Skin skin;
	private Stage stage;
	
	public MgdxDemoScreen() {
		stage = new Stage(new ScreenViewport());
		skin = new Skin(Gdx.files.internal("skins/composer-skin.json"));
		new MgdxSketchSelector(this, skin).show(stage);
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
		super.resize(width, height);
	}

	@Override
	public void create() {
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(Color.CLEAR, true);
		
		super.render();
		
		stage.getViewport().apply();
		stage.act();
		stage.draw();
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}
}
