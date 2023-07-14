package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.CustomMesh;
import com.badlogic.gdx.graphics.glutils.GPUOnlyVBOWithVAO;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.ShaderPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.math.Matrix4;

import net.mgsx.gltfx.ShaderProgramUtils;

public class GL30TransformFeedbackMeshSketch extends ScreenAdapter
{
	private ShaderProgram renderShader;
	
	private int numVertices = 2;
	private Matrix4 transform = new Matrix4();
	
	int floatsPerVertex = 8;
	int bytesPerVertex = 4 * floatsPerVertex;
	int verticesSizeBytes = numVertices * bytesPerVertex;
	
	private ShaderProgram transformShader;
	private Mesh sourceMesh;
	private Mesh targetMesh;

	private int fbVboHandle;
	
	public GL30TransformFeedbackMeshSketch() {
		
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
		
		Mesh mesh = new Mesh(true, numVertices, 0, 
				new VertexAttribute(Usage.Position, 4, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
		mesh.setVertices(new float[]{
				0,0,0,1, 1,0,0,1,
				1,1,0,1, 1,1,0,1
			});
		
		sourceMesh = mesh;
	}
	
	private void createTargetMesh(){
		
		GPUOnlyVBOWithVAO vdata = new GPUOnlyVBOWithVAO(true, numVertices, 
				new VertexAttribute(Usage.Position, 4, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
		
		fbVboHandle = vdata.getVBOHandle();
		
		IndexBufferObject idata = new IndexBufferObject(0);
		Mesh mesh = new CustomMesh(vdata, idata, false);
		// mesh.setVertices(new float[16]);
		
		targetMesh = mesh;
//		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, fbVboHandle);
//		Gdx.gl20.glBufferData(GL31.GL_ARRAY_BUFFER, verticesSizeBytes, null, GL30.GL_STATIC_DRAW);
		
	}
	
	@Override
	public void render(float delta) {
		// capture
		transformShader.bind();
		Gdx.gl30.glBindBufferRange(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, fbVboHandle, 0, verticesSizeBytes);
		
		Gdx.gl30.glBeginTransformFeedback(GL20.GL_LINES);
		
		sourceMesh.render(transformShader, GL20.GL_LINES);
		
		Gdx.gl30.glEndTransformFeedback();
		Gdx.gl.glUseProgram(0);
		
		// render source and target
		renderShader.bind();
		transform.setToOrtho2D(-1, -1, 2, 2);
		renderShader.setUniformMatrix("u_projTrans", transform);
		
		sourceMesh.render(renderShader, GL20.GL_LINES);
		
		transform.setToOrtho2D(-1, -1, 2, 2).translate(-1, 0, 0);
		renderShader.setUniformMatrix("u_projTrans", transform);
		
		targetMesh.render(renderShader, GL20.GL_LINES);
	}
}
