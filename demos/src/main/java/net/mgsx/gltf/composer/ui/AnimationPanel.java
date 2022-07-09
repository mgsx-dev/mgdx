package net.mgsx.gltf.composer.ui;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;

public class AnimationPanel  extends Table
{
	private final GLTFComposerContext ctx;
	private final Animation animation;
	private final Slider progress;

	public AnimationPanel(GLTFComposerContext ctx, Animation animation) {
		super(ctx.skin);
		defaults().pad(UI.DEFAULT_PADDING);

		this.ctx = ctx;
		this.animation = animation;
		
		Table table = this;
		
		UI.header(table, "Animation: " + animation.id);
		
		AnimationController control = ctx.scene.animationController;
		AnimationDesc current = control.current;
		
		boolean isCurrent = current != null && current.animation == animation;
		float speed = current != null ? current.speed : 1;
		
		UI.toggle(table, "play/pause", isCurrent, v->{
			if(v){
				ctx.scene.animationController.paused = false;
				ctx.scene.animationController.setAnimation(animation.id, -1);
			}else{
				ctx.scene.animationController.paused = true;
			}
		});
		
		table.add(UI.change(new TextButton("stop", getSkin()), e->{
			ctx.scene.animationController.setAnimation(null);
		})).row();
		
		UI.slider(table, "Playback speed", 1e-3f, 1e3f, speed, ControlScale.LOG, v->{
			if(ctx.scene.animationController.current != null){
				ctx.scene.animationController.current.speed = v;
			}
		});
		
		progress = UI.slider(table, "Seek", 0, 1, 0, v->{
			if(ctx.scene.animationController.current != null){
				ctx.scene.animationController.paused = false;
				ctx.scene.animationController.current.speed = 0;
				float time = v * animation.duration;
				ctx.scene.animationController.current.time = time;
			}
		});
	}
	
	@Override
	public void act(float delta) {
		AnimationController control = ctx.scene.animationController;
		AnimationDesc current = control.current;
		boolean isCurrent = current != null && current.animation == animation;
		if(isCurrent){
			float value = current.time / animation.duration;
			progress.setProgrammaticChangeEvents(false);
			progress.setValue(value);
			progress.setProgrammaticChangeEvents(true);
		}
		
		super.act(delta);
	}

}
