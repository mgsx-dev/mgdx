package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.g2d.HDRILoader;
import net.mgsx.gltfx.GLFormat;
import net.mgsx.gltfx.gfx.ToneMappingShader;
import net.mgsx.gltfx.gfx.ToneMappingShader.Exposure;

/**
 * Load a HDRI file and display it as is.
 * 
 * @author mgsx
 *
 */
public class HDRISketch extends ScreenAdapter
{
	private Texture hdri;
	private SpriteBatch batch;
	private Exposure toneMapping;
	
	public HDRISketch() {
		batch = new SpriteBatch();
		toneMapping = new ToneMappingShader.Exposure(true);
		load(Gdx.files.internal("textures/demo2/table_mountain_2_4k.hdr"));
	}
	
	@Override
	public void show() {
		Gdx.graphics.setTitle("Drop HDRI file(*.hdr)");
		Mgdx.inputs.fileDropListener = files->load(files.first());
	}
	private void load(FileHandle hdrFile) {
		if(hdrFile.exists()){
			if(hdri != null) hdri.dispose();
			hdri = new HDRILoader().load(hdrFile, GLFormat.RGB32);
		}
	}

	@Override
	public void hide() {
		Gdx.graphics.setTitle(null);
	}
	@Override
	public void render(float delta) {
		float inX = (float)Gdx.input.getX() / (float)Gdx.graphics.getWidth();
		toneMapping.bind();
		toneMapping.setExposure((float)Math.pow(inX * 2, 3));
		batch.setShader(toneMapping);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.begin();
		batch.draw(hdri, 0, 0, 1, 1);
		batch.end();
	}
}
