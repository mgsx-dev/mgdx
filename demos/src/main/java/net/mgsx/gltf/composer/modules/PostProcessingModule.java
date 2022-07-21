package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class PostProcessingModule implements GLTFComposerModule
{
	private FXAAModule fxaaModule = new FXAAModule();
	private AntialiasModule antialiasModule = new AntialiasModule();
	private ColorLUTModule colorLUT = new ColorLUTModule();

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table table = UI.table(skin);
		table.defaults().growX();
		
		UI.header(table, "Frame buffer");
		table.add(antialiasModule.initUI(ctx, skin)).row();
		
		UI.header(table, "Post processing");
		
		table.add(colorLUT.initUI(ctx, skin)).row();
		table.add(fxaaModule.initUI(ctx, skin)).row();
		return table;
	}
	
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		return colorLUT.handleFile(ctx, file);
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		
		FrameBuffer lastFBO = ctx.ldrFbo;
		
		lastFBO = fxaaModule.render(ctx, lastFBO);
		
		lastFBO = colorLUT.render(ctx, lastFBO);
		
		Texture texture = lastFBO.getColorBufferTexture();
		if(ctx.pixelZoom > 1){
			float rate = ctx.pixelZoom;
			float width = 1f / rate;
			float offset = 0.5f - width / 2;
			ctx.batch.disableBlending();
			FrameBufferUtils.blit(ctx.batch, texture, offset, offset, width, width);
			ctx.batch.enableBlending();
		}else{
			ctx.batch.disableBlending();
			FrameBufferUtils.blit(ctx.batch, texture);
			ctx.batch.enableBlending();
			
			// render overlay
			ctx.overlay.render(ctx.shapes, ctx.cameraManager.getCamera(), ctx.cameraManager.getPerspectiveTarget());
		}
	}
}
