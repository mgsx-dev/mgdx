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
}
