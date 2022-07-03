package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;
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
	private Table controls;
	
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		if(file.isDirectory()){
			// check for diffuse, environment and specular
			if(file.list().length == 3){
				FileHandle diffuse = file.child("diffuse");
				FileHandle environment = file.child("environment");
				FileHandle specular = file.child("specular");
				if(diffuse.exists() && environment.exists() && specular.exists()){
					IBL ibl = new IBL();
					ibl.load(file, "png"); // TODO could be others...
					replaceIBL(ctx, ibl);
					return true;
				}
			}
		}else{
			String ext = file.extension().toLowerCase();
			// TODO exr format ?
			if(ext.equals("hdr")){
				HDRBakingDialog dialog = new HDRBakingDialog(ctx, ()->{
					replaceIBL(ctx, IBL.fromHDR(file, bakingOptions, false));
				});
				dialog.show(ctx.stage);
				return true;
			}
			else if(ext.equals("ktx2")){
				// TODO ask which target
			}
			else if(ext.equals("exr")){
				// TODO ask which target
			}
		}
		
		
		return false;
	}

	private void replaceIBL(GLTFComposerContext ctx, IBL newIBL) {
		if(ctx.ibl != null){
			ctx.ibl.dispose();
		}
		ctx.ibl = newIBL;
		ctx.ibl.apply(ctx.sceneManager);
		if(ctx.skyBox == null){
			ctx.skyBox = new SceneSkybox(ctx.ibl.getEnvironmentCubemap(), ctx.colorShaderConfig.manualSRGB, ctx.colorShaderConfig.manualGammaCorrection);
			ctx.sceneManager.setSkyBox(ctx.skyBox);
		}else{
			ctx.skyBox.set(ctx.ibl.getEnvironmentCubemap());
		}
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = new Table(skin);
		
		Array<String> builtins = new Array<String>();
		builtins.add("None", "Outdoor", "Indoor");
		controls.add("Builtin IBL").row();
		controls.add(UI.selector(skin, builtins, builtins.first(), v->v, v->{
			int index = builtins.indexOf(v, false);
			IBLBuilder builder = null;
			if(index == 0){
				if(ctx.ibl != null){
					ctx.ibl.dispose();
				}
				ctx.ibl = null;
				if(ctx.skyBox != null){
					ctx.skyBox.dispose();
					ctx.skyBox = null;
					ctx.sceneManager.setSkyBox(null);
				}
				IBL.remove(ctx.sceneManager);
			}
			else if(index == 1){
				builder = IBLBuilder.createOutdoor(ctx.keyLight);
			}else if(index == 2){
				builder = IBLBuilder.createIndoor(ctx.keyLight);
			}
			if(builder != null){
				IBL ibl = new IBL();
				ibl.environmentCubemap = builder.buildEnvMap(bakingOptions.envSize);
				ibl.specularCubemap = builder.buildRadianceMap(9); // TODO size to mips
				ibl.diffuseCubemap = builder.buildIrradianceMap(bakingOptions.irdSize);
				ibl.loadDefaultLUT();
				builder.dispose();
				
				replaceIBL(ctx, ibl);
			}
		})).row();
		
		controls.add().padTop(50).row();
		controls.add("Drop an IBL file").row();
		controls.add("Supported files: *.hdr").row();
		
		return controls;
	}
}
