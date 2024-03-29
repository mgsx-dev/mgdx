package net.mgsx.gltf.composer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;

public interface GLTFComposerModule extends Disposable {

	default Actor initUI(GLTFComposerContext ctx, Skin skin){
		return null;
	}
	
	default boolean handleFile(GLTFComposerContext ctx, FileHandle file){
		return false;
	}
	
	default void update(GLTFComposerContext ctx, float delta){
	}
	
	default void render(GLTFComposerContext ctx){
	}
	
	default void renderOverlay(GLTFComposerContext ctx){
	}
	
	default void show(GLTFComposerContext ctx){}
	default void hide(GLTFComposerContext ctx){}
	
	@Override
	default void dispose() {
	}

}
