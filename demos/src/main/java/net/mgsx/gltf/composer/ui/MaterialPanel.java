package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Scaling;

import net.mgsx.gdx.graphics.glutils.ColorUtils;
import net.mgsx.gdx.scenes.scene2d.ui.ColorBox;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;

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
				boolean ext = true;
				if(rgb){
					// RGB mode
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".r", 0, 1, fa.color.r, v->fa.color.r=v);
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".g", 0, 1, fa.color.g, v->fa.color.g=v);
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".b", 0, 1, fa.color.b, v->fa.color.b=v);
				}else if(!ext){
					// HSV mode
					float [] hsv = new float[]{0,0,0,fa.color.a, 1};
					fa.color.toHsv(hsv);
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".h", 0, 360, hsv[0], v->{hsv[0]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".s", 0, 1, hsv[1], v->{hsv[1]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".v", 0, 1, hsv[2], v->{hsv[2]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
					UI.slider(table, Attribute.getAttributeAlias(fa.type) + ".scale", 1e-3f, 1e3f, hsv[4], ControlScale.LOG, v->{hsv[4]=v; ColorUtils.hdrScale(fa.color.fromHsv(hsv), hsv[4]);});
				}else{
					Table t = new Table(getSkin());
					t.add(Attribute.getAttributeAlias(fa.type)).minWidth(100);
					t.defaults().padLeft(UI.DEFAULT_PADDING);
					
					t.add(new ColorBox(Attribute.getAttributeAlias(fa.type), fa.color, true, getSkin()));
					
					table.add(t).row();
				}
			}
			else if(attribute instanceof TextureAttribute){
				TextureAttribute ta = (TextureAttribute)attribute;
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
				
				table.add(t).row();
			}
			else if(attribute instanceof IntAttribute){
				IntAttribute ia = (IntAttribute)attribute;
				
				Table t = new Table(getSkin());
				t.add(Attribute.getAttributeAlias(ia.type));
				
				// TODO static
				IntMap<String> cullingMap = new IntMap<String>();
				cullingMap.put(GL20.GL_FRONT, "GL_FRONT");
				cullingMap.put(GL20.GL_BACK, "GL_BACK");
				cullingMap.put(GL20.GL_FRONT_AND_BACK, "GL_FRONT_AND_BACK");
				cullingMap.put(GL20.GL_NONE, "GL_NONE");
				cullingMap.put(-1, "Inherit default");
				
				Array<Integer> items = new Array<Integer>();
				items.addAll(GL20.GL_FRONT, GL20.GL_BACK, GL20.GL_FRONT_AND_BACK, GL20.GL_NONE, -1);
				
				t.add(UI.selector(getSkin(), items, ia.value, v->cullingMap.get(v), v->ia.value=v));
				
				
				table.add(t).row();
				
			}else{
				Table t = new Table(getSkin());
				t.add(Attribute.getAttributeAlias(attribute.type));
				table.add(t).row();
			}
		}
	}
}
