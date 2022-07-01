package net.mgsx.gdx.graphics.g2d;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.GLOnlyTextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.g2d.RGBE.Header;
import net.mgsx.gfx.GLFormat;

public class HDRILoader {
	
	public Texture load(FileHandle file, GLFormat format){
		DataInputStream in = null;
		try{
			try{
				in = new DataInputStream(new BufferedInputStream(file.read()));
				Header hdrHeader = RGBE.readHeader(in);
				int numPixels = hdrHeader.getWidth() * hdrHeader.getHeight();
				byte[] hdrData = new byte[numPixels * 4];
				RGBE.readPixelsRawRLE(in, hdrData, 0, hdrHeader.getWidth(), hdrHeader.getHeight());
				
				// decode
				return createTexture(hdrHeader, hdrData, format);
			}finally{
				if(in != null) in.close();
			}
		}catch(IOException e){
			throw new GdxRuntimeException(e);
		}
	}
	
	private Texture createTexture(Header hdrHeader, byte[] hdrData, GLFormat format){
		GLOnlyTextureData data = new GLOnlyTextureData(hdrHeader.getWidth(), hdrHeader.getHeight(), 0, format.internalFormat, format.format, format.type);
		Texture texture = new Texture(data);
		int components = format.numComponents;
    	FloatBuffer buffer = BufferUtils.newFloatBuffer(hdrHeader.getWidth() * hdrHeader.getHeight() * components);
    	float [] pixels = new float[components];
    	for(int i=0 ; i<hdrData.length ; i+=4){
    		RGBE.rgbe2float(pixels, hdrData, i);
    		buffer.put(pixels);
    	}
    	buffer.flip();
    	texture.bind();
    	Gdx.gl.glTexImage2D(texture.glTarget, 0, format.internalFormat, hdrHeader.getWidth(), hdrHeader.getHeight(), 0, format.format, format.type, buffer);
    	return texture;
	}
	
}
