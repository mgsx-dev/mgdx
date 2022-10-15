package net.mgsx.gdx.demos.sketches;

import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gltfx.ShaderProgramUtils;

public class GL32OffsetElementsSketch extends ScreenAdapter
{
	private ShortBuffer indices;
	private Mesh mesh;
	private ShaderProgram renderShader;
	private Matrix4 transform = new Matrix4();
	private float time;
	
	public GL32OffsetElementsSketch() {
		indices = BufferUtils.newShortBuffer(6);
		indices.put(new short[]{
			0, 1, 2,
			2, 1, 3
		});
		indices.flip();
		
		mesh = new Mesh(true, 6, 0, VertexAttribute.Position(), VertexAttribute.ColorUnpacked());
		mesh.setVertices(new float[]{
			0, 0, 0, 1,1,1,1,
			1, 0, 0, 1,1,1,1,
			0, 1, 0, 1,1,1,1,
			1, 1, 0, 1,1,1,1,
			0, 2, 0, 1,1,1,1,
			1, 2, 0, 1,1,1,1
		});
		/*
		mesh.setIndices(new short[]{
			0, 1, 2,
			2, 1, 3,
			
			2, 3, 4,
			4, 3, 5
		});
		*/
		
		renderShader = new ShaderProgram(Gdx.files.classpath("shaders/solid-batch.vs.glsl"), Gdx.files.classpath("shaders/solid-batch.fs.glsl"));
		ShaderProgramUtils.check(renderShader);

	}
	
	@Override
	public void render(float delta) {
		time += delta;
		
		int baseVertex = ((int)time) % 3;
		
		ScreenUtils.clear(Color.CLEAR);
		
		renderShader.bind();
		transform.setToOrtho2D(-4, -4, 8, 8);
		renderShader.setUniformMatrix("u_projTrans", transform);
		
		mesh.bind(renderShader);
		Mgdx.gl32.glDrawElementsBaseVertex(GL20.GL_TRIANGLES, 6, GL20.GL_UNSIGNED_SHORT, indices, baseVertex);
		mesh.unbind(renderShader);
	}
}
