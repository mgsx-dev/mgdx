package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class SkinningModule implements GLTFComposerModule
{
	private Table controls;

	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table t = controls = new Table(skin);
		controls.add("no skeleton found");
		return t;
	}
	
	// TODO transformer en skinning module (display bones, etc...) + animation controls
	
	@Override
	public void render(GLTFComposerContext ctx) {
		if(ctx.sceneJustChanged){
			Table t = controls;
			t.clear();
			t.defaults().pad(10);
			
			// TODO find bones
			
			// TODO toggle display bones overlay
			
			
			/*
			if(ctx.scene.modelInstance.animations.size > 0){
				Array<Animation> animations = new Array<Animation>(ctx.scene.modelInstance.animations);
				animations.insert(0, new Animation());
				
				SelectBox<Animation> animSelector = UI.selector(ctx.skin, animations, null, anim->anim.id, anim->ctx.scene.animationController.setAnimation(anim.id, -1));
				t2.add(animSelector).row();
			}else{
				t2.add("No animations");
			}
			*/
		}
	}
}