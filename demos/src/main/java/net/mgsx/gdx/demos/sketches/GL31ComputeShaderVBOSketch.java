package net.mgsx.gdx.demos.sketches;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.ComputeShader;
import net.mgsx.gdx.graphics.GL31;
import net.mgsx.gdx.utils.ShaderProgramUtils;

public class GL31ComputeShaderVBOSketch extends ScreenAdapter
{
	private ComputeShader computeShader;
	private float time;
	private int vaoHandle;
	private int vboHandle;
	private ShaderProgram renderShader;
	private int numVertices = 2;
	private Matrix4 transform = new Matrix4();
	private int ssbo;
	
	int floatsPerVertex = 8;
	int bytesPerVertex = 4 * floatsPerVertex;
	int verticesSizeBytes = numVertices * bytesPerVertex;


	public GL31ComputeShaderVBOSketch() {
		
		computeShader = new ComputeShader(Gdx.files.classpath("shaders/vertex-transform.cs.glsl").readString());
		ShaderProgramUtils.check(computeShader);
		
		renderShader = new ShaderProgram(Gdx.files.classpath("shaders/solid-batch.vs.glsl"), Gdx.files.classpath("shaders/solid-batch.fs.glsl"));
		ShaderProgramUtils.check(renderShader);
		
		createMesh();
		createSSBO();

	}
	
	private void createMesh(){
		
		IntBuffer buf = BufferUtils.newIntBuffer(1);
		Gdx.gl30.glGenVertexArrays(1, buf);
		vaoHandle = buf.get();
		
		int bytesPerVertex = 4 * floatsPerVertex;
		
		Gdx.gl30.glBindVertexArray(vaoHandle);
	
		vboHandle = Gdx.gl20.glGenBuffer();
		
		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vboHandle);
		Gdx.gl20.glBufferData(GL31.GL_ARRAY_BUFFER, verticesSizeBytes, null, GL30.GL_STATIC_DRAW);
		
		int a_position = Gdx.gl.glGetAttribLocation(renderShader.getHandle(), "a_position");
		int a_color = Gdx.gl.glGetAttribLocation(renderShader.getHandle(), "a_color");
		Gdx.gl.glEnableVertexAttribArray(a_position);
		Gdx.gl.glEnableVertexAttribArray(a_color);
		Gdx.gl.glVertexAttribPointer(a_position, 4, GL20.GL_FLOAT, false, bytesPerVertex, 0);
		Gdx.gl.glVertexAttribPointer(a_color, 4, GL20.GL_FLOAT, false, bytesPerVertex, 4 * 4);
		
		Gdx.gl30.glBindVertexArray(0);
	}

	private void createSSBO(){

		int floatsPerVertex = 4;
		int nbData = floatsPerVertex * numVertices * 4;
		ByteBuffer buffer = BufferUtils.newByteBuffer(nbData);
		FloatBuffer floats = buffer.asFloatBuffer();
		floats.put(new float[]{-1,-1,0,1});
		floats.put(new float[]{1,1,0,1});
		floats.flip();
		
		ssbo = Gdx.gl30.glGenBuffer();
		Gdx.gl30.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, ssbo);
		Gdx.gl30.glBufferData(GL31.GL_SHADER_STORAGE_BUFFER, buffer.limit(), buffer, GL30.GL_STATIC_DRAW);
		
		
		Gdx.gl30.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, vboHandle);

		Gdx.gl30.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, 0);
	}
	
	@Override
	public void render(float delta) {
		time += delta;
		
		computeShader.bind();
		computeShader.setUniformf("u_time", time);
		
		Mgdx.gl31.glBindBufferBase(GL31.GL_SHADER_STORAGE_BUFFER, 0, ssbo);
		Mgdx.gl31.glBindBufferBase(GL31.GL_SHADER_STORAGE_BUFFER, 1, vboHandle);

		
		Mgdx.gl31.glDispatchCompute(numVertices / 1, 1, 1);
		
		Mgdx.gl31.glBindBufferBase(GL31.GL_SHADER_STORAGE_BUFFER, 0, 0);
		Mgdx.gl31.glBindBufferBase(GL31.GL_SHADER_STORAGE_BUFFER, 1, 0);

		Gdx.gl.glUseProgram(0);
		
		// Some other rendering command could be done here
		
		Mgdx.gl31.glMemoryBarrier(GL31.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
		
		// render
		renderShader.bind();
		transform.setToOrtho2D(-1, -1, 2, 2);
		renderShader.setUniformMatrix("u_projTrans", transform);
		
		Gdx.gl30.glBindVertexArray(vaoHandle);
		
		Gdx.gl20.glDrawArrays(GL20.GL_LINES, 0, 2);
		
		Gdx.gl30.glBindVertexArray(0);
		
	}
	
	@Override
	public void dispose() {
		computeShader.dispose();
	}
}
