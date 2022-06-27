package net.mgsx.gdx.demos.sketches;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.GL31;
import net.mgsx.gdx.utils.GLUtils;

public class GL31ProgramPipelineSketch extends ScreenAdapter
{
	private int ppHandle;
	private int vaoHandle;
	private int vertProgram;
	private int fragProgram;
	private Matrix4 transform = new Matrix4();
	private float time;
	private int vboHandle;

	public GL31ProgramPipelineSketch() {
		createShaders();
		createMesh();
	}
	
	private void createShaders(){
		vertProgram = Mgdx.gl31.glCreateShaderProgramv(GL20.GL_VERTEX_SHADER, new String[]{Gdx.files.classpath("shaders/solid-batch.vs.glsl").readString()});
		fragProgram = Mgdx.gl31.glCreateShaderProgramv(GL20.GL_FRAGMENT_SHADER, new String[]{Gdx.files.classpath("shaders/solid-batch.fs.glsl").readString()});
		
		ppHandle = GLUtils.glGenProgramPipeline();
		Mgdx.gl31.glUseProgramStages(ppHandle, GL31.GL_VERTEX_SHADER_BIT, vertProgram);
		Mgdx.gl31.glUseProgramStages(ppHandle, GL31.GL_FRAGMENT_SHADER_BIT, fragProgram);
		
		Mgdx.gl31.glValidateProgramPipeline(ppHandle);
		
		IntBuffer tmp = BufferUtils.newIntBuffer(16);
		Mgdx.gl31.glGetProgramPipelineiv(ppHandle, GL31.GL_VALIDATE_STATUS, tmp);
		int valid = tmp.get();
		String log = Mgdx.gl31.glGetProgramPipelineInfoLog(ppHandle);
		if(valid != GL20.GL_TRUE){
			throw new GdxRuntimeException(log);
		}else if(log.length() > 0){
			System.err.println(log);
		}
		
	}
	
	private void createMesh(){
		
		IntBuffer buf = BufferUtils.newIntBuffer(1);
		Gdx.gl30.glGenVertexArrays(1, buf);
		vaoHandle = buf.get();
		
		int numVertices = 2;
		int floatsPerVertex = 7;
		int bytesPerVertex = 4 * floatsPerVertex;
		int verticesSizeBytes = numVertices * bytesPerVertex;
		
		Gdx.gl30.glBindVertexArray(vaoHandle);
	
		ByteBuffer verticesAsBytes = BufferUtils.newByteBuffer(verticesSizeBytes);
		FloatBuffer verticesAsFloats = verticesAsBytes.asFloatBuffer();
		verticesAsFloats.put(new float[]{0,0,0, 1,0,0,1});
		verticesAsFloats.put(new float[]{1,1,0, 1,1,0,1});
		
		vboHandle = Gdx.gl20.glGenBuffer();
		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vboHandle);
		Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, verticesAsBytes.limit(), verticesAsBytes, GL20.GL_STATIC_DRAW);
		
		
		int a_position = Gdx.gl.glGetAttribLocation(vertProgram, "a_position");
		int a_color = Gdx.gl.glGetAttribLocation(vertProgram, "a_color");
		Gdx.gl.glEnableVertexAttribArray(a_position);
		Gdx.gl.glEnableVertexAttribArray(a_color);
		Gdx.gl.glVertexAttribPointer(a_position, 3, GL20.GL_FLOAT, false, bytesPerVertex, 0);
		Gdx.gl.glVertexAttribPointer(a_color, 4, GL20.GL_FLOAT, false, bytesPerVertex, 3 * 4);
		
		Gdx.gl30.glBindVertexArray(0);
	}
	
	@Override
	public void render(float delta) {
		
		time += delta;
		
		transform.setToOrtho2D(-1, -1, 2, 2).rotate(Vector3.Z, time * 30f);
		
		Gdx.gl.glUseProgram(0);
		Mgdx.gl31.glBindProgramPipeline(ppHandle);
		
		int u_projTrans = Mgdx.gl31.glGetUniformLocation(vertProgram, "u_projTrans");
		boolean useNew = true;
		if(useNew){
			FloatBuffer buf = GLUtils.buffer16f;
			buf.put(transform.val);
			buf.flip();
			Mgdx.gl31.glProgramUniformMatrix4fv(vertProgram, u_projTrans, false, buf);
		}else{
			Mgdx.gl31.glActiveShaderProgram(ppHandle, vertProgram);
			Gdx.gl.glUniformMatrix4fv(u_projTrans, 1, false, transform.val, 0);
		}
		
		Gdx.gl30.glBindVertexArray(vaoHandle);
		
		Gdx.gl20.glDrawArrays(GL20.GL_LINES, 0, 2);
		
		Gdx.gl30.glBindVertexArray(0);
		
		Mgdx.gl31.glBindProgramPipeline(0);
	}
	
	@Override
	public void dispose() {
		IntBuffer buf = BufferUtils.newIntBuffer(1);
		buf.put(ppHandle);
		buf.flip();
		GLUtils.glDeleteProgramPipeline(ppHandle);
	
		Gdx.gl20.glDeleteProgram(vertProgram);
		Gdx.gl20.glDeleteProgram(fragProgram);
		
		Gdx.gl.glDeleteBuffer(vboHandle);
		buf.put(vaoHandle);
		buf.flip();
		Gdx.gl30.glDeleteVertexArrays(1, buf);
	}
}
