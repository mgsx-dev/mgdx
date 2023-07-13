package net.mgsx.gdx.graphics;

import com.badlogic.gdx.graphics.glutils.ShaderPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.Mgdx;

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
	
	public void bindBuffer(int index, int bufferHandle){
		Mgdx.gl31.glBindBufferBase(GL31.GL_SHADER_STORAGE_BUFFER, index, bufferHandle);
	}
	
	public void dispatch3D(int nx, int ny, int nz){
		Mgdx.gl31.glDispatchCompute(nx, ny, nz);
	}
	public void dispatch2D(int nx, int ny){
		Mgdx.gl31.glDispatchCompute(nx, ny, 1);
	}
	public void dispatch1D(int nx){
		Mgdx.gl31.glDispatchCompute(nx, 1, 1);
	}
	
	/**
	 * Makes sure all vertex data written by any compute shader are ready to use.
	 */
	public static void syncVertexData(){
		Mgdx.gl31.glMemoryBarrier(GL31.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
	}
}
