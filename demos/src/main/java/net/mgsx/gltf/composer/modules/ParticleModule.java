package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.mgsx.gdx.graphics.g3d.particles.BillboardParticles;
import net.mgsx.gdx.graphics.g3d.particles.BillboardParticles.Emitter;
import net.mgsx.gdx.graphics.g3d.particles.BillboardParticlesBasic.BasicEmitterConfig;
import net.mgsx.gdx.scenes.scene2d.ui.Frame;
import net.mgsx.gdx.scenes.scene2d.ui.UI;
import net.mgsx.gdx.scenes.scene2d.ui.UI.ControlScale;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;

public class ParticleModule implements GLTFComposerModule {

	BillboardParticles particleSystem;
	private Emitter emitter;
	private float time;
	private BasicEmitterConfig emitterConfig;
	private boolean enabled;
	private final Vector3 hsv = new Vector3(15, .9f, 4f / 16f);
	private float alpha = 1;
	private float speed = 1;
	private float additiveRate = .5f;
	private Boolean motionEnabled = false;
	
	@Override
	public Actor initUI(GLTFComposerContext ctx, Skin skin) {
		Frame frame = UI.frameToggle("Particles", skin, false, v->enable(ctx, v));
		Table table = frame.getContentTable();
		
		UI.sliderTable(table, "Speed", 1e-3f, 10, speed, ControlScale.LOG, v->speed=v);
		
		UI.sliderTable(table, "Hue", 0, 360, hsv.x, ControlScale.LIN, v->hsv.x=v);
		UI.sliderTable(table, "Sat", 0, 1, hsv.y, ControlScale.LIN, v->hsv.y=v);
		UI.sliderTable(table, "Lum", 0, 1, hsv.z, ControlScale.LIN, v->hsv.z=v);
		UI.sliderTable(table, "Alpha", 0, 1, alpha, ControlScale.LIN, v->alpha=v);
		UI.sliderTable(table, "Additive", 0, 1, additiveRate, ControlScale.LIN, v->additiveRate=v);

		UI.toggle(table, "Motion", false, b->motionEnabled=b);
		
		return frame;
	}
	
	private void enable(GLTFComposerContext ctx, boolean enabled){
		this.enabled = enabled;
		if(enabled){
			if(particleSystem == null){
				createParticles(ctx);
			}
			ctx.sceneManager.getRenderableProviders().add(particleSystem);
		}else{
			if(particleSystem != null){
				ctx.sceneManager.getRenderableProviders().removeValue(particleSystem, true);
			}
		}
	}
	
	private void createParticles(GLTFComposerContext ctx) {
		Camera camera = ctx.cameraManager.getCamera();
		particleSystem = new BillboardParticles();
		
		Texture texture = new Texture(Gdx.files.internal("textures/particle-noise.png"), true);
		texture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
		TextureRegion textureRegion = new TextureRegion(texture);
		
		emitterConfig = new BasicEmitterConfig();
		emitterConfig.textureRegion = textureRegion;
		
		emitter = new Emitter();
		emitter.configurator = emitterConfig;
		
		particleSystem.emitters.add(emitter);
		
		applySettings();
		particleSystem.reset(camera, true, 20);
	}

	private void applySettings(){
		// play with emitters
		emitter.emitFrequency = 10;
		emitter.direction.set(0,1,0);
		
		if(motionEnabled){
			emitter.position.set(1,0,1).scl(5).rotate(Vector3.Y, time * 30);
		}else{
			emitter.position.setZero();
		}
		
		BasicEmitterConfig c = emitterConfig;
		c.additiveRate = additiveRate;
		
		c.speedMin = 0; c.speedMax = 1;
		c.speedMin = c.spreadMax = .5f;
		c.angularVelRangeMin = 10; c.angularVelRangeMax = 90;
		
		c.updater.size.values(0, 3, 0).slopes(.5f, .5f).interpolations(Interpolation.pow2Out, Interpolation.pow2In);
		
		float hue = hsv.x;
		float sat = hsv.y;
		float lum = hsv.z;
		c.updater.h.values(hue+0, hue+30, hue+15).slopes(.3f, .5f);
		c.updater.s.values(1*sat,.8f*sat,0).slopes(.5f, .5f);
		c.updater.v.values(1,lum,0).slopes(.5f, .5f);
		c.updater.a.values(0,1 * alpha,0).slopes(.5f, .5f);
		
		c.gravity.set(0,1,0);
		
		emitter.prefillDuration = emitterConfig.lifeMax;
	}
	
	@Override
	public void update(GLTFComposerContext ctx, float delta) {
		if(enabled){
			time += delta * speed;
			
			applySettings();
			
			particleSystem.update(ctx.cameraManager.getCamera(), delta * speed);
		}
	}
	
}
