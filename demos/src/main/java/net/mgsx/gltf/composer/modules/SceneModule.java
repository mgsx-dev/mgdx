package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.files.FileHandle;

import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.ComposerUtils;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.exceptions.GLTFRuntimeException;
import net.mgsx.gltf.loaders.exceptions.GLTFUnsupportedException;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class SceneModule implements GLTFComposerModule
{
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		String ext = file.extension().toLowerCase();
		SceneAsset newAsset = null;
		try{
			if(ext.equals("gltf")){
				newAsset = new GLTFLoader().load(file);
			}
			else if(ext.equals("glb")){
				newAsset = new GLBLoader().load(file);
			}
		}catch(GLTFUnsupportedException e){
			UI.popup(ctx.stage, ctx.skin, "Not supported", e.getMessage());
			return true;
		}catch(GLTFIllegalException e){
			UI.popup(ctx.stage, ctx.skin, "Invalid error", e.getMessage());
			return true;
		}catch(GLTFRuntimeException e){
			UI.popup(ctx.stage, ctx.skin, "Unexpected error", e.getMessage());
			return true;
		}
		if(newAsset != null){
			if(ctx.scene != null) ctx.sceneManager.removeScene(ctx.scene);
			if(ctx.asset != null){
				ctx.asset.dispose();
			}
			ctx.asset = newAsset;
			ctx.scene = new Scene(ctx.asset.scene);
			ctx.sceneManager.addScene(ctx.scene);
			
			// compute boundary box
			ctx.scene.modelInstance.calculateBoundingBox(ctx.sceneBounds);
			ComposerUtils.fitCameraToScene(ctx);
			
			ctx.sceneJustChanged = true;
			ctx.invalidateShaders();
			return true;
		}
		return false;
	}
}
