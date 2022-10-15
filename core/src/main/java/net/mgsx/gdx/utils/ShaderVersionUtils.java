package net.mgsx.gdx.utils;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderStage;

public class ShaderVersionUtils {
	public enum GLSLVersion {
		GL20("120", "100 es"),
		GL30("150", "300 es"),
		GL31("330", "310 es"),
		GL32("420", "320 es"), 
		OpenGL330("330", null)
		;
		public final String openGL;
		public final String openGLES;
		private GLSLVersion(String openGL, String openGLES) {
			this.openGL = openGL;
			this.openGLES = openGLES;
		}
	}

	public static String getCompatibilityHeader(ShaderStage stage, GLSLVersion minVersion) {
		String code = "#version ";
		if(Gdx.app.getType() == ApplicationType.Desktop){
			code += minVersion.openGL;
		}else{
			code += minVersion.openGLES;
		}
		code += "\n";
		if(stage == ShaderStage.fragment){
			// TODO compatibility
		}
		return code + "#line 1\n";
	}
}
