package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.mgsx.gltfx.ShaderProgramUtils;

public class CubemapDebugShader extends ShaderProgram {
	
	private boolean mipmaps;

	public CubemapDebugShader(boolean mipmaps) {
		super(
			Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(),
			(mipmaps ? "#define MIPMAP\n" : "") +
			Gdx.files.classpath("shaders/cubemap-debug.fs.glsl").readString()
			);
		ShaderProgramUtils.check(this);
		this.mipmaps = mipmaps;
	}
	private Viewport viewport = new FitViewport(1, 1);
	private Matrix4 matrix = new Matrix4();

	public void setCubemap(Cubemap map, float offsetX, float offsetY, float mipMapBias){

		setUniformi("u_textureCube", 1);
		map.bind(1);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

		matrix.idt()
		.rotate(Vector3.Y, 180 * offsetY)
		.rotate(Vector3.X, 180 * (offsetX + .5f))
		;
		setUniformMatrix("u_offset", matrix);
		
		if(mipmaps){
			setUniformf("u_bias", mipMapBias);
		}
		
//		int renderWidth = map.getWidth();
//		int renderHeight = map.getHeight();
//		
//		viewport.setWorldSize(renderWidth, renderHeight);
//		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
//		
//		SpriteBatch batch = ctx.batch;
//		batch.disableBlending();
//		batch.setShader(shader);
//		batch.setProjectionMatrix(viewport.getCamera().combined);
//		batch.begin();
//		batch.draw(ctx.whitePixel, 0, 0, renderWidth, renderHeight);
//		batch.end();
	}
	
}
