package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.utils.GLTFModuleSwitch;

public class RenderModule extends GLTFModuleSwitch
{
	public RenderModule(GLTFComposerContext ctx) {
		addSubModule(ctx, new HDRModule(), "HDR Rendering");
		addSubModule(ctx, new LDRModule(), "LDR Rendering");
		addSubModule(ctx, new GouraudModule(), "Gouraud Rendering");
		addSubModule(ctx, new CavityModule(), "Cavity");
		addSubModule(ctx, new ToonModule(), "Toon");
		addSubModule(ctx, new MRTModule(), "MRT debug");
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Actor switcher = super.initUI(ctx, skin);
		
		Table table = UI.table(skin);
		table.defaults().growX();
		UI.header(table, "Shading");
		
		table.add(switcher).fill().row();
		
		return table;
	}
	
}
