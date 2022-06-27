package com.badlogic.gdx.graphics.profiling;

import net.mgsx.gdx.graphics.GLMax;

public class GLMaxInterceptor extends GL32Interceptor implements GLMax
{
	final GLMax glMax;

	public GLMaxInterceptor(GLProfiler glProfiler, GLMax glMax) {
		super(glProfiler, glMax);
		this.glMax = glMax;
	}

}
