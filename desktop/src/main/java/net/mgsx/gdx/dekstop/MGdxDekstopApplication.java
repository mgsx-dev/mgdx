package net.mgsx.gdx.dekstop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3GL31;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3GL32;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3GLMax;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.MgdxLwjgl3Application;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.MgdxGame;

public class MGdxDekstopApplication {

	public MGdxDekstopApplication(MgdxGame game) 
	{
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.disableAudio(!game.settings.audio);
		config.setTitle(game.settings.title);
		config.useVsync(game.settings.useVSync);
		config.setIdleFPS(game.settings.fps);
		config.setForegroundFPS(game.settings.fps);
		config.setBackBufferConfig(8, 8, 8, 8, game.settings.depth, game.settings.stencil, game.settings.samples);
		
		DisplayMode display = Lwjgl3ApplicationConfiguration.getDisplayMode();
		game.hdpiDetected = display.height >= 1440;
		config.setHdpiMode(HdpiMode.Logical);
		
		int wndWidth = game.settings.wndWidth;
		int wndHeight = game.settings.wndHeight;
		if(game.hdpiDetected){
			wndWidth *= 2;
			wndHeight *= 2;
		}
		
		if(game.settings.fullscreen){
			// TODO find
			config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
		}else{
			config.setWindowedMode(wndWidth, wndHeight);
		}
		if(game.settings.icons != null){
			config.setWindowIcon(game.settings.icons);
		}
		switch(game.settings.glMode){
		default:
		case GL20: config.setOpenGLEmulation(GLEmulation.GL20, game.settings.glMajor, game.settings.glMinor); break;
		case GL30: config.setOpenGLEmulation(GLEmulation.GL30, game.settings.glMajor, game.settings.glMinor); break;
		case GL31: config.setOpenGLEmulation(GLEmulation.GL30, game.settings.glMajor, game.settings.glMinor); 
			Mgdx.gl31 = new Lwjgl3GL31();
			break;
		case GL32: config.setOpenGLEmulation(GLEmulation.GL30, game.settings.glMajor, game.settings.glMinor);
			Mgdx.gl31 = Mgdx.gl32 = new Lwjgl3GL32();
			break;
		case GLMAX: config.setOpenGLEmulation(GLEmulation.GL30, game.settings.glMajor, game.settings.glMinor);
			Mgdx.gl31 = Mgdx.gl32 = Mgdx.glMax = new Lwjgl3GLMax();
			break;
		}
		config.setWindowListener(new Lwjgl3WindowAdapter(){
			@Override
			public void filesDropped(String[] files) {
				if(Mgdx.inputs.fileDropListener != null){
					Array<FileHandle> fileHandles = new Array<FileHandle>();
					for(String file : files){
						fileHandles.add(Gdx.files.absolute(file));
					}
					Mgdx.inputs.fileDropListener.filesDropped(fileHandles);
				}
			}
		});
		new MgdxLwjgl3Application(game, config);
	}
}
