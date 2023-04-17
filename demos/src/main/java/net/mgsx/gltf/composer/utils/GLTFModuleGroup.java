package net.mgsx.gltf.composer.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class GLTFModuleGroup implements GLTFComposerModule
{
	private final Array<GLTFComposerModule> modules = new Array<GLTFComposerModule>();
	private Table controls;
	
	public void addSubModule(GLTFComposerModule module){
		modules.add(module);
	}
	
	@Override
	public boolean handleFile(GLTFComposerContext ctx, FileHandle file) {
		for(GLTFComposerModule module : modules){
			if(module.handleFile(ctx, file)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void dispose() {
		for(GLTFComposerModule module : modules){
			module.dispose();
		}
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		for(GLTFComposerModule module : modules){
			module.render(ctx);
		}
	}
	
	@Override
	public void renderOverlay(GLTFComposerContext ctx) {
		for(GLTFComposerModule module : modules){
			module.renderOverlay(ctx);
		}
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		controls = new Table(skin);
		for(GLTFComposerModule subModule : modules){
			controls.add(subModule.initUI(ctx, skin)).row();
		}
		return controls;
	}

}
