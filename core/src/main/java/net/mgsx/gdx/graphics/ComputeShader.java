package net.mgsx.gdx.graphics;

import com.badlogic.gdx.graphics.glutils.ShaderPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.utils.Array;

public class ComputeShader extends ShaderProgram {

	public ComputeShader(String... sources) {
		super(asParts(sources));
	}

	private static Array<ShaderPart> asParts(String... sources) {
		Array<ShaderPart> parts = new Array<ShaderPart>();
		for(String source : sources){
			parts.add(new ShaderPart(ShaderStage.compute, source));
		}
		return parts;
	}
}
