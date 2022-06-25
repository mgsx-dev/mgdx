package net.mgsx.gdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShaderProgramUtils {
	public static void check(ShaderProgram program){
		if(!program.isCompiled()) throw new GdxRuntimeException(program.getLog());
		String logs = program.getLog();
		if(logs.length() > 0){
			Gdx.app.error("ShaderProgramUtils", logs);
		}
	}
}
