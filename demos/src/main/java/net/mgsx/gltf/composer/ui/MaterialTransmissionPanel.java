package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRVolumeAttribute;

public class MaterialTransmissionPanel extends MaterialPanelBase
{
	public MaterialTransmissionPanel(GLTFComposerContext ctx, Material material) {
		super(ctx, material);
		rebuild(ctx, material);
	}

	private void rebuild(GLTFComposerContext ctx, Material material) {
		defaults().pad(UI.DEFAULT_PADDING).growX();
		
		Table table = this;
		table.clear();
		
		UI.header(table, "Material transmission");
		
		{
			Frame frame = UI.frameToggle("Transmission", getSkin(), material.has(PBRFloatAttribute.TransmissionFactor), b->{
				if(b){
					material.set(PBRFloatAttribute.createTransmissionFactor(1f));
				}else{
					material.remove(PBRFloatAttribute.TransmissionFactor);
				}
				rebuild(ctx, material);
			});
			if(material.has(PBRFloatAttribute.TransmissionFactor)){
				PBRFloatAttribute fa = material.get(PBRFloatAttribute.class, PBRFloatAttribute.TransmissionFactor);
				UI.slider(frame.getContentTable(), "Factor", 0, 1, fa.value, v->fa.value=v);
			}
			if(material.has(PBRTextureAttribute.TransmissionTexture)){
				frame.getContentTable().add(createTexture(material, PBRTextureAttribute.TransmissionTexture)).row();
			}
			
			table.add(frame).fill().row();
		}
		
		{
			Frame frame = UI.frameToggle("Volume", getSkin(), material.has(PBRVolumeAttribute.Type), b->{
				if(b){
					material.set(new PBRVolumeAttribute(1, 0, Color.WHITE));
				}else{
					material.remove(PBRVolumeAttribute.Type);
				}
				rebuild(ctx, material);
			});
			if(material.has(PBRVolumeAttribute.Type)){
				PBRVolumeAttribute va = material.get(PBRVolumeAttribute.class, PBRVolumeAttribute.Type);

				Table t = frame.getContentTable();
				UI.slider(t, "Thickness", 0, 10, va.thicknessFactor, ControlScale.LIN, v->va.thicknessFactor=v);
				
				if(material.has(PBRTextureAttribute.ThicknessTexture)){
					t.add(createTexture(material, PBRTextureAttribute.ThicknessTexture)).row();
				}
				
				UI.slider(t, "Distance", 0, 10, va.attenuationDistance, ControlScale.LIN, v->va.attenuationDistance=v);
				UI.colorBox(t, "Attenuation color", va.attenuationColor, false);
			}
			table.add(frame).fill().row();
		}
		
		{
			Frame frame = UI.frameToggle("IOR", getSkin(), material.has(PBRFloatAttribute.IOR), b->{
				if(b){
					material.set(PBRFloatAttribute.createIOR(1.5f));
				}else{
					material.remove(PBRFloatAttribute.IOR);
				}
				rebuild(ctx, material);
			});
			if(material.has(PBRFloatAttribute.IOR)){
				PBRFloatAttribute fa = material.get(PBRFloatAttribute.class, PBRFloatAttribute.IOR);
				UI.slider(frame.getContentTable(), Attribute.getAttributeAlias(fa.type), 1, 5, fa.value, v->fa.value=v);
			}
			table.add(frame).fill().row();
		}
	}
}
