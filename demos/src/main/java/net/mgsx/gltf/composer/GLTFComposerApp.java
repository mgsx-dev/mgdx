package net.mgsx.gltf.composer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;

import net.mgsx.gdx.MgdxGame;
import net.mgsx.gdx.MgdxGame.Settings.GLMode;
import net.mgsx.gdx.assets.CommonAssets;
import net.mgsx.gdx.demos.MgdxDemoScreen;
import net.mgsx.gdx.graphics.glutils.TextureUtils;

public class GLTFComposerApp extends MgdxGame
{
	private GLTFComposer composerScreen;

	public GLTFComposerApp() {
		settings.glMode = GLMode.GLMAX;
		settings.glMajor = 4;
		settings.glMinor = 5;
		settings.icons = new String[]{"libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png"};
		settings.wndWidth = 1024;
		settings.wndHeight = 768;
	}
	
	@Override
	public void create() {
		super.create();
		CommonAssets.whitePixel = TextureUtils.createWhitePixel();
		setScreen(composerScreen = new GLTFComposer(settings, hdpiDetected));
	}
	
	@Override
	public void render() {
		if(Gdx.input.isKeyJustPressed(Input.Keys.F12)){
			if(screen == composerScreen){
				setScreen(new MgdxDemoScreen());
			}else{
				setScreen(composerScreen);
			}
		}
		super.render();
	}
	@Override
	public void setScreen(Screen screen) {
		if(screen != composerScreen){
			screen.dispose();
		}
		super.setScreen(screen);
	}
	
}
