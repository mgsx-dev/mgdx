package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.CustomMesh;
import com.badlogic.gdx.graphics.glutils.GPUOnlyVBOWithVAO;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.IndexData;
import com.badlogic.gdx.graphics.glutils.SSBO;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

import net.mgsx.gdx.graphics.ComputeShader;
import net.mgsx.gltfx.ShaderProgramUtils;

public class GL31ComputeShaderMeshSketch extends ScreenAdapter
{
	private ComputeShader computeShader;
	private float time;
	private ShaderProgram renderShader;
	private int numVertices = 2;
	private Matrix4 transform = new Matrix4();
	private Mesh mesh;
	private int vbo;
	private SSBO ssbo;

	public GL31ComputeShaderMeshSketch() {
		
		computeShader = new ComputeShader(Gdx.files.classpath("shaders/vertex-transform.cs.glsl").readString());
		ShaderProgramUtils.check(computeShader);
		
		renderShader = new ShaderProgram(Gdx.files.classpath("shaders/solid-batch.vs.glsl"), Gdx.files.classpath("shaders/solid-batch.fs.glsl"));
		ShaderProgramUtils.check(renderShader);
		
		createMesh();
		createSSBO();

	}
	
	private void createMesh(){
		
		GPUOnlyVBOWithVAO vdata = new GPUOnlyVBOWithVAO(true, numVertices, 
				new VertexAttributes(new VertexAttribute(Usage.Position, 4, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE)));
		vbo = vdata.getVBOHandle();
		
		IndexData idata = new IndexBufferObject(true, 0);
		
		mesh = new CustomMesh(vdata, idata, false);
	}

	private void createSSBO(){

		int floatsPerVertex = 4;
		int nbData = floatsPerVertex * numVertices * 4;
		
		ssbo = new SSBO(true, nbData);
		ssbo.setData(new float[]{
				-1,-1,0,1,
				1,1,0,1});
	}
	
	@Override
	public void render(float delta) {
		time += delta;
		
		
		computeShader.bind();
		computeShader.setUniformf("u_time", time);
		computeShader.bindBuffer(0, ssbo.getHandle());
		computeShader.bindBuffer(1, vbo);
		computeShader.dispatch1D(numVertices);

		// Some other rendering command could be done here
		
		ComputeShader.syncVertexData();
		
		// render
		renderShader.bind();
		transform.setToOrtho2D(-1, -1, 2, 2);
		renderShader.setUniformMatrix("u_projTrans", transform);
		
		mesh.render(renderShader, GL20.GL_LINES);
	}
	
	@Override
	public void dispose() {
		computeShader.dispose();
	}
}
