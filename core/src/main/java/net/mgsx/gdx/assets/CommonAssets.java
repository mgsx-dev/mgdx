package net.mgsx.gdx.assets;

import com.badlogic.gdx.graphics.Texture;

public class CommonAssets {
	
	/**
	 * Texture containing only one white pixel. Useful to render texture-less shader effects with sprite batch.
	 * Application is responsible to set this texture at boot time.
	 * see {@link net.mgsx.gdx.graphics.glutils.TextureUtils#createWhitePixel()}
	 */
	public static Texture whitePixel;

}
