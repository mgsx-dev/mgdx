package net.mgsx.gdx.graphics.glutils;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.Texture3DData;

public class GLOnlyTexture3DData implements Texture3DData {

	private int width, height, depth;
	private ByteBuffer pixels;
	private boolean useMipMaps;
	private int glFormat;
	private int glInternalFormat;
	private int glType;
	
	public GLOnlyTexture3DData(int width, int height, int depth, int glFormat, int glInternalFormat, int glType, boolean useMipMaps) {
		super();
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.glFormat = glFormat;
		this.glInternalFormat = glInternalFormat;
		this.glType = glType;
		this.useMipMaps = useMipMaps;
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
		return useMipMaps;
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

	/**
	 * pixels buffer can be used in both way: 
	 * to upload pixels: fill buffer, bind texture and call {@link #consume3DData()}
	 * to downlaod pixels: bind texture, call {@link #downloadData()} and {@link #getPixels()}
	 * @return the buffer
	 */
	public ByteBuffer getPixels() {
		ensureBuffer();
		return pixels;
	}
	
	private void ensureBuffer(){
		if(pixels == null){
			int bytesPerPixel;
			if(glInternalFormat == GL30.GL_R8){ // TODO other variants
				bytesPerPixel = 1;
			}else if(glInternalFormat == GL30.GL_RGBA8){ // TODO other variants
				bytesPerPixel = 4;
			}else if(glInternalFormat == GL30.GL_RGBA16F){ // TODO other variants
				bytesPerPixel = 8;
			}else if(glInternalFormat == GL30.GL_RGBA32F){ // TODO other variants
				bytesPerPixel = 16;
			}else{
				throw new GdxRuntimeException("unsupported glInternalFormat: " + glInternalFormat);
			}
			
			pixels = BufferUtils.newByteBuffer(width * height * depth * bytesPerPixel);
		}
		
	}

	@Override
	public void consume3DData() {
		// TODO use level for mipmaps
		if(pixels != null){
			Gdx.gl30.glTexImage3D(GL30.GL_TEXTURE_3D, 0, glInternalFormat, width, height, depth, 0, glFormat, glType, pixels);
		}else{
			Gdx.gl30.glTexImage3D(GL30.GL_TEXTURE_3D, 0, glInternalFormat, width, height, depth, 0, glFormat, glType, null);
		}
	}

	@Override
	public void downloadData() {
		ensureBuffer();
		pixels.clear();
		// TODO for each level
		Mgdx.glMax.glGetTexImage(GL30.GL_TEXTURE_3D, 0, glFormat, glType, pixels);
	}

}
