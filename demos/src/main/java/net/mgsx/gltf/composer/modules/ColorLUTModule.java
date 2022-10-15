package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltfx.gdx.Texture3D;
import net.mgsx.gltfx.lut.ColorGrading;
import net.mgsx.gltfx.lut.CubeData;
import net.mgsx.gltfx.lut.CubeDataLoader;
import net.mgsx.gltfx.lut.ColorGrading.LUTFormat;
import net.mgsx.gltfx.lut.ColorGradingShaders.ColorGrading2D;
import net.mgsx.gltfx.lut.ColorGradingShaders.ColorGrading2DHQ;
import net.mgsx.gltfx.lut.ColorGradingShaders.ColorGrading3D;

public class ColorLUTModule implements GLTFComposerModule
{
	private Texture3D lut3D;
	private Texture lut2D;
	private Texture[] lutRGB2D;

	private ColorGrading3D shader3D = new ColorGrading3D();
	private ColorGrading2D shader2D = new ColorGrading2D(false);
	private ColorGrading2D shaderTri2D = new ColorGrading2D(true);
	private ColorGrading2DHQ shader2DHQ = new ColorGrading2DHQ();
	
	private Texture refrenceImage;
	
	enum Mode {
		texture3D, textureLin2D, textureTri2D, textureLin2D_HQ
	}
	
	private Mode mode = Mode.texture3D;
	private boolean enabled = true;
	private Label lutFileLabel;
	
	private static class LUTGenerateDialog extends Dialog{
		public LUTGenerateDialog(GLTFComposerContext ctx) {
			super("LUT generate", ctx.skin, "dialog");
			Table t = getContentTable();
			t.defaults().pad(UI.DEFAULT_PADDING);
			
			SelectBox<String> format = UI.selector(getSkin(), "32 (1024x32)", "64 (512x512)");
			t.add(format).row();
			
			t.add(UI.trig(getSkin(), "export", ()->{
				ctx.fileSelector.save(file->{
					Pixmap pixmap = format.getSelectedIndex() == 1 ?
							ColorGrading.createNeutralLUT(64, LUTFormat.GRID) :
							ColorGrading.createNeutralLUT(32, LUTFormat.ROW);
					PixmapIO.writePNG(file, pixmap);
					pixmap.dispose();
				}, "lut-neutral" + (format.getSelectedIndex() == 1 ? "-64" : "-32")  + ".png", "png");
				hide();
			}));
			
		}
	}
	
	private class LUTImportDialog extends Dialog{
		public LUTImportDialog(GLTFComposerContext ctx, FileHandle file) {
			super("LUT import", ctx.skin, "dialog");
			Table t = getContentTable();
			t.defaults().pad(UI.DEFAULT_PADDING);
			
			t.add(UI.trig(getSkin(), "as LUT", ()->{
				loadPNGLUT(file);
				hide();
			})).row();
			
			t.add(UI.trig(getSkin(), "as reference", ()->{
				if(refrenceImage != null) refrenceImage.dispose();
				refrenceImage = new Texture(file);
				hide();
			})).row();
			
		}
	}
	
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		if(file.extension().toLowerCase().equals("cube")){
			
			lutFileLabel.setText(file.name());
			
			CubeData data = new CubeDataLoader().load(file);
			
			disposeLUTS();
			
			lut3D = ColorGrading.createLUT3D(data);
			lut2D = ColorGrading.createLUT2D(data);
			lutRGB2D = ColorGrading.createRGBLUT2D(data);
			return true;
		}
		else if(file.extension().toLowerCase().equals("png")){
			
			// TODO see if we keep that for the editor
			boolean allowReferenceImage = false;
			if(allowReferenceImage){
				new LUTImportDialog(ctx, file).show(ctx.stage);
			}else{
				loadPNGLUT(file);
			}
			
			return true;
		}
		return false;
	}
	
	private void loadPNGLUT(FileHandle file) {
		lutFileLabel.setText(file.name());
		disposeLUTS();

		// TODO options / config ?
		Pixmap pixmap = new Pixmap(file);
		
		lut2D = new Texture(pixmap);
		lut2D.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		lut3D = ColorGrading.createLUT3D(pixmap);
		lutRGB2D = ColorGrading.createRGBLUT2D(pixmap);
	}

	private void disposeLUTS(){
		if(lut3D != null) lut3D.dispose();
		if(lut2D != null) lut2D.dispose();
		if(lutRGB2D != null){
			for(Texture t : lutRGB2D) t.dispose();
		}
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
		
		// TODO options ?
		t.add(UI.trig(skin, "generate neutral LUT", ()->{
			new LUTGenerateDialog(ctx).show(ctx.stage);
		})).colspan(2).row();
		
		return frame;
	}
	
	public FrameBuffer render(GLTFComposerContext ctx, FrameBuffer inputFBO) {
		if(enabled){
			Texture inputTexture = refrenceImage != null ? refrenceImage : inputFBO.getColorBufferTexture();
			
			if(mode == Mode.texture3D && lut3D != null){
				shader3D.bind();
				shader3D.setLUT(lut3D);
				FrameBufferUtils.blit(ctx.batch, inputTexture, inputFBO, shader3D);
			}
			else if(mode == Mode.textureLin2D && lut2D != null){
				shader2D.bind();
				shader2D.setLUT(lut2D);
				FrameBufferUtils.blit(ctx.batch, inputTexture, inputFBO, shader2D);
			}
			else if(mode == Mode.textureTri2D && lut2D != null){
				shaderTri2D.bind();
				shaderTri2D.setLUT(lut2D);
				FrameBufferUtils.blit(ctx.batch, inputTexture, inputFBO, shaderTri2D);
			}
			else if(mode == Mode.textureLin2D_HQ && lutRGB2D != null){
				shader2DHQ.bind();
				shader2DHQ.setLUT(lutRGB2D);
				FrameBufferUtils.blit(ctx.batch, inputTexture, inputFBO, shader2DHQ);
			}
		}
		return inputFBO;
	}
}
