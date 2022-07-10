package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.utils.GLTFModuleSwitch;

public class RenderModule extends GLTFModuleSwitch
{
	private LightingModule lighting = new LightingModule();
	private ShadowModule shadows = new ShadowModule();
	
	public RenderModule(GLTFComposerContext ctx) {
		addSubModule(ctx, new HDRModule(), "HDR Rendering");
		addSubModule(ctx, new LDRModule(), "LDR Rendering");
		addSubModule(ctx, new CavityModule(), "Cavity");
		addSubModule(ctx, new ToonModule(), "Toon");
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Actor switcher = super.initUI(ctx, skin);
		
		Table table = UI.table(skin);
		table.defaults().fill();

		UI.header(table, "Lighting");
		
		// common options
		table.add(lighting.initUI(ctx, skin)).row();
		table.add(shadows.initUI(ctx, skin)).row();
		
		UI.header(table, "Renderer");

		table.add(switcher).row();
		
		return table;
	}
	
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		lighting.update(ctx, delta);
		shadows.update(ctx, delta);
		super.update(ctx, delta);
	}
}
