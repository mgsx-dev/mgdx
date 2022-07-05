package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.ibl.io.AWTFileSelector;
import net.mgsx.gltf.ibl.io.FileSelector;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;
import net.mgsx.ibl.IBL;
import net.mgsx.ibl.IBL.IBLBakingOptions;
import net.mgsx.ktx2.KTX2TextureData;

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
	
	private static class Exporter {
		String name;
		Runnable callback;
		public Exporter(String name, Runnable callback) {
			super();
			this.name = name;
			this.callback = callback;
		}
	}
	private class HDRExportDialog extends Dialog {
		public HDRExportDialog(GLTFComposerContext ctx, Cubemap map, boolean mipmaps, Runnable callback) {
			super("HDR export options", ctx.skin);
			Array<Exporter> formats = new Array<Exporter>();
			formats.add(new Exporter("ktx2", ()->{
				fileSelector.save(file->{
					IBL.exportToKtx2(map, file, mipmaps, GLFormat.RGB16, true);
				});
			}));
			
			Table t = getContentTable();
			SelectBox<Exporter> selector = UI.selector(ctx.skin, formats, formats.first(), f->f.name, f->{});
			t.add(selector).row();
			
			t.add(UI.trig(getSkin(), "Export", ()->{
				selector.getSelected().callback.run();
				remove();
			})).row();
		}
	}
	
	private class ImportDialog extends Dialog
	{
		public ImportDialog(GLTFComposerContext ctx, Cubemap map) {
			super("Import cube map", ctx.skin);
			Table t = getContentTable();
			t.add(UI.trig(getSkin(), "import as environment map (skybox)", ()->{
				if(ctx.ibl == null){
					ctx.ibl = new IBL();
					ctx.ibl.loadDefaultLUT();
				}
				if(ctx.ibl.environmentCubemap != null){
					ctx.ibl.environmentCubemap.dispose();
				}
				ctx.ibl.environmentCubemap = map;
				ctx.ibl.environmentCubemap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
				applyIBL(ctx);
				remove();
			})).row();
			
			t.add(UI.trig(getSkin(), "import as radiance map (specular)", ()->{
				if(ctx.ibl == null){
					ctx.ibl = new IBL();
					ctx.ibl.loadDefaultLUT();
				}
				if(ctx.ibl.specularCubemap != null){
					ctx.ibl.specularCubemap.dispose();
				}
				ctx.ibl.specularCubemap = map;
				// TODO should be auto set somewhere
				ctx.ibl.specularCubemap.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
				applyIBL(ctx);
				remove();
			})).row();
			
			t.add(UI.trig(getSkin(), "import as irradiance map (diffuse)", ()->{
				if(ctx.ibl == null){
					ctx.ibl = new IBL();
					ctx.ibl.loadDefaultLUT();
				}
				if(ctx.ibl.diffuseCubemap != null){
					ctx.ibl.diffuseCubemap.dispose();
				}
				ctx.ibl.diffuseCubemap = map;
				ctx.ibl.diffuseCubemap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
				applyIBL(ctx);
				remove();
			})).row();
		}
	}
	
	// TODO env map only option
	private final IBLBakingOptions bakingOptions = new IBLBakingOptions();
	private Table controls;
	private FileSelector fileSelector = new AWTFileSelector();
	
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
				// ask which target
				KTX2TextureData data = new KTX2TextureData(file);
				data.prepare();
				if(data.getTarget() == GL30.GL_TEXTURE_CUBE_MAP){
					new ImportDialog(ctx, new Cubemap(data)).show(ctx.stage);
					return true;
				}
				// TODO return true and display an error popup : cubemap expected...
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
		applyIBL(ctx);
	}
	private void applyIBL(GLTFComposerContext ctx){
		ctx.ibl.apply(ctx.sceneManager);
		if(ctx.ibl.environmentCubemap != null){
			if(ctx.skyBox == null){
				ctx.skyBox = new SceneSkybox(ctx.ibl.getEnvironmentCubemap(), ctx.colorShaderConfig.manualSRGB, ctx.colorShaderConfig.manualGammaCorrection);
				ctx.sceneManager.setSkyBox(ctx.skyBox);
			}else{
				ctx.skyBox.set(ctx.ibl.getEnvironmentCubemap());
			}
		}else{
			if(ctx.skyBox != null){
				ctx.sceneManager.setSkyBox(null);
				ctx.skyBox.dispose();
			}
		}
		ctx.invalidateShaders();
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
		controls.add("Supported files: *.hdr, png folder").row();
		
		controls.add(UI.trig(skin, "Export environment map (skybox)", ()->{
			new HDRExportDialog(ctx, ctx.ibl.environmentCubemap, false, ()->{}).show(ctx.stage);
		})).row();
		
		controls.add(UI.trig(skin, "Export radiance map (specular)", ()->{
			new HDRExportDialog(ctx, ctx.ibl.specularCubemap, true, ()->{}).show(ctx.stage);
		})).row();
		
		controls.add(UI.trig(skin, "Export irradiance map (diffuse)", ()->{
			new HDRExportDialog(ctx, ctx.ibl.diffuseCubemap, false, ()->{}).show(ctx.stage);
		})).row();
		
		return controls;
	}
}
