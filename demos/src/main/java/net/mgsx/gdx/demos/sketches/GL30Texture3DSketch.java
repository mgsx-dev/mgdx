package net.mgsx.gdx.demos.sketches;

import java.nio.IntBuffer;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.mgsx.gdx.graphics.Texture3D;
import net.mgsx.gdx.graphics.glutils.CustomTexture3DData;

public class GL30Texture3DSketch extends ScreenAdapter
{
	private Texture3D texture3D;
	private Texture texture;
	private float time;
	private ShaderProgram renderShader;
	private SpriteBatch batch;
	
	static ShaderProgram createShader () {
		String vertexShader = "in vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "in vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "in vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "out vec4 v_color;\n" //
			+ "out vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n" //
			+ "in LOWP vec4 v_color;\n" //
			+ "in vec2 v_texCoords;\n" //
			+ "out vec4 fragColor;" //
			+ "uniform sampler2D u_texture;\n" //
			+ "uniform sampler3D u_texture3D;\n" //
			+ "uniform float u_time;\n" //
			+ "void main()\n"//
			+ "{\n" //
			+ "  fragColor = v_color * texture(u_texture, v_texCoords) * texture(u_texture3D, vec3(v_texCoords, u_time));\n" //
			+ "}";
		
		String prepend;
		if(Gdx.app.getType() == ApplicationType.Desktop){
			prepend = "#version 130\n";
		}else{
			prepend = "#version 300 es\n";
		}

		ShaderProgram shader = new ShaderProgram(prepend + vertexShader, prepend + fragmentShader);
		if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		System.err.println(shader.getLog());
		
		return shader;
	}

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
		
		texture3D = new Texture3D(data);
		
		renderShader = createShader();
		
		batch = new SpriteBatch(4, renderShader);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		
		Pixmap pixmap = new Pixmap(1, 1, Format.RGB888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		texture = new Texture(pixmap);
		pixmap.dispose();
	}
	
	@Override
	public void render(float delta) {
		time += delta;
		float pingPong = Math.abs(time % 2f - 1);
		
		renderShader.bind();
		renderShader.setUniformf("u_time", pingPong);
		renderShader.setUniformi("u_texture3D", 1);
		texture3D.bind(1);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		batch.begin();
		batch.draw(texture, 0, 0, 1, 1);
		batch.end();
	}
	
	@Override
	public void dispose() {
		texture.dispose();
		texture3D.dispose();
		renderShader.dispose();
		batch.dispose();
	}
}
