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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.ComposerUtils;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.composer.utils.UI.ControlScale;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class HDRModule implements GLTFComposerModule
{
	private boolean hdrEnabled = true;
	
	private FrameBuffer fbo;
	private SpriteBatch batch;
	
	private ToneMappingModule toneMappingModule = new ToneMappingModule();
	private BloomModule bloomModule = new BloomModule();
	
	public HDRModule() {
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		
		Table t = UI.table(skin);
		
		UI.header(t, "Lights");
		
		UI.slider(t, "Ambiant", 0, 1, 1, value->ctx.sceneManager.setAmbientLight(value));
		
		// TODO fill light and back light (rim light)
		// TODO add shadow map option
		
		
		UI.slider(t, "Key light", 0.01f, 100f, ctx.keyLight.intensity, ControlScale.LOG, value->ctx.keyLight.intensity=value);
		
		Array<Integer> shadowSizes = new Array<Integer>();
		for(int i=8 ; i<=12 ; i++) shadowSizes.add(1<<i);
		t.add(UI.selector(skin, shadowSizes, ctx.shadowSize, v->v+"x"+v, v->{ctx.shadowSize = v; ComposerUtils.updateShadowSize(ctx);})).row();
		
		UI.toggle(t, "Shadows", ctx.shadows, value->{ctx.shadows = value; ComposerUtils.recreateLight(ctx);});
		
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
		
		UI.header(t, "Post processing");
		
		UI.toggle(t, "HDR", hdrEnabled, value->enableHDR(ctx, value));
		
		t.add(bloomModule.initUI(ctx, skin)).fill().row();
		
		t.add(toneMappingModule.initUI(ctx, skin)).fill().row();
		
		return t;
	}
	
	private void enableHDR(GLTFComposerContext ctx, boolean enabled) {
		hdrEnabled = enabled;
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		ctx.colorShaderConfig.manualGammaCorrection = !hdrEnabled;
		ctx.invalidateShaders();
		if(fbo != null){
			fbo.dispose();
			fbo = null;
		}
	}

	@Override
	public void render(GLTFComposerContext ctx) {
		// update shadow light
		if(ctx.keyLight instanceof DirectionalShadowLight){
			DirectionalShadowLight shadowLight = (DirectionalShadowLight)ctx.keyLight;
			shadowLight.setBounds(ctx.sceneBounds);
		}
		
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
		// render bloom
		bloomModule.render(batch, fbo);
		
		// render final with tone mapping (HDR to LDR)
		toneMappingModule.render(batch, fbo.getColorBufferTexture());
	}

	private void ensureFBO(int width, int height) {
		if(fbo == null || fbo.getWidth() != width || fbo.getHeight() != height){
			if(fbo != null) fbo.dispose();
			FrameBufferBuilder builder = new FrameBufferBuilder(width, height);
			
			if(hdrEnabled){
				builder.addColorTextureAttachment(GL30.GL_RGBA16F, GL30.GL_RGBA, GL30.GL_FLOAT);
			}else{
				builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
			}
			builder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24);
			
			fbo = builder.build();
		}
	}
}
