package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Json;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.core.Composition;
import net.mgsx.gltf.composer.core.CompositionLoader;

public class FileModule implements GLTFComposerModule
{
	private Table controls;

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = UI.table(skin);
		controls.add("Drop a model file").row();
		controls.add("Supported files: *.gltf, *.glb").row();
		controls.add().padTop(50).row();
		controls.add("Drop an IBL file").row();
		controls.add("Supported files: *.hdr").row();
		controls.add().padTop(50).row();
		controls.add("Drop an Composition file").row();
		controls.add("Supported files: *.json").row();
		controls.add().padTop(50).row();
		
		
		controls.add(UI.trig(skin, "Save composition as...", ()->saveComposition(ctx)));
	
		return controls;
	}
	
	private void saveComposition(GLTFComposerContext ctx) {
		// copy some stuff
		ctx.compo.keyLight.set(ctx.keyLight);
		ctx.compo.camera.set(ctx.cameraManager.getPerspectiveCamera(), ctx.cameraManager.getPerspectiveTarget());
		
		ctx.fileSelector.save(file->saveComposition(ctx, file));
	}

	private void saveComposition(GLTFComposerContext ctx, FileHandle file){
		ctx.compo.file = file;
		
		// make all path relatives if possible
		String base = file.parent().path() + "/";
		ctx.compo.hdrPath = relativePath(base, ctx.compo.hdrPath);
		ctx.compo.envPath = relativePath(base, ctx.compo.envPath);
		ctx.compo.diffusePath = relativePath(base, ctx.compo.diffusePath);
		ctx.compo.specularPath = relativePath(base, ctx.compo.specularPath);
		for(int i=0 ; i<ctx.compo.scenesPath.size ; i++){
			ctx.compo.scenesPath.set(i, relativePath(base, ctx.compo.scenesPath.get(i)));
		}
		
		file.writeString(new Json().prettyPrint(ctx.compo), false);
	}
	
	private String relativePath(String base, String path){
		if(path != null){
			if(path.startsWith(base)){
				path = path.substring(base.length());
			}
		}
		return path;
	}
	
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		if(file.extension().toLowerCase().equals("json")){
			Composition compo = new CompositionLoader().load(file);
			
			ctx.setComposition(compo);
			
			return true;
		}
		return false;
	}
}
