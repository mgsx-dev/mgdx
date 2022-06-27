package net.mgsx.gdx.utils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.utils.BufferUtils;

import net.mgsx.gdx.Mgdx;

public class GLUtils {
	public static final IntBuffer buffer1i = BufferUtils.newIntBuffer(1);
	public static final FloatBuffer buffer16f = BufferUtils.newFloatBuffer(16);
	
	public static int glGenProgramPipeline() {
		Mgdx.gl31.glGenProgramPipelines(1, buffer1i);
		return buffer1i.get();
	}
	public static void glDeleteProgramPipeline(int pipeline) {
		buffer1i.put(pipeline);
		buffer1i.flip();
		Mgdx.gl31.glDeleteProgramPipelines(1, buffer1i);
	}
}
