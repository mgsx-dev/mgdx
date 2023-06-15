package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.ui.CUI;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.CascadeShadowMap;

public class CSMModule implements GLTFComposerModule {

	private boolean enabled;
	private Texture defaultMap;
	private CascadeShadowMap csm;
	private int numCascades = 3;
	private boolean displayCascades;
	private float splitDivisor = 4f;
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Frame frame = UI.frameToggle("CSM", skin, enabled, v->enable(ctx,v));
		Table t = frame.getContentTable();
		CUI.slideri(t, "Cascades", 0, 4, numCascades, v->{numCascades=v; recreateCSM(ctx);});
		CUI.slider(t, "Split divisor", 1f, 16f, splitDivisor, v->{splitDivisor=v;});
		CUI.toggle(t, "Display cascades", displayCascades, v->displayCascades=v);
		return frame;
	}

	private void recreateCSM(GLTFComposerContext ctx) {
		enable(ctx, enabled);
	}

	private void enable(GLTFComposerContext ctx, boolean enable) {
		enabled = enable;
		if(enabled && numCascades > 0){
			csm = new CascadeShadowMap(numCascades);
		}else{
			csm = null;
		}
		ctx.sceneManager.setCascadeShadowMap(csm);
		ctx.invalidateShaders();
	}
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		DirectionalShadowLight shadowLight = ctx.sceneManager.getFirstDirectionalShadowLight();
		if(shadowLight != null){
			if(csm != null){
				float size = Math.max(ctx.sceneBounds.getWidth(), Math.max(ctx.sceneBounds.getHeight(), ctx.sceneBounds.getDepth()));
				csm.setCascades(ctx.cameraManager.getCamera(), shadowLight, size, splitDivisor);
				defaultMap = (Texture)shadowLight.getDepthMap().texture;
			}
		}
	}
	
	@Override
	public void renderOverlay(GLTFComposerContext ctx) {
		if(csm != null && displayCascades){
			ctx.batch.disableBlending();
			float ratio = (float)Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
			float w = 0.1f;
			float h = w * ratio;
			float pad = 0.003f;
			
			for(int i=0 ; i<=csm.lights.size ; i++){
				Texture texture = i < csm.lights.size ? (Texture)csm.lights.get(i).getDepthMap().texture : defaultMap;
				if(texture != null){
					FrameBufferUtils.subBlit(ctx.batch, texture, 1 - (w + pad) * (i + 1), 1-h-pad, w, h);
				}
			}
			ctx.batch.enableBlending();
		}
	}
	
	
}
