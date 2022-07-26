package net.mgsx.cube;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.Texture3D;
import net.mgsx.gdx.graphics.glutils.CustomTexture3DData;

public class ColorGrading {
	
	public enum LUTFormat{
		ROW, GRID
	}
	
	public static Texture3D createLUT3D(CubeData data){
		CustomTexture3DData textureData = new CustomTexture3DData(data.dimSize, data.dimSize, data.dimSize, 0, GL30.GL_RGB, GL30.GL_RGB32F, GL30.GL_FLOAT);
		data.buffer.rewind();
		textureData.getPixels().put(data.buffer);
		textureData.getPixels().flip();
		Texture3D texture = new Texture3D(textureData);
		texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		return texture;
	}
	
	public static Texture createLUT2D(CubeData data){
		Pixmap pixmap = new Pixmap(data.dimSize * data.dimSize, data.dimSize, Format.RGB888);
		pixmap.setBlending(Blending.None);
		data.buffer.rewind();
		FloatBuffer floatBuffer = data.buffer.asFloatBuffer();
		for(int z=0 ; z<data.dimSize ; z++){
			for(int y=0 ; y<data.dimSize ; y++){
				for(int x=0 ; x<data.dimSize ; x++){
					float r = floatBuffer.get();
					float g = floatBuffer.get();
					float b = floatBuffer.get();
					pixmap.drawPixel(x + z*data.dimSize, y, Color.rgba8888(r,g,b,1));
				}
			}
		}
		
		Texture texture = new Texture(pixmap);
		texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		return texture;
	}
	
	public static Texture[] createRGBLUT2D(CubeData data){
		Pixmap pixmapR = new Pixmap(data.dimSize * data.dimSize, data.dimSize, Format.RGBA8888);
		Pixmap pixmapG = new Pixmap(data.dimSize * data.dimSize, data.dimSize, Format.RGBA8888);
		Pixmap pixmapB = new Pixmap(data.dimSize * data.dimSize, data.dimSize, Format.RGBA8888);
		pixmapR.setBlending(Blending.None);
		pixmapG.setBlending(Blending.None);
		pixmapB.setBlending(Blending.None);
		data.buffer.rewind();
		FloatBuffer floatBuffer = data.buffer.asFloatBuffer();
		for(int z=0 ; z<data.dimSize ; z++){
			for(int y=0 ; y<data.dimSize ; y++){
				for(int x=0 ; x<data.dimSize ; x++){
					float r = floatBuffer.get();
					float g = floatBuffer.get();
					float b = floatBuffer.get();
					pixmapR.drawPixel(x + z*data.dimSize, y, packFloat(r));
					pixmapG.drawPixel(x + z*data.dimSize, y, packFloat(g));
					pixmapB.drawPixel(x + z*data.dimSize, y, packFloat(b));
				}
			}
		}
		
		Texture textureR = new Texture(pixmapR);
		textureR.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		textureR.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		Texture textureG = new Texture(pixmapG);
		textureG.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		textureG.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		Texture textureB = new Texture(pixmapB);
		textureB.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		textureB.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		return new Texture[]{textureR, textureG, textureB};
	}
	
	private static int packFloat(float value){
//		const HIGH vec4 bias = vec4(1.0 / 255.0, 1.0 / 255.0, 1.0 / 255.0, 0.0);
//		HIGH vec4 color = vec4(depth, fract(depth * 255.0), fract(depth * 65025.0), fract(depth * 16581375.0));
//		gl_FragColor = color - (color.yzww * bias);
		
		value = value * 255f / 256f;
		
		float v0 = value % 1f;
		float v1 = (value * 255f) % 1f;
		float v2 = (value * 65025f) % 1f;
		float v3 = (value * 16581375f) % 1f;
		
		// TODO not sure about this...
		// return Color.rgba8888(v0 - v1 / 255f, v1 - v2 / 255f, v2 - v3 / 255f, v3);
		return Color.rgba8888(v0, v1, v2, v3);
	}

