package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRHDRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;

public class MaterialSpecularPanel extends MaterialPanelBase
{
	public MaterialSpecularPanel(GLTFComposerContext ctx, Material material) {
		super(ctx, material);
		rebuild(ctx, material);
	}

	private void rebuild(GLTFComposerContext ctx, Material material) {
		defaults().pad(UI.DEFAULT_PADDING).growX();
		
		Table table = this;
		table.clear();
		
		UI.header(table, "Material specular");
		
		{
			Frame frame = UI.frameToggle("Factor", getSkin(), material.has(PBRFloatAttribute.SpecularFactor), b->{
				if(b){
					material.set(PBRFloatAttribute.createSpecularFactor(1f));
				}else{
					material.remove(PBRFloatAttribute.SpecularFactor);
				}
				rebuild(ctx, material);
			});
			if(material.has(PBRFloatAttribute.SpecularFactor)){
				PBRFloatAttribute fa = material.get(PBRFloatAttribute.class, PBRFloatAttribute.SpecularFactor);
				UI.slider(frame.getContentTable(), "Factor", 0, 1, fa.value, v->fa.value=v);
			}
			if(material.has(PBRTextureAttribute.SpecularFactorTexture)){
				frame.getContentTable().add(createTexture(material, PBRTextureAttribute.SpecularFactorTexture)).row();
			}
			
			table.add(frame).fill().row();
		}
		
		{
			Frame frame = UI.frameToggle("Color", getSkin(), material.has(PBRHDRColorAttribute.Specular), b->{
				if(b){
					material.set(new PBRHDRColorAttribute(PBRHDRColorAttribute.Specular, 1, 1, 1));
				}else{
					material.remove(PBRHDRColorAttribute.Specular);
				}
				rebuild(ctx, material);
			});
			if(material.has(PBRHDRColorAttribute.Specular)){
				PBRHDRColorAttribute fa = material.get(PBRHDRColorAttribute.class, PBRHDRColorAttribute.Specular);

				Table t = frame.getContentTable();
				// TODO HSV like
				UI.slider(t, Attribute.getAttributeAlias(fa.type) + ".r", 0, 10, fa.r, v->{fa.r=v;});
				UI.slider(t, Attribute.getAttributeAlias(fa.type) + ".g", 0, 10, fa.g, v->{fa.g=v;});
				UI.slider(t, Attribute.getAttributeAlias(fa.type) + ".b", 0, 10, fa.b, v->{fa.b=v;});

				if(material.has(PBRTextureAttribute.Specular)){
					t.add(createTexture(material, PBRTextureAttribute.Specular)).row();
				}
				
			}
			table.add(frame).fill().row();
		}
		
	}
}
