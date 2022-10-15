package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.ComputeShader;
import net.mgsx.gdx.graphics.GL31;
import net.mgsx.gltfx.ShaderProgramUtils;

// FIXME not working
public class GL31ComputeShaderChainSketch extends ScreenAdapter
{
	private Array<Texture> texturesDown = new Array<Texture>();
	private Array<Texture> texturesUp = new Array<Texture>();
	private ComputeShader sampleDown, sampleUp;
	private SpriteBatch batch;
	private Texture composedTexture;
	private Texture textureBase;

	public GL31ComputeShaderChainSketch() {
		
		batch = new SpriteBatch();
		
		int baseWidth = 64 * 8;
		int baseHeight = 32 * 8;
		int maxStages = 8;
		
		textureBase = new Texture(Gdx.files.classpath("libgdx128.png"));
		baseWidth = textureBase.getWidth();
		baseHeight = textureBase.getHeight();
		texturesDown.add(textureBase);
		
		composedTexture = new Texture(baseWidth, baseHeight, Format.RGBA8888);
		texturesUp.add(composedTexture);
		
		for(int i=0, w=baseWidth, h=baseHeight ; i<maxStages && w>0 && h>0 ; i++, w/=2, h/=2){
			if(i>0){
				Texture texture = new Texture(w, h, Format.RGBA8888);
				texturesDown.add(texture);
				texturesUp.add(texture);
			}
		}
		
		sampleDown = new ComputeShader(Gdx.files.classpath("shaders/sample-down.cs.glsl").readString());
		ShaderProgramUtils.check(sampleDown);
		
		sampleUp = new ComputeShader(Gdx.files.classpath("shaders/sample-up.cs.glsl").readString());
		ShaderProgramUtils.check(sampleUp);
		
	}
	
	@Override
	public void render(float delta) {
		
		sampleDown.bind();
		sampleDown.setUniformi("img_in", 0);
		
		for(int i=0 ; i<texturesDown.size-1 ; i++){
			Texture inputTexture = texturesDown.get(i);
			Texture outputTexture = texturesDown.get(i+1);
			
			if(i > 0){
				Mgdx.gl31.glMemoryBarrier(GL31.GL_TEXTURE_FETCH_BARRIER_BIT);
			}
			
			inputTexture.bind();
			Mgdx.gl31.glBindImageTexture(0, outputTexture.getTextureObjectHandle(), 0, false, 0, GL31.GL_WRITE_ONLY, GL30.GL_RGBA8);
			
			Mgdx.gl31.glDispatchCompute(outputTexture.getWidth(), outputTexture.getHeight(), 1);
			
		}

		
		sampleUp.bind();
		sampleUp.setUniformi("img_in", 0);
		sampleUp.setUniformf("u_mix", .5f, .5f);
		sampleUp.setUniformf("u_mix", .1f, 1.5f);
		sampleUp.setUniformf("u_mix", 0.99f, 0.01f);
		
		// sampleUp.setUniformf("u_mix", .01f, 2.9f);
		
		for(int i=texturesUp.size-1 ; i>=1 ; i--){
			Texture inputTexture = texturesUp.get(i);
			Texture outputTexture = texturesUp.get(i-1);
			
			
			inputTexture.bind();
			Mgdx.gl31.glBindImageTexture(0, outputTexture.getTextureObjectHandle(), 0, false, 0, GL31.GL_WRITE_ONLY, GL30.GL_RGBA8);
			
			Mgdx.gl31.glMemoryBarrier(GL31.GL_TEXTURE_FETCH_BARRIER_BIT);

			Mgdx.gl31.glDispatchCompute(outputTexture.getWidth(), outputTexture.getHeight(), 1);
		}
		
		Texture lastTexture = composedTexture;
		
		batch.disableBlending();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.begin();
		batch.draw(lastTexture, 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
	}
	
	@Override
	public void dispose() {
		sampleDown.dispose();
		batch.dispose();
	}
}
