package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class AssetPanel extends Table {
	
	private ModelCache mc;
	
	public AssetPanel(GLTFComposerContext ctx, SceneAsset asset) {
		super(ctx.skin);
		defaults().pad(UI.DEFAULT_PADDING).left();
		
		Table table = this;
		
		if(asset.data != null){
			UI.header(table, "glTF Infos");

			table.add().width(300).row();
			table.add("glTF Version: " + asset.data.asset.version).row();
			if(asset.data.asset.minVersion != null){
				table.add("Min version: " + asset.data.asset.minVersion).row();
			}
			if(asset.data.asset.copyright != null){
				table.add(CUI.wrappedLabel(ctx.skin, "Copyright: " + asset.data.asset.copyright)).growX().row();
			}
			if(asset.data.asset.generator != null){
				table.add(CUI.wrappedLabel(ctx.skin, "Generator: " + asset.data.asset.generator)).growX().row();
			}
			
			UI.header(table, "glTF Extensions");
			boolean hasExts = false;
			if(asset.data.extensionsUsed != null){
				// TODO find which are supported by the libray and those not :
				// GLTFExtensions.isSupported(ext) :
				// also used by the lib itself, to either throw exception or log some warnings
				for(String ext : asset.data.extensionsUsed){
					if(asset.data.extensionsRequired != null && asset.data.extensionsRequired.contains(ext, false)){
						table.add(ext + " *");
					}else{
						table.add(ext);
					}
					table.row();
					hasExts = true;
				}
			}
			if(!hasExts){
				table.add("no extension").row();
			}
			
		}
		
		UI.toggle(table, "Use model cache", mc != null, on->{
			
			if(on){
				
				mc = new ModelCache();
				mc.begin();
				mc.add(ctx.scene.modelInstance);
				mc.end();
				
				Array<Renderable> renderables = new Array<Renderable>();
				mc.getRenderables(renderables, new FlushablePool<Renderable>() {
					@Override
					protected Renderable newObject () {
						return new Renderable();
					}
				});
				
				for(Renderable r : renderables){
					System.out.println(r.material);
				}
				ctx.sceneManager.getRenderableProviders().clear();
				ctx.sceneManager.getRenderableProviders().add(mc);
			}else{
				if(mc != null) mc.dispose();
				
				ctx.sceneManager.getRenderableProviders().clear();
				ctx.sceneManager.getRenderableProviders().add(ctx.scene);
			}
			
		});
		
	}
}
