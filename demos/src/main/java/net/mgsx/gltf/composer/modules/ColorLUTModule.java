package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.cube.ColorGrading;
import net.mgsx.cube.ColorGradingShaders.ColorGrading2D;
import net.mgsx.cube.ColorGradingShaders.ColorGrading2DHQ;
import net.mgsx.cube.ColorGradingShaders.ColorGrading3D;
import net.mgsx.cube.CubeData;
import net.mgsx.cube.CubeDataLoader;
import net.mgsx.gdx.graphics.Texture3D;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class ColorLUTModule implements GLTFComposerModule
{
	private Texture3D lut3D;
	private Texture lut2D;
	private Texture[] lutRGB2D;

	private ColorGrading3D shader3D = new ColorGrading3D();
	private ColorGrading2D shader2D = new ColorGrading2D();
	private ColorGrading2DHQ shader2DHQ = new ColorGrading2DHQ();
	
	enum Mode {
		texture3D, texture2D, texture2D_HQ
	}
	
	private Mode mode = Mode.texture3D;
	private boolean enabled = true;
	private Label lutFileLabel;
	
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		if(file.extension().toLowerCase().equals("cube")){
			
			lutFileLabel.setText(file.name());
			
			CubeData data = new CubeDataLoader().load(file);
			
			if(lut3D != null) lut3D.dispose();
			if(lut2D != null) lut2D.dispose();
			if(lutRGB2D != null){
				for(Texture t : lutRGB2D) t.dispose();
			}
			
			lut3D = ColorGrading.createLUT3D(data);
			lut2D = ColorGrading.createLUT2D(data);
			lutRGB2D = ColorGrading.createRGBLUT2D(data);
			return true;
		}
		return false;
	}

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Frame frame = UI.frameToggle("Color grading", skin, enabled, v->enabled=v);
		Table t = frame.getContentTable();
		t.add("LUT mode");
		t.add(UI.selector(skin, Mode.values(), mode, v->mode=v));
		t.row();
		
		t.add("LUT file");
		lutFileLabel = t.add("none").getActor();
		t.row();
		
		return frame;
	}
	
	public FrameBuffer render(GLTFComposerContext ctx, FrameBuffer inputFBO) {
		if(enabled){
			if(mode == Mode.texture3D && lut3D != null){
				shader3D.bind();
				shader3D.setLUT(lut3D);
				FrameBufferUtils.blit(ctx.batch, inputFBO.getColorBufferTexture(), inputFBO, shader3D);
			}
			else if(mode == Mode.texture2D && lut2D != null){
				shader2D.bind();
				shader2D.setLUT(lut2D);
				FrameBufferUtils.blit(ctx.batch, inputFBO.getColorBufferTexture(), inputFBO, shader2D);
			}
			else if(mode == Mode.texture2D_HQ && lutRGB2D != null){
				shader2DHQ.bind();
				shader2DHQ.setLUT(lutRGB2D);
				FrameBufferUtils.blit(ctx.batch, inputFBO.getColorBufferTexture(), inputFBO, shader2DHQ);
			}
		}
		return inputFBO;
	}
}
