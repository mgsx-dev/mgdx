package net.mgsx.gltfx.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.mgsx.gltfx.ShaderProgramUtils;

public class BlurShader extends ShaderProgram {
	
	public BlurShader(boolean alphaClip) {
		this(alphaClip, true);
	}
	public BlurShader(boolean alphaClip, boolean colorClip) {
		super(
				Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(),
				createPrfix(alphaClip, colorClip) + Gdx.files.classpath("shaders/blur.fs.glsl").readString());
		ShaderProgramUtils.check(this);
	}

	private static String createPrfix(boolean alphaClip, boolean colorClip) {
		String options = "";
		if(alphaClip) options += "#define ALPHA_CLIP\n";
		if(!colorClip){
			options += "#define NO_CLIP\n";
		}
		return options;
	}
	
	public void setDirection(float x, float y){
		setUniformf("u_dir", x, y);
	}
	public void setFade(float value){
		setUniformf("u_fade", value);
	}
	
}
