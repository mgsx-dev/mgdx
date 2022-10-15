package net.mgsx.gdx.graphics.glutils;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.CubemapData;
import com.badlogic.gdx.graphics.GL20;

import net.mgsx.gltfx.GLFormat;

public class FlexCubemapData implements CubemapData {

	private int width;
	private int height;
	private int levels;
	private GLFormat format;
	private ByteBuffer buffer;
	
	public FlexCubemapData(int width, int height, int levels, GLFormat format, ByteBuffer buffer) {
		this(width, height, format, levels);
		this.buffer = buffer;
	}
	public FlexCubemapData(int width, int height, GLFormat format) {
		this(width, height, format, 1);
	}
	public FlexCubemapData(int width, int height, GLFormat format, int levels) {
		this.width = width;
		this.height = height;
		this.format = format;
		this.levels = levels;
	}

	@Override
	public boolean isPrepared() {
		return false;
	}

	@Override
	public void prepare() {
	}

	@Override
	public void consumeCubemapData() {
		int position = 0;
		int bpp = format.bppCpu;
		for(int level=0 ; level<levels ; level++){
			int w = width >> level;
			int h = height >> level; 
			for(int face=0 ; face<6 ; face++){
				if(buffer != null){
					buffer.position(position);
					position += w*h*bpp;
					buffer.limit(position);
				}
				Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, level,
						format.internalFormat, w, h, 0, format.format, format.type, buffer);
			}
		}
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public boolean isManaged() {
		return false;
	}

}
