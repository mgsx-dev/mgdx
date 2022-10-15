package net.mgsx.gdx.demos.sketches;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.ComputeShader;
import net.mgsx.gdx.graphics.GL31;
import net.mgsx.gltfx.ShaderProgramUtils;

public class GL31ComputeShaderSSBOSketch extends ScreenAdapter
{
	private ComputeShader computeShader;
	private float time;
	private int ssbo;
	private FloatBuffer readBack;
	private int nbVertices = 4;
	private int nbFloatPerVertices = 3;

	public GL31ComputeShaderSSBOSketch() {
		
		computeShader = new ComputeShader(Gdx.files.classpath("shaders/demo-ssbo.cs.glsl").readString());
		ShaderProgramUtils.check(computeShader);
		
		createSSBO();
	}
	
	private void createSSBO(){
		int nbFloats = nbVertices * nbFloatPerVertices;
		int nbData = nbFloats * 4;
		ByteBuffer buffer = BufferUtils.newByteBuffer(nbData);
		FloatBuffer floats = buffer.asFloatBuffer();
		for(int i=0 ; i<nbFloats ; i++) floats.put(i);
		floats.flip();
		
		ssbo = Gdx.gl30.glGenBuffer();
		Gdx.gl30.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, ssbo);
		Gdx.gl30.glBufferData(GL31.GL_SHADER_STORAGE_BUFFER, buffer.limit(), buffer, GL30.GL_DYNAMIC_COPY);
		Gdx.gl30.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, 0);
		
		readBack = BufferUtils.newFloatBuffer(nbFloats);
	}
	
	@Override
	public void render(float delta) {
		time += delta;
		
		computeShader.bind();
		computeShader.setUniformf("u_time", time);
		
		Mgdx.gl31.glBindBufferBase(GL31.GL_SHADER_STORAGE_BUFFER, 0, ssbo);

		
		Mgdx.gl31.glDispatchCompute(nbVertices, 1, 1);
		
		Mgdx.gl31.glBindBufferBase(GL31.GL_SHADER_STORAGE_BUFFER, 0, 0);
		
		Gdx.gl.glUseProgram(0);
		
		
		Mgdx.gl31.glMemoryBarrier(GL31.GL_SHADER_STORAGE_BARRIER_BIT);
		
		readBack.clear();
		Mgdx.glMax.glGetNamedBufferSubData(ssbo, 0, readBack);
		while(readBack.hasRemaining()){
			System.out.println(readBack.get());
		}
		
	}
	
	@Override
	public void dispose() {
		computeShader.dispose();
	}
}
