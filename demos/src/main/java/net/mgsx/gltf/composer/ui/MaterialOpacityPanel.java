package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;

public class MaterialOpacityPanel extends MaterialPanelBase
{
	public MaterialOpacityPanel(GLTFComposerContext ctx, Material material) {
		super(ctx, material);
		rebuild(ctx, material);
	}

	private void rebuild(GLTFComposerContext ctx, Material material) {
		defaults().pad(UI.DEFAULT_PADDING).growX();
		
		Table table = this;
		table.clear();
		
		UI.header(table, "Material opacity");
		
		{
			Frame frame = UI.frameToggle("Blending", getSkin(), material.has(BlendingAttribute.Type), b->{
				if(b){
					material.set(new BlendingAttribute(true, 1f));
				}else{
					material.remove(BlendingAttribute.Type);
				}
				rebuild(ctx, material);
			});
			if(material.has(BlendingAttribute.Type)){
				BlendingAttribute fa = material.get(BlendingAttribute.class, BlendingAttribute.Type);
				UI.slider(frame.getContentTable(), Attribute.getAttributeAlias(fa.type), 0, 1, fa.opacity, v->fa.opacity=v);
				// TODO blend modes
			}
			
			table.add(frame).fill().row();
		}
		
		{
			Frame frame = UI.frameToggle("Alpha test", getSkin(), material.has(FloatAttribute.AlphaTest), b->{
				if(b){
					material.set(FloatAttribute.createAlphaTest(.5f));
				}else{
					material.remove(FloatAttribute.AlphaTest);
				}
				rebuild(ctx, material);
			});
			if(material.has(FloatAttribute.AlphaTest)){
				FloatAttribute fa = material.get(FloatAttribute.class, FloatAttribute.AlphaTest);
				UI.slider(frame.getContentTable(), Attribute.getAttributeAlias(fa.type), 0, 1, fa.value, v->fa.value=v);
			}
			
			table.add(frame).fill().row();
		}
		
	}
}
