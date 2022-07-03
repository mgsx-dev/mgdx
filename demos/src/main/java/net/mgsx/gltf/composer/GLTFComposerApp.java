package net.mgsx.gltf.composer;

import net.mgsx.gdx.MgdxGame;
import net.mgsx.gdx.MgdxGame.Settings.GLMode;

public class GLTFComposerApp extends MgdxGame
{
	public GLTFComposerApp() {
		settings.glMode = GLMode.GLMAX;
		settings.glMajor = 4;
		settings.glMinor = 5;
		settings.icons = new String[]{"libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png"};
	}
	
	@Override
	public void create() {
		super.create();
		setScreen(new GLTFComposer());
	}
	
}
