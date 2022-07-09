package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.scenes.scene2d.ui.ColorBox;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene.PBRRenderTargets;
import net.mgsx.gltf.scene.RenderTargets;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class HDRModule implements GLTFComposerModule
{
	private boolean hdrEnabled = true, mrtEnabled = true;
	
	private SpriteBatch batch;
	
	private ToneMappingModule toneMappingModule = new ToneMappingModule();
	private BloomModule bloomModule = new BloomModule();
	private ShadowModule ShadowModule = new ShadowModule();
	
	private CavityModule cavityModule;
	
	public HDRModule(GLTFComposerContext ctx) {
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		cavityModule = new CavityModule(ctx);
		enableHDR(ctx, hdrEnabled);
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		
		Table t = UI.table(skin);
		
		UI.header(t, "Lights");
		
		UI.slider(t, "Ambiant", 0, 1, 1, value->ctx.sceneManager.setAmbientLight(value));
		
		// TODO fill light and back light (rim light)
		
		t.add(new ColorBox("Key light", ()->ctx.keyLight.baseColor, false, skin)).row();
		
		UI.slider(t, "Key light", 0.01f, 100f, ctx.keyLight.intensity, ControlScale.LOG, value->ctx.keyLight.intensity=value);
		
		// Shadows
		t.add(ShadowModule.initUI(ctx, skin)).growX().row();
		
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
		
		t.add(cavityModule.initUI(ctx, skin)).fill().row();
		
		t.add(bloomModule.initUI(ctx, skin)).fill().row();
		
		t.add(toneMappingModule.initUI(ctx, skin)).fill().row();
		
		return t;
	}
	
	private void enableHDR(GLTFComposerContext ctx, boolean enabled) {
		hdrEnabled = enabled;
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		ctx.colorShaderConfig.manualGammaCorrection = !hdrEnabled;
		ctx.invalidateShaders();
		ctx.fbo.replaceLayer(RenderTargets.COLORS, hdrEnabled ? GLFormat.RGB16 : GLFormat.RGB8);
		ctx.invalidateFBO();
	}

	@Override
	public void render(GLTFComposerContext ctx) {
		// update shadow light
		if(ctx.keyLight instanceof DirectionalShadowLight){
			DirectionalShadowLight shadowLight = (DirectionalShadowLight)ctx.keyLight;
			shadowLight.setBounds(ctx.sceneBounds);
		}
		
		if(hdrEnabled || mrtEnabled){
			ctx.sceneManager.renderShadows();
			ctx.fbo.ensureScreenSize();
			ctx.fbo.begin();
			ScreenUtils.clear(ctx.clearColor, true);
			ctx.sceneManager.renderColors();
			ctx.fbo.end();
			applyPostProcess(ctx);
		}else{
			ScreenUtils.clear(ctx.clearColor, true);
			ctx.sceneManager.render();
		}
	}
	
	private void applyPostProcess(GLTFComposerContext ctx) {

		cavityModule.render(batch, ctx.fbo);
		
		// TODO need to render to first layer only ! or need another FBO and compose later with tone mapping ?
		// render bloom
		bloomModule.render(batch, ctx.fbo.getFrameBuffer());
		
		// render final with tone mapping (HDR to LDR)
		toneMappingModule.render(batch, ctx.fbo.getTexture(PBRRenderTargets.COLORS));
		
	}

}
