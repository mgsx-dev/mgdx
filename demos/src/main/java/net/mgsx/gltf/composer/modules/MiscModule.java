package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.exporters.GLTFExporter;

public class MiscModule implements GLTFComposerModule
{
	private ParticleModule particles;
	
	public MiscModule(GLTFComposerContext ctx) {
		particles = new ParticleModule();
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table controls = UI.table(skin);
		controls.add(particles.initUI(ctx, skin)).growX().row();
		
		controls.add(UI.trig(skin, "Save to gltf", ()->{
			if(ctx.scene != null){
				ctx.fileSelector.save(file->{
					new GLTFExporter().export(ctx.scene, file);
				});
			}
		})).row();
		
		return controls;
	}
	
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		particles.update(ctx, delta);
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		particles.render(ctx);
	}
}
