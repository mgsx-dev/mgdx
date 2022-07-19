package net.mgsx.cube;

import java.nio.FloatBuffer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

import net.mgsx.gdx.graphics.Texture3D;
import net.mgsx.gdx.graphics.glutils.GLOnlyTexture3DData;

public class ColorGrading {
	public static Texture3D createLUT3D(CubeData data){
		GLOnlyTexture3DData textureData = new GLOnlyTexture3DData(data.dimSize, data.dimSize, data.dimSize, GL30.GL_RGB, GL30.GL_RGB32F, GL30.GL_FLOAT, false);
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
}
