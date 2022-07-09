package net.mgsx.gdx.graphics.g3d.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.gdx.graphics.g3d.particles.BillboardParticles.Emitter;
import net.mgsx.gdx.graphics.g3d.particles.BillboardParticles.Particle;
import net.mgsx.gdx.graphics.g3d.particles.BillboardParticles.ParticleConfigurator;
import net.mgsx.gdx.graphics.g3d.particles.BillboardParticles.ParticleUpdater;

/**
 * A more friendly way to configure and update particles.
 */
public class BillboardParticlesBasic {

	public static class FloatChannel {
		public float startValue, midValue, endValue;
		public Interpolation interpolationIn = Interpolation.linear, interpolationOut = Interpolation.linear;
		public float startSlope = 0.33f;
		public float endSlope = 0.33f;
		public FloatChannel(float constantValue) {
			startValue = midValue = endValue = constantValue;
		}
		public FloatChannel values(float start, float mid, float end){
			startValue = start;
			midValue = mid;
			endValue = end;
			return this;
		}
		public FloatChannel interpolations(Interpolation start, Interpolation end){
			interpolationIn = start;
			interpolationOut = end;
			return this;
		}
		public FloatChannel slopes(float start, float end){
			startSlope = start;
			endSlope = end;
			return this;
		}
		public float get(float t) {
			if(t < startSlope){
				float n = MathUtils.norm(0, startSlope, t);
				return MathUtils.lerp(startValue, midValue, interpolationIn.apply(n));
			}
			else if(t > 1 - endSlope){
				float n = MathUtils.norm(1 - endSlope, 1, t);
				return MathUtils.lerp(midValue, endSlope, interpolationOut.apply(n));
			}else{
				return midValue;
			}
		}
	}
	public static class Range<T> {
		T min, max;
		public Range(T constantValue) {
			min = max = constantValue;
		}
	}
	
	public static class BasicParticleDynamic implements ParticleUpdater {
		private static final Color color = new Color();
		
		public final FloatChannel h = new FloatChannel(0);
		public final FloatChannel s = new FloatChannel(0);
		public final FloatChannel v = new FloatChannel(1);
		public final FloatChannel a = new FloatChannel(1);
		public final FloatChannel size = new FloatChannel(1);
		
		@Override
		public void configure(Particle p, float delta) {
			float t = p.time / p.life;
			
			float size = this.size.get(t);
			
			color.fromHsv(h.get(t), s.get(t), v.get(t));
			color.a = a.get(t);
			
			p.decal.setColor(color);
			p.decal.setDimensions(size, size);
		}
	}
	
	public static class BasicEmitterConfig implements ParticleConfigurator {
		public BasicParticleDynamic updater;
		public TextureRegion textureRegion;
		public float lifeMin = 3, lifeMax = 3;
		public float angleMin = 0, angleMax = 360;
		public float angularVelRangeMin, angularVelRangeMax;
		public float spreadMin=0, spreadMax=180;
		public float speedMin=1, speedMax=1;
		public float additiveRate = .5f;
		public final Vector3 gravity = new Vector3(0, -9.8f, 0);
		
		public BasicEmitterConfig() {
			updater = new BasicParticleDynamic();
		}
		
		@Override
		public void configure(Emitter em, Particle p) {
			p.life = MathUtils.random(lifeMin, lifeMax);
			p.angle = MathUtils.random(angleMin, angleMax);
			p.angularVelocity = MathUtils.random(angularVelRangeMin, angularVelRangeMax) * MathUtils.randomSign();
			p.forces.set(gravity);
			float spread = MathUtils.random(spreadMin, spreadMax);
			float speed = MathUtils.random(speedMin, speedMax);
			p.velocity.setToRandomDirection().lerp(em.direction, 1 - spread).nor().scl(speed);
			p.decal.setTextureRegion(textureRegion);
			p.additive = MathUtils.randomBoolean(additiveRate);
			p.updater = updater;
		}
	}
	
}
