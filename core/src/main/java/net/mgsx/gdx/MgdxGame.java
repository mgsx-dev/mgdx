package net.mgsx.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;

/**
 * TODO save preferences automatically
 */
public abstract class MgdxGame extends Game
{
	public String preferencesName;
	public static class Settings {
		
		public enum GLMode {
			GL20, GL30, GL31, GL32, GLMAX
		}
		
		public boolean useVSync = true;
		public int fps = 60;
		public String title;
		public int wndWidth = 640;
		public int wndHeight = 480;
		public int fsWidth = 1920;
		public int fsHeight = 1080;
		public boolean fullscreen = false;
		public boolean audio = true;
		public GLMode glMode = GLMode.GL20;
		public int glMajor = 2;
		public int glMinor = 0;
		public String [] icons;
		public int depth = 24;
		public int stencil = 0;
		public int samples = 0;
	}
	public Settings settings = new Settings();
	
	
	private DisplayMode defaultDisplayMode;
	private int lastWndWidth;
	private int lastWndHeight;

	/**
	 * automatically set by launcher.
	 * in case of HDPI, it is recommended for a game to scale its UI by 2.
	 * window size is automatically scale by 2 as well.
	 */
	public boolean hdpiDetected;
	
	@Override
	public void create() {
		if(preferencesName != null){
			Preferences prefs = Gdx.app.getPreferences(preferencesName);
			String value = prefs.getString("settings");
			if(value != null){
				settings = new Json().fromJson(Settings.class, value);
			}
		}
	}
	
	@Override
	public void render() {
		if(Gdx.input.isKeyJustPressed(Input.Keys.F11)){
			if(Gdx.graphics.isFullscreen()){
				defaultDisplayMode = Gdx.graphics.getDisplayMode();
				Gdx.graphics.setWindowedMode(
					lastWndWidth > 0 ? lastWndWidth : settings.wndWidth, 
					lastWndHeight > 0 ? lastWndHeight : settings.wndHeight);
			}else{
				lastWndWidth = Gdx.graphics.getWidth();
				lastWndHeight = Gdx.graphics.getHeight();
				Gdx.graphics.setFullscreenMode(
					defaultDisplayMode != null ? defaultDisplayMode : Gdx.graphics.getDisplayMode());
			}
		}
		super.render();
	}
	
	public void savePreferences(){
		if(preferencesName != null){
			Preferences prefs = Gdx.app.getPreferences(preferencesName);
			prefs.putString("settings", new Json().toJson(settings));
			prefs.flush();
		}
	}
	
}
