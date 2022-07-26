package net.mgsx.gdx.utils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.Texture3D;
import net.mgsx.gdx.graphics.Texture3DData;
import net.mgsx.gdx.graphics.glutils.GLOnlyTexture3DData;

public class GLUtils {
	public static final IntBuffer buffer1i = BufferUtils.newIntBuffer(1);
	public static final FloatBuffer buffer16f = BufferUtils.newFloatBuffer(16);
	
	public static int glGenProgramPipeline() {
		buffer1i.clear();
		Mgdx.gl31.glGenProgramPipelines(1, buffer1i);
		return buffer1i.get();
	}
	public static void glDeleteProgramPipeline(int pipeline) {
		buffer1i.clear();
		buffer1i.put(pipeline);
		buffer1i.flip();
		Mgdx.gl31.glDeleteProgramPipelines(1, buffer1i);
	}
	public static int getMaxSamples() {
		return geti(GL30.GL_MAX_SAMPLES);
	}
	public static int getMaxSamplesPoT() {
		int max = geti(GL30.GL_MAX_SAMPLES);
		return max > 1 ? 32 - Integer.numberOfLeadingZeros(max - 1) : 0;
	}
	public static int geti(int pname) {
		buffer1i.clear();
		Gdx.gl.glGetIntegerv(pname, buffer1i);
		return buffer1i.get();
	}
	public static void downloadTextureData(Texture3D texture){
		Texture3DData data = texture.getData();
		if(data instanceof GLOnlyTexture3DData){
			GLOnlyTexture3DData customData = (GLOnlyTexture3DData)data;
			texture.bind();
			ByteBuffer pixels = customData.getPixels();
			pixels.clear();
			Mgdx.glMax.glGetTexImage(GL30.GL_TEXTURE_3D, customData.getMipMapLevel(), customData.getGLFormat(), data.getGLType(), pixels);
		}else{
			throw new GdxRuntimeException("texture data not supported");
		}
	}
}
