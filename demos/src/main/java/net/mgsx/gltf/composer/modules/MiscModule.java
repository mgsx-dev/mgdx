package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.exporters.GLTFExporter;
import net.mgsx.gltf.scene3d.scene.Scene;

public class MiscModule implements GLTFComposerModule
{
	private ParticleModule particles;
	
	public MiscModule(GLTFComposerContext ctx) {
		particles = new ParticleModule();
	}
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Table controls = UI.table(skin);
		controls.defaults().growX();
		
		UI.header(controls, "Experimental");
		
		controls.add(particles.initUI(ctx, skin)).row();
		
		{
			Frame frame = UI.frame("Export", skin);
			controls.add(frame).row();
			Table t = frame.getContentTable();
			
			t.add(UI.trig(ctx.skin, "save current model as gltf", ()->{
				ctx.fileSelector.save(file->{
					new GLTFExporter().export(ctx.scene != null ? ctx.scene : new Scene(new Model()), file);
				});
			})).row();
		}
		
		
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
