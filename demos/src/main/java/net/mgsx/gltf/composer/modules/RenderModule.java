package net.mgsx.gltf.composer.modules;

import java.util.function.Supplier;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.composer.utils.GLTFModuleSwitch;

public class RenderModule extends GLTFModuleSwitch
{
	public static final ObjectMap<GLTFComposerModule, String> extra = new ObjectMap<>();
	public static final ObjectMap<Supplier<GLTFComposerModule>, String> extraFactory = new ObjectMap<>();
	
	public RenderModule(GLTFComposerContext ctx) {
		for(Entry<GLTFComposerModule, String> e : extra){
			addSubModule(ctx, e.key, e.value);
		}
		for(Entry<Supplier<GLTFComposerModule>, String> e : extraFactory){
			addSubModule(ctx, e.key.get(), e.value);
		}
		addSubModule(ctx, new HDRModule(), "HDR Rendering");
		addSubModule(ctx, new LDRModule(), "LDR Rendering");
		addSubModule(ctx, new GouraudModule(), "Gouraud Rendering");
		addSubModule(ctx, new CavityModule(), "Cavity");
		addSubModule(ctx, new ToonModule(), "Toon");
		addSubModule(ctx, new MRTModule(), "MRT debug");
		addSubModule(ctx, new LDROldModule(), "LDR (old shader) Rendering");
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
