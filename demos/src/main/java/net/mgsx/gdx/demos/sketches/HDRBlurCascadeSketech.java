package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.mgsx.gdx.utils.ShaderProgramUtils;
import net.mgsx.gfx.BlurCascade;
import net.mgsx.gfx.GLFormat;

public class HDRBlurCascadeSketech extends ScreenAdapter
{
	private Texture textureBase;
	private SpriteBatch batch;
	private BlurCascade blur;
	private ShaderProgram toneMapping;

	public HDRBlurCascadeSketech() {
		
		batch = new SpriteBatch();
		
		toneMapping = new ShaderProgram(
			Gdx.files.internal("shaders/sprite-batch.vs.glsl").readString(), 
			"#define GAMMA_COMPRESSION\n" +
			Gdx.files.internal("shaders/tone-mapping.fs.glsl").readString());
		ShaderProgramUtils.check(toneMapping);
		
		GLFormat format = new GLFormat();
		format.format = GL30.GL_RGBA;
		format.internalFormat = GL30.GL_RGBA16F;
		format.type = GL30.GL_FLOAT;
		
		blur = new BlurCascade(format, 16);
		
		textureBase = new Texture(Gdx.files.classpath("libgdx128.png"));
	}
	
	@Override
	public void render(float delta) {

		float inputX = Gdx.input.getX() / (float)Gdx.graphics.getWidth();
		float inputY = Gdx.input.getY() / (float)Gdx.graphics.getHeight();
		
		Texture resultTexture = blur.render(textureBase);
		
		toneMapping.bind();
		toneMapping.setUniformf("u_luminosity", inputX * 2);
		toneMapping.setUniformf("u_contrast", inputY * 2);
		
		batch.setShader(toneMapping);
		batch.disableBlending();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.begin();
		float l = inputX;
		batch.setColor(l, l, l, 1);
		batch.draw(resultTexture, 0, 0, 1, 1);
		batch.end();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
	}
}
