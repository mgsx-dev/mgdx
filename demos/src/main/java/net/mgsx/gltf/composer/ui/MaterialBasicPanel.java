package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFlagAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;

public class MaterialBasicPanel extends MaterialPanelBase
{
	public MaterialBasicPanel(GLTFComposerContext ctx, Material material) {
		super(ctx, material);
		rebuild(ctx, material);
	}

	private void rebuild(GLTFComposerContext ctx, Material material) {
		defaults().pad(UI.DEFAULT_PADDING).growX();
		
		Table table = this;
		table.clear();
		
		UI.header(table, "Material " + material.id);
		
		// Base color
		if(material.has(PBRColorAttribute.BaseColorFactor)){
			PBRColorAttribute fa = material.get(PBRColorAttribute.class, PBRColorAttribute.BaseColorFactor);
			UI.colorBox(table, "Base color", fa.color, false);
		}
		if(material.has(PBRTextureAttribute.BaseColorTexture)){
			table.add(createTexture(material, PBRTextureAttribute.BaseColorTexture)).row();
		}
		
		if(material.has(PBRFlagAttribute.Unlit)){
			
			table.add("Unlit material").row();
			
		}else{
			
			// metal roughness
			if(material.has(PBRFloatAttribute.Metallic)){
				PBRFloatAttribute fa = material.get(PBRFloatAttribute.class, PBRFloatAttribute.Metallic);
				UI.slider(table, Attribute.getAttributeAlias(fa.type), 0, 1, fa.value, v->fa.value=v);
			}
			if(material.has(PBRFloatAttribute.Roughness)){
				PBRFloatAttribute fa = material.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness);
				UI.slider(table, Attribute.getAttributeAlias(fa.type), 0, 1, fa.value, v->fa.value=v);
			}
			if(material.has(PBRTextureAttribute.MetallicRoughnessTexture)){
				table.add(createTexture(material, PBRTextureAttribute.MetallicRoughnessTexture)).row();
			}
			
			// normals
			if(material.has(PBRTextureAttribute.NormalTexture)){
				table.add(createTexture(material, PBRTextureAttribute.NormalTexture)).row();
				
				if(material.has(PBRFloatAttribute.NormalScale)){
					PBRFloatAttribute fa = material.get(PBRFloatAttribute.class, PBRFloatAttribute.NormalScale);
					UI.slider(table, Attribute.getAttributeAlias(fa.type), 0, 2, fa.value, v->fa.value=v);
				}
			}
			
			// AO
			if(material.has(PBRTextureAttribute.OcclusionTexture)){
				table.add(createTexture(material, PBRTextureAttribute.OcclusionTexture)).row();
				
				if(material.has(PBRFloatAttribute.OcclusionStrength)){
					PBRFloatAttribute fa = material.get(PBRFloatAttribute.class, PBRFloatAttribute.OcclusionStrength);
					UI.slider(table, Attribute.getAttributeAlias(fa.type), 0, 2, fa.value, v->fa.value=v);
				}
			}
		}
		
		
		// Options
		if(material.has(IntAttribute.CullFace)){
			IntAttribute ia = material.get(IntAttribute.class, IntAttribute.CullFace);
			
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
		}
		
	}
}
