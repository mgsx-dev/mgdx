package net.mgsx.gltf.composer.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.ibl.IBL;

public class CompositionLoader {

	public Composition load(FileHandle file, boolean withGltfData){
		Composition compo = new Json().fromJson(Composition.class, file);
		compo.file = file;
		for(String path : compo.scenesPath){
			SceneAsset sceneAsset = new GLTFLoader().load(path.startsWith("/") ? Gdx.files.absolute(path) : file.sibling(path), withGltfData);
			compo.sceneAssets.add(sceneAsset);
		}
		if(compo.envPath != null && compo.diffusePath != null && compo.specularPath != null){
			compo.ibl = IBL.load(file.sibling(compo.envPath), file.sibling(compo.diffusePath), file.sibling(compo.specularPath));
		}
		else if(compo.hdrPath != null){
			// load and bake
			compo.ibl = IBL.fromHDR(compo.hdrPath.startsWith("/") ? Gdx.files.absolute(compo.hdrPath) : file.sibling(compo.hdrPath), compo.iblBaking, false);
		}
		return compo;
	}
}
