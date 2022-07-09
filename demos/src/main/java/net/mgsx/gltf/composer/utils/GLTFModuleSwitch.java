package net.mgsx.gltf.composer.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class GLTFModuleSwitch implements GLTFComposerModule
{
	private static class SubModule {
		GLTFComposerModule module;
		String name;
		Actor ui;
	}
	private final Array<SubModule> modules = new Array<SubModule>();
	private SubModule current;
	private Table controls;
	private Cell cell;
	
	public void addSubModule(GLTFComposerContext ctx, GLTFComposerModule module, String name){
		SubModule s = new SubModule();
		s.module = module;
		s.name = name;
		modules.add(s);
		if(current == null) {
			current = s;
			current.module.show(ctx);
		}
	}
	
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		return current.module.handleFile(ctx, file);
	}
	
	@Override
	public void dispose() {
		for(SubModule subModule : modules){
			subModule.module.dispose();
		}
	}
	
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		current.module.update(ctx, delta);
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		current.module.render(ctx);
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = new Table(skin);
		for(SubModule subModule : modules){
			subModule.ui = subModule.module.initUI(ctx, skin);
		}
		controls.add(UI.selector(skin, modules, current, m->m.name, m->setCurrent(ctx, m))).row();
		cell = controls.add(current.ui);
		controls.row();
		return controls;
	}

	private void setCurrent(GLTFComposerContext ctx, SubModule m) {
		if(m == current) return;
		if(current != null) current.module.hide(ctx);
		current = m;
		if(current != null) current.module.show(ctx);
		cell.setActor(current.ui);
	}
}
