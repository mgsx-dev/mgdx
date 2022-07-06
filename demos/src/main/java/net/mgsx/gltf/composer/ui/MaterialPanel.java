package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;

import net.mgsx.gdx.graphics.glutils.ColorUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.utils.UI;
import net.mgsx.gltf.composer.utils.UI.ControlScale;

public class MaterialPanel extends Table
{
	public MaterialPanel(GLTFComposerContext ctx, Material material) {
		super(ctx.skin);
		defaults().pad(UI.DEFAULT_PADDING);
		
		Table table = this;
		
		UI.header(table, "Material: " + material.id);
		
		// TODO more use case (textures, etc..)
		
		for(Attribute attribute : material){
			if(attribute instanceof FloatAttribute){
				FloatAttribute fa = (FloatAttribute)attribute;
				UI.slider(table, Attribute.getAttributeAlias(fa.type), 0, 1, fa.value, v->fa.value=v);
			}
			else if(attribute instanceof ColorAttribute){
				ColorAttribute fa = (ColorAttribute)attribute;
				boolean rgb = false;
				if(rgb){
					// RGB mode
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".r", 0, 1, fa.color.r, v->fa.color.r=v);
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".g", 0, 1, fa.color.g, v->fa.color.g=v);
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".b", 0, 1, fa.color.b, v->fa.color.b=v);
				}else{
					// HSV mode
					float [] hsv = new float[]{0,0,0,fa.color.a, 1};
					fa.color.toHsv(hsv);
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".h", 0, 360, hsv[0], v->{hsv[0]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".s", 0, 1, hsv[1], v->{hsv[1]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".v", 0, 1, hsv[2], v->{hsv[2]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".scale", 1e-3f, 1e3f, hsv[4], ControlScale.LOG, v->{hsv[4]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
				}
			}
			else if(attribute instanceof TextureAttribute){
				TextureAttribute ta = (TextureAttribute)attribute;
				Texture texture = ta.textureDescription.texture;
				
				Table t = new Table(getSkin());
				
				
				Image img = new Image(texture);
				img.setScaling(Scaling.fit);
				t.add(img).size(64);
				
				t.add(Attribute.getAttributeAlias(ta.type));
				
				// TODO will messup when ui is removed (lost attributes)
				t.add(UI.toggle(getSkin(), "enabled", true, v->{
					if(v) material.set(ta); else material.remove(ta.type);
				}));
				
				table.add(t).row();
				
			}
		}
	}
}
