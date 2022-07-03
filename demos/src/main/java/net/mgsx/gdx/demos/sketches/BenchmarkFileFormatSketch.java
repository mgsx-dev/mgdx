package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.exr.EXRLoader;
import net.mgsx.gdx.graphics.GL32;
import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.graphics.Texture3D;
import net.mgsx.gdx.graphics.g2d.HDRILoader;
import net.mgsx.gdx.graphics.glutils.TextureUtils;
import net.mgsx.ktx2.KTX2TextureData;

public class BenchmarkFileFormatSketch extends ScreenAdapter
{
	private SpriteBatch batch;
	private GLTexture texture;
	private CubemapDebugShader cubemapShader;
	private Texture whitePixel;
	
	public BenchmarkFileFormatSketch() {
		batch = new SpriteBatch();
		whitePixel = TextureUtils.createWhitePixel();
	}
	
	@Override
	public void show() {
		Gdx.graphics.setTitle("Drop image file (*.ktx2, *.hdr, *.exr, *.png, *.jpg, *.ktx)");
		Mgdx.inputs.fileDropListener = files->load(files.first());
	}
	private void load(FileHandle file) {
		if(file.exists()){
			if(texture != null) texture.dispose();
			String ext = file.extension().toLowerCase();
			long ptime = System.currentTimeMillis();
			if(ext.equals("ktx2")){
				KTX2TextureData data = new KTX2TextureData(file);
				data.prepare();
				switch(data.getTarget()){
				case GL20.GL_TEXTURE_2D:
					texture = new Texture(data);
					break;
				case GL30.GL_TEXTURE_3D:
					texture = new Texture3D(data);
					break;
				case GL30.GL_TEXTURE_CUBE_MAP:
					texture = new Cubemap(data);
					if(cubemapShader != null) cubemapShader.dispose();
					cubemapShader = new CubemapDebugShader(data.getMipmapCount() > 1);
					break;
				case GL30.GL_TEXTURE_2D_ARRAY:
				case GL32.GL_TEXTURE_CUBE_MAP_ARRAY:
					throw new GdxRuntimeException("not supported");
				}
				if(data.getMipmapCount() > 1){
					texture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
				}else{
					texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
				}
			}else if(ext.equals("hdr")){
				HDRILoader hdri = new HDRILoader();
				hdri.load(file);
				texture = hdri.createTexture(GLFormat.RGB16);
			}else if(ext.equals("exr")){
				EXRLoader exr = new EXRLoader();
				exr.load(file);
				texture = exr.createTexture(GLFormat.RGB16);
			}else{
				System.err.println("not yet supported: " + ext);
			}
			long ctime = System.currentTimeMillis();
			if(texture != null){
				System.out.println("Load time: " + (ctime-ptime) + "ms");
			}
		}
	}

	@Override
	public void hide() {
		Gdx.graphics.setTitle(null);
	}
	@Override
	public void render(float delta) {
		float ix = Gdx.input.getX() / (float)Gdx.graphics.getWidth();
		float iy = Gdx.input.getY() / (float)Gdx.graphics.getHeight();

		if(texture instanceof Texture){
			batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
			batch.begin();
			batch.draw((Texture) texture, 0, 0, 1, 1);
			batch.end();
		}
		else if(texture instanceof Cubemap){
			cubemapShader.bind();
			cubemapShader.setCubemap((Cubemap)texture, iy, ix, (iy*2-1) * 10);
			batch.setShader(cubemapShader);
			batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
			batch.begin();
			batch.draw(whitePixel, 0, 0, 1, 1);
			batch.end();
			batch.setShader(null);
		}
	}
}
