package net.mgsx.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.gdx.utils.ShaderProgramUtils;

public class BrighnessExtractShader extends ShaderProgram
{
	/**
	 * Based on human eye perception with weight normalized.
	 */
	public static final Vector3 realisticThreashold = new Vector3(0.2126f, 0.7152f, 0.0722f);
	private int u_threshold;
	
	/**
	 * @param scale default is 1.0
	 */
	public void setThresholdRealistic(float scale){
		setUniformf(u_threshold, realisticThreashold.x * scale, realisticThreashold.y * scale, realisticThreashold.z * scale);
	}
	
	public void setThreshold(float r, float g, float b){
		setUniformf(u_threshold, r, g, b);
	}
	
	public BrighnessExtractShader() {
		super(Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(), 
			"#define EXPOSURE\n" +
			Gdx.files.classpath("shaders/bloom-extract.fs.glsl").readString());
		ShaderProgramUtils.check(this);
		u_threshold = getUniformLocation("u_threshold");
	}
}
