package net.mgsx.gdx.utils;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShaderProgramUtils {
	public static void check(ShaderProgram program){
		if(!program.isCompiled()) throw new GdxRuntimeException(program.getLog());
		String logs = program.getLog();
		if(logs.length() > 0){
			Gdx.app.error("ShaderProgramUtils", logs);
		}
	}
	
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

	public static ShaderProgram createPixelShader(String fragmentPath) {
		ShaderProgram s = new ShaderProgram(
			Gdx.files.internal("shaders/sprite-batch.vs.glsl"),
			Gdx.files.internal(fragmentPath));
		check(s);
		return s;
	}

	public static ShaderProgram createPixelShader(String fragmentPath, String fragmentOptions) {
		ShaderProgram s = new ShaderProgram(
			Gdx.files.internal("shaders/sprite-batch.vs.glsl").readString(),
			fragmentOptions + Gdx.files.internal(fragmentPath).readString());
		check(s);
		return s;
	}

}
