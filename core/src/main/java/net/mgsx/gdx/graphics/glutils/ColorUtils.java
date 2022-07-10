package net.mgsx.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.Color;

public class ColorUtils {
	public static Color hdrScale(Color color, float scale){
		color.r *= scale;
		color.g *= scale;
		color.b *= scale;
		color.a *= scale;
		return color;
	}

	public static Color hdrSet(Color dst, Color src) {
		dst.r = src.r;
		dst.g = src.g;
		dst.b = src.b;
		dst.a = src.a;
		return dst;
	}
}
