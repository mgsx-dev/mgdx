package net.mgsx.gltf.composer.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;

public class ComposerCode {

	public static void toClipboard(DirectionalLightEx light) {
		String content = "";
		content += "DirectionalLightEx sunLight = new DirectionalLightEx();\n";
		content += set("sunLight.baseColor", light.baseColor);
		content += set("sunLight.direction", light.direction);
		content += set("sunLight.intensity", light.intensity);
		Gdx.app.getClipboard().setContents(content);
	}

	private static String set(String expression, Color color) {
		return expression + call("set", f(color.r), f(color.g), f(color.b), f(color.a));
	}
	private static String set(String expression, Vector3 point) {
		return expression + call("set", f(point.x), f(point.y), f(point.z));
	}
	
	private static String set(String expression, float value) {
		return expression + " = " + f(value) + ";\n";
	}
	
	private static String f(float value){
		return value + "f";
	}
	private static String args(String...args){
		String line = "";
		for(int i=0 ; i<args.length ; i++){
			if(i > 0) line = line + ", ";
			line += args[i];
		}
		return line;
	}
	private static String call(String method, String...args){
		return "." + method + "(" + args(args) + ");\n";
	}
	

}
