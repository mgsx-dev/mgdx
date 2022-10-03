package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class AssetPanel extends Table {
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
		
	}
}
