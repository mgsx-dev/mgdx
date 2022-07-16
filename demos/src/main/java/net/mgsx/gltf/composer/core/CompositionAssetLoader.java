package net.mgsx.gltf.composer.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.ibl.IBL;

public class CompositionAssetLoader extends AsynchronousAssetLoader<Composition, AssetLoaderParameters<Composition>>{

	public CompositionAssetLoader() {
		super(new InternalFileHandleResolver());
	}
	public CompositionAssetLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	private Composition compo;

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file,
			AssetLoaderParameters<Composition> parameter) {
		
	}

	@Override
	public Composition loadSync(AssetManager manager, String fileName, FileHandle file,
			AssetLoaderParameters<Composition> parameter) {
		
		for(String path : compo.scenesPath){
			AssetDescriptor<SceneAsset> desc = new AssetDescriptor<SceneAsset>(sceneFile(file, path), SceneAsset.class);
			SceneAsset sceneAsset = manager.get(desc);
			compo.sceneAssets.add(sceneAsset);
		}
		
		if(compo.hdrPath != null){
			// load and bake
			compo.ibl = IBL.fromHDR(compo.hdrPath.startsWith("/") ? Gdx.files.absolute(compo.hdrPath) : file.sibling(compo.hdrPath), compo.iblBaking, false);
		}
		
		Composition c = compo;
		compo = null;
		return c;
	}

	private FileHandle sceneFile(FileHandle file, String path) {
		if(path.startsWith("/")){
			return Gdx.files.absolute(path);
		}else{
			return file.sibling(path);
		}
	}
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file,
			AssetLoaderParameters<Composition> parameter) {
		compo = new Json().fromJson(Composition.class, file);
		Array<AssetDescriptor> deps = new Array<AssetDescriptor>();
		for(String path : compo.scenesPath){
			AssetDescriptor desc = new AssetDescriptor<>(sceneFile(file, path), SceneAsset.class);
			deps.add(desc);
		}
		return deps;
	}

}