	public static Pixmap createNeutralLUT(int size, LUTFormat format) {
		if(format == LUTFormat.ROW){
			Pixmap pixmap = new Pixmap(size*size, size, Format.RGB888);
			pixmap.setBlending(Blending.None);
			for(int z=0 ; z<size ; z++){
				float b = (float)z / (float)(size-1);
				for(int y=0 ; y<size ; y++){
					float g = (float)y / (float)(size-1);
					for(int x=0 ; x<size ; x++){
						float r = (float)x / (float)(size-1);
						pixmap.drawPixel(x + z*size, y, Color.rgba8888(r,g,b,1));
					}
				}
			}
			return pixmap;
		}else{
			// size should be POT
			int gridSize = MathUtils.round((float)Math.sqrt(size));
			if(gridSize * gridSize != size) throw new GdxRuntimeException("size not supported : only 4, 16, 64, 256");
			Pixmap pixmap = new Pixmap(gridSize*size, gridSize*size, Format.RGB888);
			pixmap.setBlending(Blending.None);
			for(int z=0 ; z<size ; z++){
				float b = (float)z / (float)(size-1);
				for(int y=0 ; y<size ; y++){
					float g = (float)y / (float)(size-1);
					for(int x=0 ; x<size ; x++){
						float r = (float)x / (float)(size-1);
						int ix = (z % gridSize) * size + x;
						int iy = (z / gridSize) * size + y;
						pixmap.drawPixel(ix, iy, Color.rgba8888(r,g,b,1));
					}
				}
			}
			return pixmap;
		}
	}

	public static Texture3D createLUT3D(Pixmap pixmap) {
		if(pixmap.getWidth() == pixmap.getHeight()){
			int gridSize = MathUtils.round((float)Math.pow(pixmap.getWidth(), 1.0 / 3.0));
			int dimSize = pixmap.getHeight() / gridSize;
			CustomTexture3DData textureData = new CustomTexture3DData(dimSize, dimSize, dimSize, 0, GL30.GL_RGB, GL30.GL_RGB8, GL30.GL_UNSIGNED_BYTE);
			ByteBuffer buffer = textureData.getPixels();
			Pixmap submap = new Pixmap(dimSize, dimSize, Format.RGB888);
			submap.setBlending(Blending.None);
			for(int y=0 ; y<gridSize ; y++){
				for(int x=0 ; x<gridSize ; x++){
					submap.drawPixmap(pixmap, 0, 0, x*dimSize, y*dimSize, dimSize, dimSize);
					submap.getPixels().rewind();
					buffer.put(submap.getPixels());
				}
			}
			submap.dispose();
			buffer.flip();
			
			Texture3D texture = new Texture3D(textureData);
			texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
			texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			return texture;

		}else{
			
			int dimSize = pixmap.getHeight();
			CustomTexture3DData textureData = new CustomTexture3DData(dimSize, dimSize, dimSize, 0, GL30.GL_RGB, GL30.GL_RGB8, GL30.GL_UNSIGNED_BYTE);
			ByteBuffer buffer = textureData.getPixels();
			Pixmap submap = new Pixmap(dimSize, dimSize, Format.RGB888);
			submap.setBlending(Blending.None);
			for(int i=0 ; i<dimSize ; i++){
				submap.drawPixmap(pixmap, 0, 0, i*dimSize, 0, dimSize, dimSize);
				submap.getPixels().rewind();
				buffer.put(submap.getPixels());
			}
			submap.dispose();
			buffer.flip();
			
			Texture3D texture = new Texture3D(textureData);
			texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
			texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			return texture;
		}
	}

	public static Texture[] createRGBLUT2D(Pixmap pixmap) {
		Pixmap pixmapR = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
		Pixmap pixmapG = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
		Pixmap pixmapB = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
		pixmapR.setBlending(Blending.None);
		pixmapG.setBlending(Blending.None);
		pixmapB.setBlending(Blending.None);
		Color color = new Color();
		for(int y=0 ; y<pixmap.getHeight() ; y++){
			for(int x=0 ; x<pixmap.getWidth() ; x++){
				color.set(pixmap.getPixel(x, y));
				pixmapR.drawPixel(x, y, packFloat(color.r));
				pixmapG.drawPixel(x, y, packFloat(color.g));
				pixmapB.drawPixel(x, y, packFloat(color.b));
			}
		}
	
		Texture textureR = new Texture(pixmapR);
		textureR.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		textureR.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		Texture textureG = new Texture(pixmapG);
		textureG.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		textureG.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		Texture textureB = new Texture(pixmapB);
		textureB.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		textureB.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		return new Texture[]{textureR, textureG, textureB};
	}
}
