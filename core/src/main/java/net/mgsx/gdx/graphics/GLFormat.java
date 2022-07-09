package net.mgsx.gdx.graphics;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GLFormat {
	public static final GLFormat RGB8 = new GLFormat(GL30.GL_RGB8, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE);
	public static final GLFormat RGB16 = new GLFormat(GL30.GL_RGB16F, GL20.GL_RGB, GL20.GL_FLOAT);
	public static final GLFormat RGB32 = new GLFormat(GL30.GL_RGB32F, GL20.GL_RGB, GL20.GL_FLOAT);
	
	public static final GLFormat RGBA8 = new GLFormat(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE);
	public static final GLFormat RGBA16 = new GLFormat(GL30.GL_RGBA16F, GL20.GL_RGBA, GL20.GL_FLOAT);
	public static final GLFormat RGBA32 = new GLFormat(GL30.GL_RGBA32F, GL20.GL_RGBA, GL20.GL_FLOAT);
	
	public static final GLFormat DEPTH16 = new GLFormat(GL30.GL_DEPTH_COMPONENT16, GL20.GL_DEPTH_COMPONENT, GL20.GL_UNSIGNED_SHORT);
	public static final GLFormat DEPTH24 = new GLFormat(GL30.GL_DEPTH_COMPONENT24, GL20.GL_DEPTH_COMPONENT, GL20.GL_UNSIGNED_INT);
	public static final GLFormat DEPTH32 = new GLFormat(GL30.GL_DEPTH_COMPONENT32F, GL20.GL_DEPTH_COMPONENT, GL20.GL_FLOAT);
	
	public int internalFormat, format, type;
	public final int bppGpu, bppCpu, numComponents;

	public GLFormat(int internalFormat, int format, int type) {
		super();
		this.internalFormat = internalFormat;
		this.format = format;
		this.type = type;
		this.bppGpu = glInternalSize(internalFormat);
		this.numComponents = glFormatComponents(format);
		this.bppCpu = numComponents * glTypeSize(type);
	}

	public static int glInternalSize(int internalFormat){
		switch(internalFormat){
		case GL30.GL_R8: return 1;
		case GL30.GL_R16F: return 2;
		case GL30.GL_R32F: return 4;
		
		case GL30.GL_RG8: return 2;
		case GL30.GL_RG16F: return 4;
		case GL30.GL_RG32F: return 8;
		
		case GL30.GL_RGB8: return 3;
		case GL30.GL_RGB16F: return 6;
		case GL30.GL_RGB32F: return 12;
		
		case GL30.GL_RGBA8: return 4;
		case GL30.GL_RGBA16F: return 8;
		case GL30.GL_RGBA32F: return 16;
		
		case GL20.GL_DEPTH_COMPONENT16: return 2;
		case GL30.GL_DEPTH_COMPONENT24: return 3;
		case GL30.GL_DEPTH_COMPONENT32F: return 4;
		
		default: throw new GdxRuntimeException("unknown");
		}
	}
	
	public static int glFormatComponents(int format){
		switch(format){
		case GL30.GL_RGBA: return 4;
		case GL30.GL_RGB: return 3;
		case GL30.GL_RG: return 2;
		case GL30.GL_RED: return 1;
		case GL20.GL_DEPTH_COMPONENT: return 1;
		default: throw new GdxRuntimeException("unknown");
		}
	}

	public static int glTypeSize(int type) {
		switch(type){
		case GL30.GL_UNSIGNED_BYTE:
		case GL30.GL_BYTE: return 1;
		case GL30.GL_SHORT:
		case GL30.GL_UNSIGNED_SHORT: return 2;
		case GL30.GL_INT:
		case GL30.GL_UNSIGNED_INT: return 4;
		case GL30.GL_HALF_FLOAT: return 2;
		case GL30.GL_FLOAT: return 4;
		default: throw new GdxRuntimeException("unknown");
		}
	}

	public GLFormat pack() {
		switch(internalFormat){
		case GL30.GL_R8: 
		case GL30.GL_RG8:
		case GL30.GL_RGB8:
		case GL30.GL_RGBA8:
			
		case GL30.GL_R32F:
		case GL30.GL_RG32F:
		case GL30.GL_RGB32F:
		case GL30.GL_RGBA32F:

			return new GLFormat(internalFormat, format, type);

		case GL30.GL_R16F: 
		case GL30.GL_RG16F:
		case GL30.GL_RGB16F:
		case GL30.GL_RGBA16F:
			
			return new GLFormat(internalFormat, format, GL30.GL_HALF_FLOAT);
			
		default: throw new GdxRuntimeException("unknown");
		}
	}
}
