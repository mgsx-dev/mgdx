package com.badlogic.gdx.graphics.profiling;

import java.nio.Buffer;

import net.mgsx.gdx.graphics.GLMax;

public class GLMaxInterceptor extends GL32Interceptor implements GLMax
{
	final GLMax glMax;

	public GLMaxInterceptor(GLProfiler glProfiler, GLMax glMax) {
		super(glProfiler, glMax);
		this.glMax = glMax;
	}

	@Override
	public void glGetNamedBufferSubData(int buffer, int offset, Buffer data) {
		calls++;
		glMax.glGetNamedBufferSubData(buffer, offset, data);
		check();		
	}

}
