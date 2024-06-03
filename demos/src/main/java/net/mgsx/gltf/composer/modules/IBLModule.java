package net.mgsx.gltf.composer.modules;

import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
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

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.ComposerUtils;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;
import net.mgsx.gltfx.GLFormat;
import net.mgsx.gltfx.ktx2.KTX2TextureData;
import net.mgsx.ibl.IBL;

public class IBLModule implements GLTFComposerModule
{
	private class HDRBakingDialog extends Dialog
	{
		public HDRBakingDialog(GLTFComposerContext ctx, Runnable callback) {
			super("HDR baking options", ctx.skin, "dialog");
			
			Table t = getContentTable();
			t.defaults().pad(UI.DEFAULT_PADDING);
			
			// from 1x1 to 4k
			Array<Integer> resolutions = new Array<Integer>();
			for(int i=0 ; i<=12 ; i++) resolutions.add(1 << i);
			
			t.add("Skybox map");
			t.add(UI.selector(getSkin(), resolutions, ctx.compo.iblBaking.envSize, v->v+"x"+v, v->ctx.compo.iblBaking.envSize=v));
			t.row();
			
			t.add("Radiance map");
			t.add(UI.selector(getSkin(), resolutions, ctx.compo.iblBaking.radSize, v->v+"x"+v, v->ctx.compo.iblBaking.radSize=v));
			t.row();
			
			t.add("Irradiance map");
			t.add(UI.selector(getSkin(), resolutions, ctx.compo.iblBaking.irdSize, v->v+"x"+v, v->ctx.compo.iblBaking.irdSize=v));
			t.row();
			
			Array<Integer> precisions = new Array<Integer>();
			precisions.add(16, 32);
			
			t.add("Precision");
			t.add(UI.selector(getSkin(), precisions, ctx.compo.iblBaking.format == GLFormat.RGB32 ? 32 : 16, v->v+" bits", v->ctx.compo.iblBaking.format=v==32 ? GLFormat.RGB32 : GLFormat.RGB16));
			t.row();
			
			t.add(UI.primary(getSkin(), "Bake", ()->{
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
		public HDRExportDialog(GLTFComposerContext ctx, Cubemap map, boolean mipmaps, String defaultName, Consumer<FileHandle> callback) {
			super("HDR export options", ctx.skin, "dialog");
			Array<Exporter> formats = new Array<Exporter>();
			formats.add(new Exporter("ktx2", ()->{
				ctx.fileSelector.save(file->{
					IBL.exportToKtx2(map, file, mipmaps, GLFormat.RGB16, true);
					callback.accept(file);
				}, defaultName + ".ktx2", "ktx2");
			}));
			formats.add(new Exporter("png", ()->{
				ctx.fileSelector.selectFolder(file->{
					IBL.exportToPngs(map, file, mipmaps);
					callback.accept(file);
				});
			}));
			
			Table t = getContentTable();
			t.defaults().pad(UI.DEFAULT_PADDING);
			
			SelectBox<Exporter> selector = UI.selector(ctx.skin, formats, formats.first(), f->f.name, f->{});
			t.add(selector).row();
			
			t.add(UI.trig(getSkin(), "Export", ()->{
				selector.getSelected().callback.run();
				remove();
			})).row();
		}
	}
	
	private class HDRExportAllDialog extends Dialog {
		public HDRExportAllDialog(GLTFComposerContext ctx) {
			super("HDR export options", ctx.skin, "dialog");
			Array<Exporter> formats = new Array<Exporter>();
			formats.add(new Exporter("ktx2", ()->{
				ctx.fileSelector.save(file->{
					FileHandle envFile = file.sibling(file.nameWithoutExtension() + "-env.ktx2");
					FileHandle difFile = file.sibling(file.nameWithoutExtension() + "-dif.ktx2");
					FileHandle speFile = file.sibling(file.nameWithoutExtension() + "-spe.ktx2");
					
					IBL.exportToKtx2(ctx.ibl.environmentCubemap, envFile, false, GLFormat.RGB16, true);
					IBL.exportToKtx2(ctx.ibl.diffuseCubemap, difFile, false, GLFormat.RGB16, true);
					IBL.exportToKtx2(ctx.ibl.specularCubemap, speFile, true, GLFormat.RGB16, true);
					
					ctx.compo.envPath = envFile.path();
					ctx.compo.diffusePath = difFile.path();
					ctx.compo.specularPath = speFile.path();
				}, "ibl");
			}));
			
			formats.add(new Exporter("png", ()->{
				ctx.fileSelector.selectFolder(file->{
					FileHandle envFile = file.child("environement");
					FileHandle difFile = file.child("diffuse");
					FileHandle speFile = file.child("specular");
					
					IBL.exportToPngs(ctx.ibl.environmentCubemap, envFile, false);
					IBL.exportToPngs(ctx.ibl.diffuseCubemap, difFile, false);
					IBL.exportToPngs(ctx.ibl.specularCubemap, speFile, true);
					
					ctx.compo.envPath = envFile.path();
					ctx.compo.diffusePath = difFile.path();
					ctx.compo.specularPath = speFile.path();
				});
			}));
			
			Table t = getContentTable();
			t.defaults().pad(UI.DEFAULT_PADDING);
			
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
		public ImportDialog(GLTFComposerContext ctx, FileHandle file, Cubemap map) {
			super("Import cube map", ctx.skin, "dialog");
			Table t = getContentTable();
			t.defaults().pad(UI.DEFAULT_PADDING);
			
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
				ctx.applyIBL();
				
				ctx.compo.envPath = file.path();
				
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
				ctx.ibl.specularCubemap.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
				ctx.applyIBL();
				
				ctx.compo.specularPath = file.path();
				
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
				ctx.applyIBL();
				
				ctx.compo.diffusePath = file.path();
				
				remove();
			})).row();
		}
	}
	
	// TODO env map only option
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
				ctx.compo.hdrPath = file.path();
				HDRBakingDialog dialog = new HDRBakingDialog(ctx, ()->{
					replaceIBL(ctx, IBL.fromHDR(file, ctx.compo.iblBaking, false));
				});
				dialog.show(ctx.stage);
				return true;
			}
			else if(ext.equals("ktx2")){
				// ask which target
				KTX2TextureData data = new KTX2TextureData(file);
				data.prepare();
				if(data.getTarget() == GL30.GL_TEXTURE_CUBE_MAP){
					new ImportDialog(ctx, file, new Cubemap(data)).show(ctx.stage);
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
	
	
	private int builtInIndex;
	private FogModule fogModule = new FogModule();
	private MirrorModule mirrorModule = new MirrorModule();

	private void replaceIBL(GLTFComposerContext ctx, IBL newIBL) {
		if(ctx.ibl != null){
			ctx.ibl.dispose();
		}
		ctx.ibl = newIBL;
		ctx.applyIBL();
		updateUI(ctx, ctx.skin);
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = UI.table(skin);
		
		updateUI(ctx, skin);
		
		return controls;
	}
	
	private void updateUI(GLTFComposerContext ctx, Skin skin)
	{
		controls.clear();
		controls.defaults().fill();
		
		UI.header(controls, "Environment");
		
		Frame wdFrame = UI.frame("Background", skin);
		Table wdTable = wdFrame.getContentTable();
		controls.add(wdFrame).growX().row();

		UI.colorBox(wdTable, "color", ctx.compo.clearColor, false);
		
		
		Frame sbFrame = UI.frameToggle("Skybox", skin, true, v->ComposerUtils.enabledSkybox(ctx, v));
		Table sbTable = sbFrame.getContentTable();
		UI.slider(sbTable, "rotation", 0, 360, ctx.compo.envRotation, v->ctx.compo.envRotation = v);
		UI.slider(sbTable, "blur", -10, 10, ctx.compo.skyboxBlur, v->ctx.compo.skyboxBlur=v);
		UI.slider(sbTable, "Opacity", 0, 1, ctx.compo.skyBoxColor.a, value->ComposerUtils.setSkyboxOpacity(ctx, value));
		controls.add(sbFrame).growX().row();
		
		controls.add(fogModule.initUI(ctx, skin)).row();

		controls.add(mirrorModule.initUI(ctx, skin)).row();
		
		// IBL
		
		UI.header(controls, "IBL");
		
		Array<String> builtins = new Array<String>();
		builtins.add("None", "Outdoor", "Indoor");
		Table tBuiltin = UI.table(skin);
		controls.add(tBuiltin).row();
		tBuiltin.add("Builtin IBL").row();
		tBuiltin.add(UI.selector(skin, builtins, builtins.get(builtInIndex), v->v, v->{
			builtInIndex = builtins.indexOf(v, false);
			IBLBuilder builder = null;
			if(builtInIndex == 0){
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
			else if(builtInIndex == 1){
				builder = IBLBuilder.createOutdoor(ctx.keyLight);
			}else if(builtInIndex == 2){
				builder = IBLBuilder.createIndoor(ctx.keyLight);
			}
			if(builder != null){
				IBL ibl = new IBL();
				ibl.environmentCubemap = builder.buildEnvMap(ctx.compo.iblBaking.envSize);
				ibl.specularCubemap = builder.buildRadianceMap(9); // TODO size to mips
				ibl.diffuseCubemap = builder.buildIrradianceMap(ctx.compo.iblBaking.irdSize);
				ibl.loadDefaultLUT();
				builder.dispose();
				
				replaceIBL(ctx, ibl);
			}else{
				updateUI(ctx, ctx.skin);
			}
		})).row();
		
		if(ctx.compo.hdrPath != null){
			Frame bakeFrame = UI.frame("Baking", skin);
			Table bakeTable = bakeFrame.getContentTable();
			controls.add(bakeFrame).row();
			bakeTable.add(UI.trig(skin, "Bake again", ()->{
				HDRBakingDialog dialog = new HDRBakingDialog(ctx, ()->{
					FileHandle file = ctx.compo.hdrPath.startsWith("/") ? Gdx.files.absolute(ctx.compo.hdrPath) : ctx.compo.file.sibling(ctx.compo.hdrPath);
					replaceIBL(ctx, IBL.fromHDR(file, ctx.compo.iblBaking, false));
				});
				dialog.show(ctx.stage);
			})).row();
		}else{
			controls.add("Drop IBL files : .hdr, .ktx2, .png folder").fill(false).row();
		}

		if(ctx.ibl != null && (ctx.ibl.environmentCubemap != null || ctx.ibl.diffuseCubemap != null || ctx.ibl.specularCubemap != null)){
			
			Frame exportFrame = UI.frame("Export", skin);
			Table exportTable = exportFrame.getContentTable();
			controls.add(exportFrame).row();
			
//		controls.add().padTop(50).row();
//		controls.add("Drop an IBL file").row();
//		controls.add("Supported files: *.hdr, png folder").row();
			
			exportTable.add(UI.trig(skin, "Export all", ()->{
				new HDRExportAllDialog(ctx).show(ctx.stage);
			})).row();
			
			exportTable.add(UI.trig(skin, "Export environment map (skybox)", ()->{
				new HDRExportDialog(ctx, ctx.ibl.environmentCubemap, false, "environment", file->ctx.compo.envPath=file.path()).show(ctx.stage);
			})).row();
			
			exportTable.add(UI.trig(skin, "Export radiance map (specular)", ()->{
				new HDRExportDialog(ctx, ctx.ibl.specularCubemap, true, "specular", file->ctx.compo.specularPath=file.path()).show(ctx.stage);
			})).row();
			
			exportTable.add(UI.trig(skin, "Export irradiance map (diffuse)", ()->{
				new HDRExportDialog(ctx, ctx.ibl.diffuseCubemap, false, "diffuse", file->ctx.compo.diffusePath=file.path()).show(ctx.stage);
			})).row();
		}
		
		
		
	}
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		if(ctx.compositionJustChanged){ // TODO or IBL changed !
			updateUI(ctx, ctx.skin);
		}
		ctx.sceneManager.setEnvironmentRotation(ctx.compo.envRotation);
		if(ctx.skyBox != null){
			ctx.skyBox.lodBias = ctx.compo.skyboxBlur;
		}
	}
}
