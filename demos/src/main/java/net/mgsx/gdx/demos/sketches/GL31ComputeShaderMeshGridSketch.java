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

public class GL31ComputeShaderMeshGridSketch extends ScreenAdapter
{
	private ComputeShader computeShader;
	private float time;
	private ShaderProgram renderShader;
	private Matrix4 transform = new Matrix4();
	private Mesh mesh;
	private int vbo;
	private SSBO ssbo;
	private final int nbX = 80, nbY = 53,
			nbVertices = nbX * nbY,
			nbLines = (nbX - 1) * nbY + nbX * (nbY - 1),
			nbIndices = nbLines * 2;

	public GL31ComputeShaderMeshGridSketch() {
		
		computeShader = new ComputeShader(Gdx.files.classpath("shaders/vertex-transform2.cs.glsl").readString());
		ShaderProgramUtils.check(computeShader);
		
		renderShader = new ShaderProgram(Gdx.files.classpath("shaders/solid-batch.vs.glsl"), Gdx.files.classpath("shaders/solid-batch.fs.glsl"));
		ShaderProgramUtils.check(renderShader);
		
		createMesh();
		createSSBO();
	}
	
	private void createMesh(){
		
		GPUOnlyVBOWithVAO vdata = new GPUOnlyVBOWithVAO(true, nbVertices, 
				new VertexAttributes(new VertexAttribute(Usage.Position, 4, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE)));
		vbo = vdata.getVBOHandle();
		
		IndexData idata = new IndexBufferObject(true, nbIndices);
		short[] indices = new short[nbIndices];
		int max = 0;
		for(int y=0, i=0 ; y<nbY ; y++){
			for(int x=0 ; x<nbX ; x++){
				if(x < nbX-1){
					indices[i++] = (short)((y * nbX) + x);
					indices[i++] = (short)((y * nbX) + x + 1);
				}
				if(y < nbY-1){
					indices[i++] = (short)((y * nbX) + x);
					indices[i++] = (short)(((y+1) * nbX) + x);
				}
				max = i;
			}
		}
		idata.setIndices(indices, 0, max);
		
		mesh = new CustomMesh(vdata, idata, false);
	}

	private void createSSBO(){

		int floatsPerVertex = 4;
		int nbFloats = floatsPerVertex * nbVertices;
		int nbBytes = nbFloats * 4;
		
		ssbo = new SSBO(true, nbBytes);
		
		float [] vertices = new float[nbFloats];
		for(int y=0, i=0 ; y<nbY ; y++){
			for(int x=0 ; x<nbX ; x++){
				vertices[i++] = (float)x / (float)(nbX - 1);
				vertices[i++] = (float)y / (float)(nbY - 1);
				vertices[i++] = 0;
				vertices[i++] = 1;
			}
		}
		
		ssbo.setData(vertices);
	}
	
	@Override
	public void render(float delta) {
		time += delta;
		
		
		computeShader.bind();
		computeShader.setUniformf("u_time", time);
		computeShader.setUniformf("u_amplitude", 2f / Math.max(nbX, nbY));
		computeShader.bindBuffer(0, ssbo.getHandle());
		computeShader.bindBuffer(1, vbo);
		computeShader.dispatch1D(nbVertices);

		// Some other rendering command could be done here
		
		ComputeShader.syncVertexData();
		
		// render
		renderShader.bind();
		transform.setToOrtho2D(-.5f, -.5f, 2, 2);
		renderShader.setUniformMatrix("u_projTrans", transform);
		
		mesh.render(renderShader, GL20.GL_LINES);
	}
	
	@Override
	public void dispose() {
		computeShader.dispose();
	}
}
