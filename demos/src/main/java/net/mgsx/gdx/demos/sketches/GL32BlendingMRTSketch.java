package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.GL32;
import net.mgsx.gltfx.ShaderProgramUtils;

public class GL32BlendingMRTSketch extends ScreenAdapter
{
	private FrameBuffer fbo;
	private ShapeRenderer shapes;
	private SpriteBatch batch;
	private ShaderProgram mrtShader;
	private ShaderProgram alphaShader;
	
	public GL32BlendingMRTSketch() {
		FrameBufferBuilder builder = new FrameBufferBuilder(64, 64);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		fbo = builder.build();
		
		mrtShader = new ShaderProgram(
				Gdx.files.internal("shaders/shape-renderer.vs.glsl"), 
				Gdx.files.internal("shaders/mrt-blending.fs.glsl"));
			ShaderProgramUtils.check(mrtShader);
			
		alphaShader = new ShaderProgram(
				Gdx.files.internal("shaders/sprite-batch.vs.glsl"), 
				Gdx.files.internal("shaders/alpha-as-grayscale.fs.glsl"));
			ShaderProgramUtils.check(alphaShader);
				
		shapes = new ShapeRenderer(6, mrtShader);
		
		batch = new SpriteBatch();
		
		// MRT Blending control with GL32
		
		// Mgdx.gl32.glBlendEquationSeparatei(1, GL20.GL_FUNC_SUBTRACT, GL20.GL_FUNC_ADD);
		for(int i=0 ; i<fbo.getTextureAttachments().size ; i++){
			boolean enabled = Mgdx.gl32.glIsEnabledi(GL20.GL_BLEND, i);
			System.out.println("#" + i + " blending: " + enabled);
		}
	}
	
	@Override
	public void render(float delta) {
		
		// Configure blending for individual render target
		Mgdx.gl32.glEnablei(GL20.GL_BLEND, 0);
		Mgdx.gl32.glBlendFuncSeparatei(0, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);
		
		Mgdx.gl32.glEnablei(GL20.GL_BLEND, 1);
		Mgdx.gl32.glBlendFunci(1, GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		
		Mgdx.gl32.glDisablei(GL20.GL_BLEND, 2);
		
		Mgdx.gl32.glEnablei(GL20.GL_BLEND, 3);
		Mgdx.gl32.glBlendFunci(3, GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		Mgdx.gl32.glBlendEquationi(3, GL20.GL_FUNC_SUBTRACT);
		
		Mgdx.gl32.glDisablei(GL20.GL_BLEND, 4);
		Mgdx.gl32.glColorMaski(4, true, false, false, false);
		
		Mgdx.gl32.glEnablei(GL20.GL_BLEND, 5);
		Mgdx.gl32.glBlendFunci(5, GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		Mgdx.gl32.glBlendEquationSeparatei(5, GL20.GL_FUNC_SUBTRACT, GL20.GL_FUNC_ADD);
		
		Mgdx.gl32.glEnablei(GL20.GL_BLEND, 6);
		Mgdx.gl32.glBlendFunci(6, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Mgdx.gl32.glBlendEquationSeparatei(6, GL32.GL_FUNC_REVERSE_SUBTRACT, GL20.GL_FUNC_ADD);
		
		Mgdx.gl32.glEnablei(GL20.GL_BLEND, 7);
		Mgdx.gl32.glBlendFunci(7, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Mgdx.gl32.glBlendEquationSeparatei(7, GL32.GL_MAX, GL20.GL_FUNC_ADD);
		
		
		// render into MRT fbo
		fbo.begin();
		ScreenUtils.clear(Color.CLEAR);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 4, 4);
		shapes.begin(ShapeType.Filled);
		shapes.setColor(1, .5f, 0, .5f);
		shapes.rect(0, 0, 3, 3);
		shapes.setColor(0f, .5f, .7f, .5f);
		shapes.rect(1, 1, 3, 3);
		shapes.end();
		fbo.end();
		
		// Display render targets
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 4, 4);
		batch.disableBlending();
		batch.begin();
		float x = 0;
		
		batch.draw(fbo.getTextureAttachments().get(0), x, 0, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(1), x+1, 0, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(2), x, 1, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(3), x+1, 1, 1, 1, 0, 0, 1, 1);
		
		batch.draw(fbo.getTextureAttachments().get(4), x, 2, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(5), x+1, 2, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(6), x, 3, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(7), x+1, 3, 1, 1, 0, 0, 1, 1);
		
		batch.setShader(alphaShader);
		x = 2;
		
		batch.draw(fbo.getTextureAttachments().get(0), x, 0, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(1), x+1, 0, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(2), x, 1, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(3), x+1, 1, 1, 1, 0, 0, 1, 1);
		
		batch.draw(fbo.getTextureAttachments().get(4), x, 2, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(5), x+1, 2, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(6), x, 3, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(7), x+1, 3, 1, 1, 0, 0, 1, 1);
		
		batch.setShader(null);
		batch.end();
	}
	
}
