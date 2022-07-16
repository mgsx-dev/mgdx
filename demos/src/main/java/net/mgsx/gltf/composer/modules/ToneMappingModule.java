package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gfx.ToneMappingShader;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.core.Composition;
import net.mgsx.gltf.composer.core.Composition.ToneMappingOptions.ToneMappingMode;

public class ToneMappingModule implements GLTFComposerModule
{
	private final ToneMappingShader.Reinhard reinhardMode = new ToneMappingShader.Reinhard(true);
	private final ToneMappingShader.Exposure exposureMode = new ToneMappingShader.Exposure(true);
	private final ToneMappingShader.GammaCompression gammaMode = new ToneMappingShader.GammaCompression(true);
	
	private ToneMappingShader current = exposureMode;
	
	private Table controls;
	private Table subControls;
	private SelectBox<ToneMappingShader> selector;
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Frame frame = UI.frame("Tone mapping", skin);
		controls = frame.getContentTable();
		
		subControls = UI.table(skin);
		
		Array<ToneMappingShader> shaders = new Array<ToneMappingShader>();
		shaders.add(reinhardMode);
		shaders.add(exposureMode);
		shaders.add(gammaMode);
		
		controls.add("Mode");
		controls.add(selector = UI.selector(skin, shaders, current, v->v.getClass().getSimpleName(), v->setMode(ctx.compo, v))).row();
		
		controls.add(subControls).colspan(2);
		
		return frame;
	}
	
	private void updateUI(Composition compo){
		if(selector != null && current != null) selector.setSelected(current);
		subControls.clear();
		if(current == exposureMode){
			UI.slider(subControls, "Exposure", 1e-4f, 100f, compo.toneMapping.exposure, ControlScale.LOG, value->compo.toneMapping.exposure=value);
		}
		else if(current == gammaMode){
			UI.slider(subControls, "Luminosity", 0.01f, 100f, compo.toneMapping.luminosity, ControlScale.LOG, value->compo.toneMapping.luminosity=value);
			UI.slider(subControls, "Contrast", 0.01f, 100f, compo.toneMapping.contrast, ControlScale.LOG, value->compo.toneMapping.contrast=value);
		}
	}

	private void setMode(Composition compo, ToneMappingShader mode) {
		if(mode == reinhardMode){
			compo.toneMapping.mode = ToneMappingMode.REINHARD;
		}
		else if(mode == exposureMode){
			compo.toneMapping.mode = ToneMappingMode.EXPOSURE;
		}
		else if(mode == gammaMode){
			compo.toneMapping.mode = ToneMappingMode.GAMMA_COMPRESSION;
		}else{
			compo.toneMapping.mode = null;
		}
		current = mode;
		updateUI(compo);
	}

	public void render(GLTFComposerContext ctx, SpriteBatch batch, Texture texture) {
		if(ctx.compositionJustChanged){
			if(ctx.compo.toneMapping.mode == ToneMappingMode.REINHARD){
				current = reinhardMode;
			}
			if(ctx.compo.toneMapping.mode == ToneMappingMode.EXPOSURE){
				current = exposureMode;
			}
			if(ctx.compo.toneMapping.mode == ToneMappingMode.GAMMA_COMPRESSION){
				current = gammaMode;
			}
			updateUI(ctx.compo);
		}
		
		current.bind();
		
		if(current == exposureMode){
			exposureMode.setExposure(ctx.compo.toneMapping.exposure);
		}else if(current == gammaMode){
			gammaMode.setLuminosity(ctx.compo.toneMapping.luminosity);
			gammaMode.setContrast(ctx.compo.toneMapping.contrast);
		}
		batch.setShader(current);
		
		batch.disableBlending();
		batch.begin();
		batch.draw(texture, 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
		batch.setShader(null);
	}
}
