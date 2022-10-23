package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;

public abstract class MaterialPanelBase extends Table
{
	public MaterialPanelBase(GLTFComposerContext ctx, Material material) {
		super(ctx.skin);
	}
	
	protected Actor createTexture(Material material, long type){
		TextureAttribute ta = material.get(TextureAttribute.class, type);
		Texture texture = ta.textureDescription.texture;
		
		Table t = new Table(getSkin());
		
		// TODO will messup when ui is removed (lost attributes)
		t.add(UI.toggle(getSkin(), Attribute.getAttributeAlias(ta.type), true, v->{
			if(v) material.set(ta); else material.remove(ta.type);
		}));
		
		t.row();
		
		Image img = new Image(texture);
		img.setScaling(Scaling.fit);
		t.add(img).size(64);
		
		return t;
	}
	
}
