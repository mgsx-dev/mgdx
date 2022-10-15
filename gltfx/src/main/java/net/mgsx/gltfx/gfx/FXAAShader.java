package net.mgsx.gltfx.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.mgsx.gltfx.ShaderProgramUtils;

public class FXAAShader extends ShaderProgram
{
	public FXAAShader(boolean alpha) {
		super(Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(),
			(alpha ? "#define SUPPORT_ALPHA" : "") +
			Gdx.files.classpath("shaders/fxaa.fs.glsl").readString());
		ShaderProgramUtils.check(this);
	}
	
	public void setSize(int width, int height){
		setUniformf("u_viewportInverse", 1f / width, 1f / height);
	}
	public void setConfig(float reduceMin, float reduceMul, float spanMax){
		setUniformf("u_fxaaReduceMin", reduceMin);
		setUniformf("u_fxaaReduceMul", reduceMul);
		setUniformf("u_fxaaSpanMax", spanMax);
		
	}
}
