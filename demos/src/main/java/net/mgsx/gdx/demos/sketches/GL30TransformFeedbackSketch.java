package net.mgsx.gdx.demos.sketches;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.ShaderPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;

import net.mgsx.gdx.graphics.GL31;
import net.mgsx.gdx.utils.ShaderProgramUtils;

public class GL30TransformFeedbackSketch extends ScreenAdapter
{
	private int vaoHandle;
	private int vboHandle;
	private ShaderProgram renderShader;
	
	private int numVertices = 2;
	private Matrix4 transform = new Matrix4();
	
	int floatsPerVertex = 8;
	int bytesPerVertex = 4 * floatsPerVertex;
	int verticesSizeBytes = numVertices * bytesPerVertex;
	
	private ShaderProgram transformShader;
	private int fbVaoHandle;
	private int fbVboHandle;
	
	public GL30TransformFeedbackSketch() {
		
		transformShader = new ShaderProgram(new ShaderPart(ShaderStage.vertex, Gdx.files.classpath("shaders/transform-feedback.vert").readString()));
		ShaderProgramUtils.check(transformShader);
		
		
		transformShader.setTransformFeedback(true, "gl_Position", "v_color");
		ShaderProgramUtils.check(transformShader);
		
		
		renderShader = new ShaderProgram(Gdx.files.classpath("shaders/solid-batch.vs.glsl"), Gdx.files.classpath("shaders/solid-batch.fs.glsl"));
		ShaderProgramUtils.check(renderShader);
		
		createSourceMesh();
		createTargetMesh();
		
		
	}
	
	private void createSourceMesh(){
		
		IntBuffer buf = BufferUtils.newIntBuffer(1);
		Gdx.gl30.glGenVertexArrays(1, buf);
		vaoHandle = buf.get();
		
		ByteBuffer verticesBytes = BufferUtils.newByteBuffer(verticesSizeBytes);
		FloatBuffer verticesFloats = verticesBytes.asFloatBuffer();
		verticesFloats.put(new float[]{
			0,0,0,1, 1,0,0,1,
			1,1,0,1, 1,1,0,1
		});
		verticesFloats.flip();
		
		Gdx.gl30.glBindVertexArray(vaoHandle);
	
		vboHandle = Gdx.gl20.glGenBuffer();
		
		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vboHandle);
		Gdx.gl20.glBufferData(GL31.GL_ARRAY_BUFFER, verticesSizeBytes, verticesBytes, GL30.GL_STATIC_DRAW);
		
		int a_position = Gdx.gl.glGetAttribLocation(transformShader.getHandle(), "a_position");
		int a_color = Gdx.gl.glGetAttribLocation(transformShader.getHandle(), "a_color");
		Gdx.gl.glEnableVertexAttribArray(a_position);
		Gdx.gl.glEnableVertexAttribArray(a_color);
		Gdx.gl.glVertexAttribPointer(a_position, 4, GL20.GL_FLOAT, false, bytesPerVertex, 0);
		Gdx.gl.glVertexAttribPointer(a_color, 4, GL20.GL_FLOAT, false, bytesPerVertex, 4 * 4);
		
		Gdx.gl30.glBindVertexArray(0);
	}
	
	private void createTargetMesh(){
		
		IntBuffer buf = BufferUtils.newIntBuffer(1);
		Gdx.gl30.glGenVertexArrays(1, buf);
		fbVaoHandle = buf.get();
		
		Gdx.gl30.glBindVertexArray(fbVaoHandle);
	
		fbVboHandle = Gdx.gl20.glGenBuffer();
		
		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, fbVboHandle);
		Gdx.gl20.glBufferData(GL31.GL_ARRAY_BUFFER, verticesSizeBytes, null, GL30.GL_STATIC_DRAW);
		
		int a_position = Gdx.gl.glGetAttribLocation(renderShader.getHandle(), "a_position");
		int a_color = Gdx.gl.glGetAttribLocation(renderShader.getHandle(), "a_color");
		Gdx.gl.glEnableVertexAttribArray(a_position);
		Gdx.gl.glEnableVertexAttribArray(a_color);
		Gdx.gl.glVertexAttribPointer(a_position, 4, GL20.GL_FLOAT, false, bytesPerVertex, 0);
		Gdx.gl.glVertexAttribPointer(a_color, 4, GL20.GL_FLOAT, false, bytesPerVertex, 4 * 4);
		
		Gdx.gl30.glBindVertexArray(0);
	}
	
	@Override
	public void render(float delta) {
		// capture
		transformShader.bind();
		Gdx.gl30.glBindBufferRange(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, fbVboHandle, 0, verticesSizeBytes);
		
		Gdx.gl30.glBeginTransformFeedback(GL20.GL_LINES);
		
		Gdx.gl30.glBindVertexArray(vaoHandle);
		Gdx.gl20.glDrawArrays(GL20.GL_LINES, 0, 2);
		Gdx.gl30.glBindVertexArray(0);
		
		Gdx.gl30.glEndTransformFeedback();
		Gdx.gl.glUseProgram(0);
		
		// render source and target
		renderShader.bind();
		transform.setToOrtho2D(-1, -1, 2, 2);
		renderShader.setUniformMatrix("u_projTrans", transform);
		
		Gdx.gl30.glBindVertexArray(vaoHandle);
		Gdx.gl20.glDrawArrays(GL20.GL_LINES, 0, 2);
		Gdx.gl30.glBindVertexArray(0);
		
		transform.setToOrtho2D(-1, -1, 2, 2).translate(-1, 0, 0);
		renderShader.setUniformMatrix("u_projTrans", transform);
		
		Gdx.gl30.glBindVertexArray(fbVaoHandle);
		Gdx.gl20.glDrawArrays(GL20.GL_LINES, 0, 2);
		Gdx.gl30.glBindVertexArray(0);
	}
}
