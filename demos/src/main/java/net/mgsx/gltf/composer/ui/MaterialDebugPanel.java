package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gltf.composer.GLTFComposerContext;

public class MaterialDebugPanel extends Table
{
	public MaterialDebugPanel(GLTFComposerContext ctx, Material material) {
		super(ctx.skin);
		
		for(int i=0 ; i<64 ; i++){
			long type = 1L << i;
			
			String alias = Attribute.getAttributeAlias(type);
			
			if(alias == null) continue;
			
			add("Flag " + i);
			add(alias);
			add(material.has(type) ? "yes" : "no");
			row();
			
		}
		
	}
}
