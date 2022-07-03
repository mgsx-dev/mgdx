package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.files.FileHandle;

import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;

public class SceneModule implements GLTFComposerModule
{
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		if(file.extension().equalsIgnoreCase("gltf")){
			if(ctx.scene != null) ctx.sceneManager.removeScene(ctx.scene);
			if(ctx.asset != null){
				ctx.asset.dispose();
			}
			ctx.asset = new GLTFLoader().load(file);
			ctx.scene = new Scene(ctx.asset.scene);
			ctx.sceneManager.addScene(ctx.scene);
			ctx.sceneJustChanged = true;
			ctx.invalidateShaders();
			return true;
		}
		return false;
	}
}
