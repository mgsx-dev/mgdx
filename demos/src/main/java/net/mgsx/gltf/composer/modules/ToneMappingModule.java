package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gfx.ToneMappingShader;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class ToneMappingModule implements GLTFComposerModule
{
	private final ToneMappingShader.Reinhard reinhardMode = new ToneMappingShader.Reinhard(true);
	private final ToneMappingShader.Exposure exposureMode = new ToneMappingShader.Exposure(true);
	private final ToneMappingShader.GammaCompression gammaMode = new ToneMappingShader.GammaCompression(true);
	
	private float exposure = 1;
	private float luminosity = 1;
	private float contrast = 1;
	
	private ToneMappingShader current = exposureMode;
	
	private Table controls;
	private Table subControls;
	
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
		controls.add(UI.selector(skin, shaders, current, v->v.getClass().getSimpleName(), v->setMode(v))).row();
		
		controls.add(subControls).colspan(2);
		
		return frame;
	}
	
	private void updateUI(){
		subControls.clear();
		if(current == exposureMode){
			UI.slider(subControls, "Exposure", 1e-4f, 100f, exposure, ControlScale.LOG, value->exposure=value);
		}
		else if(current == gammaMode){
			UI.slider(subControls, "Luminosity", 0.01f, 100f, luminosity, ControlScale.LOG, value->luminosity=value);
			UI.slider(subControls, "Contrast", 0.01f, 100f, contrast, ControlScale.LOG, value->contrast=value);
		}
	}

	private void setMode(ToneMappingShader mode) {
		current = mode;
		updateUI();
	}

	public void render(SpriteBatch batch, Texture texture) {
		current.bind();
		if(current == exposureMode){
			exposureMode.setExposure(exposure);
		}else if(current == gammaMode){
			gammaMode.setLuminosity(luminosity);
			gammaMode.setContrast(contrast);
		}
		batch.setShader(current);
		
		batch.disableBlending();
		batch.begin();
		batch.draw(texture, 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
		batch.setShader(null);
	}
}
