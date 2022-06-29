package com.badlogic.gdx.backends.lwjgl3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;

import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.GLMax;

public class Lwjgl3GLMax extends Lwjgl3GL32 implements GLMax
{

//	@Override
//	public void glGetBufferSubData(int target, int offset, Buffer buffer) {
//		if(buffer instanceof ByteBuffer){
//			GL15.glGetBufferSubData(target, offset, (ByteBuffer)buffer);
//		}
//	}

	@Override
	public void glGetNamedBufferSubData(int buffer, int offset, Buffer data) {
		if(data instanceof ByteBuffer){
			GL45.glGetNamedBufferSubData(buffer, offset, (ByteBuffer)data);
		}
		else if(data instanceof DoubleBuffer){
			GL45.glGetNamedBufferSubData(buffer, offset, (DoubleBuffer)data);
		}
		else if(data instanceof FloatBuffer){
			GL45.glGetNamedBufferSubData(buffer, offset, (FloatBuffer)data);
		}
		else if(data instanceof IntBuffer){
			GL45.glGetNamedBufferSubData(buffer, offset, (IntBuffer)data);
		}
		else if(data instanceof LongBuffer){
			GL45.glGetNamedBufferSubData(buffer, offset, (LongBuffer)data);
		}
		else if(data instanceof ShortBuffer){
			GL45.glGetNamedBufferSubData(buffer, offset, (ShortBuffer)data);
		}else{
			throw new GdxRuntimeException("not supported");
		}
	}
	
	@Override
	public void glGetTexImage(int target, int level, int glFormat, int glType, Buffer pixels) {
		if(pixels instanceof ByteBuffer){
			GL11.glGetTexImage(target, level, glFormat, glType, (ByteBuffer)pixels);
		}
		else if(pixels instanceof DoubleBuffer){
			GL11.glGetTexImage(target, level, glFormat, glType, (DoubleBuffer)pixels);
		}
		else if(pixels instanceof FloatBuffer){
			GL11.glGetTexImage(target, level, glFormat, glType, (FloatBuffer)pixels);
		}
		else if(pixels instanceof IntBuffer){
			GL11.glGetTexImage(target, level, glFormat, glType, (IntBuffer)pixels);
		}
		else if(pixels instanceof ShortBuffer){
			GL11.glGetTexImage(target, level, glFormat, glType, (ShortBuffer)pixels);
		}else{
			throw new GdxRuntimeException("not supported");
		}
	}

}
