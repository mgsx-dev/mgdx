package net.mgsx.gdx.demos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import net.mgsx.gdx.MgdxGame;
import net.mgsx.gdx.MgdxGame.Settings.GLMode;

public class MgdxDemo extends MgdxGame
{
	private Stage stage;
	private Skin skin;
	private GLProfiler profiler;
	
	public MgdxDemo() {
		settings.glMode = GLMode.GLMAX;
		settings.glMajor = 4;
		settings.glMinor = 5;
		settings.icons = new String[]{"libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png"};
	}
	
	@Override
	public void create() {
		profiler = new GLProfiler(Gdx.graphics);
		profiler.enable();
		profiler.setListener(GLErrorListener.THROWING_LISTENER);
		stage = new Stage(new ScreenViewport());
		skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
		new MgdxSketchSelector(this, skin).show(stage);
		Gdx.input.setInputProcessor(stage);
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
		super.resize(width, height);
	}
	
	@Override
	public void render() {
		ScreenUtils.clear(Color.CLEAR, true);
		
		super.render();
		
		stage.getViewport().apply();
		stage.act();
		stage.draw();
	}
	

}
