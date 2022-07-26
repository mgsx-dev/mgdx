package net.mgsx.gdx.demos.sketches;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

import net.mgsx.gdx.graphics.Texture3D;
import net.mgsx.gdx.graphics.glutils.CustomTexture3DData;
import net.mgsx.gdx.utils.ShaderProgramUtils;

public class GL30Texture3DSketch extends ScreenAdapter
{
	private Texture3D texture;
	private float time;
	private ShaderProgram renderShader;
	private Matrix4 transform = new Matrix4();
	private Mesh mesh;

	public GL30Texture3DSketch() {
		int size = 8;
		int w = size, h = size, d = size;
		CustomTexture3DData data = new CustomTexture3DData(w, h, d, 0, GL30.GL_RGBA, GL30.GL_RGBA8, GL30.GL_UNSIGNED_BYTE);
		IntBuffer buffer = data.getPixels().asIntBuffer();
		Color c = new Color(Color.BLACK);
		for(int z=0 ; z<d ; z++){
			for(int y=0 ; y<h ; y++){
				for(int x=0 ; x<w ; x++){
					buffer.put(c.set(x/(float)(w-1), y/(float)(h-1), z/(float)(d-1), 1).toIntBits());
				}
			}
		}
		buffer.flip();
		
		texture = new Texture3D(data);
		
		mesh = new Mesh(true, 4, 6, VertexAttribute.Position());
		mesh.setIndices(new short[]{
				2,1,0, 
				1,2,3});
		mesh.setVertices(new float[]{
				0,0,0,
				1,0,0,
				0,1,0,
				1,1,0
		});
		
		renderShader = new ShaderProgram(
			Gdx.files.classpath("shaders/position.vert"), 
			Gdx.files.classpath("shaders/texture3d.frag"));
		ShaderProgramUtils.check(renderShader);

	}
	
	@Override
	public void render(float delta) {
		time += delta;
		float move = Math.abs(time % 2f - 1);
		
		renderShader.bind();
		renderShader.setUniformMatrix("u_projTrans", transform.setToOrtho2D(0, 0, 1, 1).translate(0, 0, -move));
		renderShader.setUniformi("u_texture", 0);
		texture.bind();
		mesh.render(renderShader, GL20.GL_TRIANGLES);
	}
	
	@Override
	public void dispose() {
		texture.dispose();
	}
}
