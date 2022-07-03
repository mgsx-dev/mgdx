package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gfx.ToneMappingShader;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.composer.utils.UI.ControlScale;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class HDRModule implements GLTFComposerModule
{
	private boolean hdrEnabled = true;
	private boolean bloomEnabled = true;
	private Bloom bloom;
	private ToneMappingShader.Exposure toneMapping;
	
	private FrameBuffer fbo;
	private SpriteBatch batch;
	private float exposure = 1f;
	
	public HDRModule() {
		bloom = new Bloom();
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		toneMapping = new ToneMappingShader.Exposure(true);
		
		// defaults
		bloom.bloomRate = .5f;
		bloom.blurMix = .5f;
		exposure = 1;
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table t = new Table(skin);
		
		UI.slider(t, "Ambiant", 0, 1, 1, value->ctx.sceneManager.setAmbientLight(value));
		
		// TODO fill light and back light (rim light)
		// TODO add shadow map option
		
		
		UI.slider(t, "Key light", 0.01f, 100f, ctx.keyLight.intensity, ControlScale.LOG, value->ctx.keyLight.intensity=value);
		
		// key light orientation picker
		ClickListener listener = new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Ray ray = ctx.cameraManager.getCamera().getPickRay(Gdx.input.getX(), Gdx.input.getY());
				ctx.keyLight.direction.set(ray.direction).scl(-1);
				ctx.stage.removeCaptureListener(this);
			}
		};
		t.add(UI.trig(skin, "Pick sun position from skybox", ()->{
			ctx.stage.addCaptureListener(listener);
		})).row();
		
		
		UI.toggle(t, "HDR", hdrEnabled, value->enableHDR(ctx, value));
		
		// TODO propose all tone mapping modes
		UI.slider(t, "Exposure", 0.01f, 100f, exposure, ControlScale.LOG, value->exposure=value);
		
		
		UI.toggle(t, "Bloom", bloomEnabled, value->bloomEnabled=value);
		UI.slider(t, "Bloom rate", 0, 1, bloom.bloomRate, value->bloom.bloomRate=value);
		UI.slider(t, "Bloom blur", 0, 1, bloom.blurMix, value->bloom.blurMix=value);
		
		/*
		UI.slider(t, "Emissive", 0.001f, 1000, 1f, ControlScale.LOG, value->{
			if(ctx.scene != null) GLTFMaterialUtils.forceEmissiveTextureFactor(ctx.scene, value);
		});
		
		UI.slider(t, "Metallic", 0, 1, .5f, value->{
			if(ctx.scene != null) GLTFMaterialUtils.forceMetallic(ctx.scene, value);
		});
		
		UI.slider(t, "Roughness", 0, 1, .5f, value->{
			if(ctx.scene != null) GLTFMaterialUtils.forceRoughness(ctx.scene, value);
		});
		*/
		
		return t;
	}
	
	private void enableHDR(GLTFComposerContext ctx, boolean enabled) {
		hdrEnabled = enabled;
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		ctx.colorShaderConfig.manualGammaCorrection = !hdrEnabled;
		ctx.invalidateShaders();
	}

	@Override
	public void render(GLTFComposerContext ctx) {
		if(hdrEnabled){
			ctx.sceneManager.renderShadows();
			ensureFBO(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
			fbo.begin();
			ScreenUtils.clear(ctx.clearColor, true);
			ctx.sceneManager.renderColors();
			fbo.end();
			applyPostProcess(ctx);
		}else{
			ScreenUtils.clear(ctx.clearColor, true);
			ctx.sceneManager.render();
		}
	}
	
	private void applyPostProcess(GLTFComposerContext ctx) {
		// Bloom
		if(bloomEnabled){
			bloom.apply(fbo, batch);
		}
		toneMapping.bind();
		toneMapping.setExposure(exposure);
		batch.setShader(toneMapping);
		
		// render final with tone mapping (HDR to LDR)
		batch.disableBlending();
		batch.begin();
		batch.draw(fbo.getColorBufferTexture(), 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
		batch.setShader(null);
	}

	private void ensureFBO(int width, int height) {
		if(fbo == null || fbo.getWidth() != width || fbo.getHeight() != height){
			if(fbo != null) fbo.dispose();
			FrameBufferBuilder builder = new FrameBufferBuilder(width, height);
			
			builder.addColorTextureAttachment(GL30.GL_RGBA32F, GL30.GL_RGBA, GL30.GL_FLOAT);
			builder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24);
			
			fbo = builder.build();
		}
	}
}
