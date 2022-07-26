package net.mgsx.gdx.graphics.glutils;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.Texture3DData;

// TODO separate : CustomTexture3DData and GLOnlyTexture3DData
public class GLOnlyTexture3DData implements Texture3DData {

	private int width, height, depth;
	private int mipMapLevel;
	private int glFormat;
	private int glInternalFormat;
	private int glType;
	private ByteBuffer pixels;
	
	/**
	 * @see "https://registry.khronos.org/OpenGL-Refpages/es3.0/html/glTexImage3D.xhtml"
	 * @param width
	 * @param height
	 * @param depth
	 * @param mipMapLevel
	 * @param glFormat
	 * @param glInternalFormat
	 * @param glType
	 */
	public GLOnlyTexture3DData(int width, int height, int depth, int mipMapLevel, int glFormat, int glInternalFormat, int glType) {
		super();
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.glFormat = glFormat;
		this.glInternalFormat = glInternalFormat;
		this.glType = glType;
		this.mipMapLevel = mipMapLevel;
	}
	
	@Override
	public boolean isPrepared() {
		return true;
	}

	@Override
	public void prepare() {
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
	
	public int getDepth(){
		return depth;
	}

	@Override
	public boolean useMipMaps() {
		return false;
	}

	@Override
	public boolean isManaged() {
		return false;
	}

	public int getInternalFormat() {
		return glInternalFormat;
	}

	public int getGLType() {
		return glType;
	}
	
	public int getGLFormat() {
		return glFormat;
	}

	public int getMipMapLevel() {
		return mipMapLevel;
	}
	
	public ByteBuffer getPixels() {
		ensureBuffer();
		return pixels;
	}
	
	private void ensureBuffer(){
		if(pixels == null){
			
			int numChannels;
			if(glFormat == GL30.GL_RED || glFormat == GL30.GL_RED_INTEGER || glFormat == GL30.GL_LUMINANCE || glFormat == GL30.GL_ALPHA){
				numChannels = 1;
			}else if(glFormat == GL30.GL_RG || glFormat == GL30.GL_RG_INTEGER || glFormat == GL30.GL_LUMINANCE_ALPHA){
				numChannels = 2;
			}else if(glFormat == GL30.GL_RGB || glFormat == GL30.GL_RGB_INTEGER){
				numChannels = 3;
			}else if(glFormat == GL30.GL_RGBA || glFormat == GL30.GL_RGBA_INTEGER){
				numChannels = 4;
			}else{
				throw new GdxRuntimeException("unsupported glFormat: " + glFormat);
			}
			
			int bytesPerChannel;
			if(glType == GL30.GL_UNSIGNED_BYTE || glType == GL30.GL_BYTE){
				bytesPerChannel = 1;
			}else if(glType == GL30.GL_UNSIGNED_SHORT || glType == GL30.GL_SHORT || glType == GL30.GL_HALF_FLOAT){
				bytesPerChannel = 2;
			}else if(glType == GL30.GL_UNSIGNED_INT || glType == GL30.GL_INT || glType == GL30.GL_FLOAT){
				bytesPerChannel = 4;
			}else{
				throw new GdxRuntimeException("unsupported glType: " + glType);
			}
			
			int bytesPerPixel = numChannels * bytesPerChannel;
			
			pixels = BufferUtils.newByteBuffer(width * height * depth * bytesPerPixel);
		}
		
	}

	@Override
	public void consume3DData() {
		Gdx.gl30.glTexImage3D(GL30.GL_TEXTURE_3D, mipMapLevel, glInternalFormat, width, height, depth, 0, glFormat, glType, pixels);
	}

}
