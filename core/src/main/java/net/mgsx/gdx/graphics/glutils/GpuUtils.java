package net.mgsx.gdx.graphics.glutils;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BufferUtils;

public class GpuUtils {
	private static final IntBuffer intBuffer = BufferUtils.newIntBuffer(16);

	public static final String GL_NVX_gpu_memory_info_ext = "GL_NVX_gpu_memory_info";
	public static final int GL_GPU_MEM_INFO_TOTAL_AVAILABLE_MEM_NVX = 0x9048;
	public static final int GL_GPU_MEM_INFO_CURRENT_AVAILABLE_MEM_NVX = 0x9049;
	
	public static int getMaxMemoryKB(){
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(GL_GPU_MEM_INFO_TOTAL_AVAILABLE_MEM_NVX, intBuffer);
		return intBuffer.get();
	}
	
	public static int getAvailableMemoryKB(){
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(GL_GPU_MEM_INFO_CURRENT_AVAILABLE_MEM_NVX, intBuffer);
		return intBuffer.get();
	}
	
	public static boolean hasMemoryInfo(){
		return Gdx.graphics.supportsExtension(GL_NVX_gpu_memory_info_ext);
	}

}
