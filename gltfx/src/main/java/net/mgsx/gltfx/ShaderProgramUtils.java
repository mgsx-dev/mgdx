package net.mgsx.gltfx;

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
