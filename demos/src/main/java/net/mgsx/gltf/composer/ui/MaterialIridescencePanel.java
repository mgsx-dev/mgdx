package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.scene3d.attributes.PBRIridescenceAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;

public class MaterialIridescencePanel extends MaterialPanelBase
{
	public MaterialIridescencePanel(GLTFComposerContext ctx, Material material) {
		super(ctx, material);
		rebuild(ctx, material);
	}

	private void rebuild(GLTFComposerContext ctx, Material material) {
		defaults().pad(UI.DEFAULT_PADDING).growX();
		
		Table table = this;
		table.clear();
		
		UI.header(table, "Material iridescence");
		
		{
			Frame frame = UI.frameToggle("Iridescence", getSkin(), material.has(PBRIridescenceAttribute.Type), b->{
				if(b){
					material.set(new PBRIridescenceAttribute(1, 1.5f, 100, 400));
				}else{
					material.remove(PBRIridescenceAttribute.Type);
				}
				rebuild(ctx, material);
			});
			if(material.has(PBRIridescenceAttribute.Type)){
				PBRIridescenceAttribute va = material.get(PBRIridescenceAttribute.class, PBRIridescenceAttribute.Type);
				Table t = frame.getContentTable();
				UI.slider(t, "Factor", 0, 1, va.factor, ControlScale.LIN, v->va.factor=v);
				UI.slider(t, "IOR", 1, 5, va.ior, v->va.ior=v);
				UI.slider(t, "Thick. min", 0, 1000, va.thicknessMin, v->va.thicknessMin=v);
				UI.slider(t, "Thick. max", 0, 1000, va.thicknessMax, v->va.thicknessMax=v);
			}
			if(material.has(PBRTextureAttribute.IridescenceTexture)){
				frame.getContentTable().add(createTexture(material, PBRTextureAttribute.IridescenceTexture)).row();
			}
			if(material.has(PBRTextureAttribute.IridescenceThicknessTexture)){
				frame.getContentTable().add(createTexture(material, PBRTextureAttribute.IridescenceThicknessTexture)).row();
			}
			
			table.add(frame).fill().row();
		}
		
	}
}
