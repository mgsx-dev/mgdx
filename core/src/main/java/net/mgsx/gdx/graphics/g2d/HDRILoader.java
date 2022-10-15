package net.mgsx.gdx.graphics.g2d;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.GLOnlyTextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.g2d.RGBE.Header;
import net.mgsx.gltfx.GLFormat;

// TODO rename HDRI
public class HDRILoader {
	
	private Header hdrHeader;
	private byte[] hdrData;
	
	public int getWidth() {
		return hdrHeader.getWidth();
	}
	public int getHeight() {
		return hdrHeader.getHeight();
	}
	
	public Texture load(FileHandle file, GLFormat format){
		load(file.read());
		return createTexture(format);
	}
	
	public Texture load(InputStream stream, GLFormat format){
		load(stream);
		return createTexture(format);
	}
	
	public void load(FileHandle file){
		load(file.read());
	}
	
	public void load(InputStream stream){
		DataInputStream in = null;
		try{
			try{
				in = new DataInputStream(new BufferedInputStream(stream));
				hdrHeader = RGBE.readHeader(in);
				int numPixels = hdrHeader.getWidth() * hdrHeader.getHeight();
				hdrData = new byte[numPixels * 4];
				RGBE.readPixelsRawRLE(in, hdrData, 0, hdrHeader.getWidth(), hdrHeader.getHeight());
			}finally{
				if(in != null) in.close();
			}
		}catch(IOException e){
			throw new GdxRuntimeException(e);
		}
	}
	
	public ByteBuffer createRGBBuffer() {
		int components = 3;
		ByteBuffer buffer = BufferUtils.newByteBuffer(hdrHeader.getWidth() * hdrHeader.getHeight() * components * 4);
    	FloatBuffer fb = buffer.asFloatBuffer();
		float [] pixels = new float[components];
    	for(int i=0 ; i<hdrData.length ; i+=4){
    		RGBE.rgbe2float(pixels, hdrData, i);
    		fb.put(pixels);
    	}
    	fb.flip();
		return buffer;
	}

	public Texture createTexture(GLFormat format){
		GLOnlyTextureData data = new GLOnlyTextureData(hdrHeader.getWidth(), hdrHeader.getHeight(), 0, format.internalFormat, format.format, format.type);
		Texture texture = new Texture(data);
		FloatBuffer buffer = createRGBBuffer().asFloatBuffer();
    	texture.bind();
    	Gdx.gl.glTexImage2D(texture.glTarget, 0, format.internalFormat, hdrHeader.getWidth(), hdrHeader.getHeight(), 0, format.format, format.type, buffer);
    	return texture;
	}
	
}
