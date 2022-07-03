package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.ibl.IBL;
import net.mgsx.ibl.IBL.IBLBakingOptions;

public class IBLModule implements GLTFComposerModule
{
	private class HDRBakingDialog extends Dialog
	{
		public HDRBakingDialog(GLTFComposerContext ctx, Runnable callback) {
			super("HDR baking options", ctx.skin);
			
			Table t = getContentTable();
			
			// from 1x1 to 4k
			Array<Integer> resolutions = new Array<Integer>();
			for(int i=0 ; i<=12 ; i++) resolutions.add(1 << i);
			
			t.add("Skybox map");
			t.add(UI.selector(getSkin(), resolutions, bakingOptions.envSize, v->v+"x"+v, v->bakingOptions.envSize=v));
			t.row();
			
			t.add("Radiance map");
			t.add(UI.selector(getSkin(), resolutions, bakingOptions.radSize, v->v+"x"+v, v->bakingOptions.radSize=v));
			t.row();
			
			t.add("Irradiance map");
			t.add(UI.selector(getSkin(), resolutions, bakingOptions.irdSize, v->v+"x"+v, v->bakingOptions.irdSize=v));
			t.row();
			
			Array<Integer> precisions = new Array<Integer>();
			precisions.add(16, 32);
			
			t.add("Precision");
			t.add(UI.selector(getSkin(), precisions, bakingOptions.format == GLFormat.RGB32 ? 32 : 16, v->v+" bits", v->bakingOptions.format=v==32 ? GLFormat.RGB32 : GLFormat.RGB16));
			t.row();
			
			t.add(UI.trig(getSkin(), "Bake", ()->{
				callback.run();
				remove();
			}));
			t.add(UI.trig(getSkin(), "Cancel", ()->{
				remove();
			}));
		}
	}
	
	// TODO env map only option
	private final IBLBakingOptions bakingOptions = new IBLBakingOptions();
	
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		String ext = file.extension().toLowerCase();
		// TODO exr format ?
		if(ext.equals("hdr")){
			HDRBakingDialog dialog = new HDRBakingDialog(ctx, ()->{
				// bake as IBL and apply
				if(ctx.ibl != null){
					ctx.ibl.dispose();
				}
				ctx.ibl = IBL.fromHDR(file, false);
				ctx.ibl.apply(ctx.sceneManager);
				if(ctx.skyBox == null){
					// TODO
					ctx.skyBox = new SceneSkybox(ctx.ibl.getEnvironmentCubemap(), ctx.colorShaderConfig.manualSRGB, ctx.colorShaderConfig.manualGammaCorrection);
					ctx.sceneManager.setSkyBox(ctx.skyBox);
				}else{
					ctx.skyBox.set(ctx.ibl.getEnvironmentCubemap());
				}
			});
			dialog.show(ctx.stage);
			return true;
		}
		// TODO case of folder, or KTX2 : choose
		// TODO allow multiple files (3 KTX)
		
		// TODO allow quick IBL !
		
		return false;
	}
}
