package net.mgsx.gdx.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ComputeShader implements Disposable{
	
	private int program;
	private String log;
	private int shader;
	private boolean compiled;
	
	public ComputeShader(String source) {
		loadShader(source);
	}

	private void loadShader (String source) {
		log = "";
		
		final GL20 gl = Gdx.gl30;
		
		compiled = true;
		
		program = gl.glCreateProgram();
		if(program == 0) throw new GdxRuntimeException("unable to generate a shader program");
		
		
		IntBuffer intbuf = BufferUtils.newIntBuffer(1);

		shader = gl.glCreateShader(GL31.GL_COMPUTE_SHADER);
		if (shader == 0) throw new GdxRuntimeException("error");
		
		gl.glShaderSource(shader, source);
		gl.glCompileShader(shader);
		gl.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, intbuf);

		if (intbuf.get(0) == GL20.GL_NO_ERROR) {
			log += "Compute shader compilation failed:\n";
			log += gl.glGetShaderInfoLog(shader) + "\n";
			compiled = false;
		}
		
		// LINK
		if(compiled){
			
			gl.glAttachShader(program, shader);
			gl.glLinkProgram(program);
			
			ByteBuffer tmp = ByteBuffer.allocateDirect(4);
			tmp.order(ByteOrder.nativeOrder());
			intbuf = tmp.asIntBuffer();
			
			gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, intbuf);
			int linked = intbuf.get(0);
			if (linked == GL20.GL_NO_ERROR) {
				log += "Compute shader linkage failed:\n";
				log += Gdx.gl20.glGetProgramInfoLog(program) + "\n";
				compiled = false;
			}
		}
	}
	
	public boolean isCompiled() {
		return compiled;
	}
	
	public boolean hasLogs() {
		return log != null && log.length() > 0;
	}
	
	public String getLog() {
		return log;
	}
	
	public void bind() {
		Gdx.gl.glUseProgram(program);
	}
	
	public void unbind() {
		Gdx.gl.glUseProgram(0); // TODO should be static
	}
	
	@Override
	public void dispose() {
		Gdx.gl.glUseProgram(0);
		Gdx.gl.glDeleteShader(shader);
		Gdx.gl.glDeleteProgram(program);
	}

	public int getProgram() {
		return program;
	}

	public void setUniformf(String name, Color value) {
		int loc = Gdx.gl.glGetUniformLocation(program, name);
		Gdx.gl.glUniform4f(loc, value.r, value.g, value.b, value.a);
	}
	public void setUniformf(String name, Vector3 value) {
		int loc = Gdx.gl.glGetUniformLocation(program, name);
		Gdx.gl.glUniform3f(loc, value.x, value.y, value.z);
	}
	public void setUniformf(String name, float value) {
		int loc = Gdx.gl.glGetUniformLocation(program, name);
		Gdx.gl.glUniform1f(loc, value);
	}


}
