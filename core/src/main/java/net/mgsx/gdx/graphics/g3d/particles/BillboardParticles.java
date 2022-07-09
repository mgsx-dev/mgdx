package net.mgsx.gdx.graphics.g3d.particles;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalCache;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Inputs;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Setters;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class BillboardParticles implements RenderableProvider {
	
	public static class Emitter {
		public final Vector3 position = new Vector3();
		public final Vector3 direction = new Vector3();
		public float emitFrequency, time, emitTime, prefillDuration;
		
		public ParticleConfigurator configurator;
	}
	@FunctionalInterface
	public static interface ParticleConfigurator {
		void configure(Emitter em, Particle p);
	}
	@FunctionalInterface
	public static interface ParticleUpdater {
		void configure(Particle p, float delta);
	}
	public static class Particle {
		public final Vector3 position = new Vector3();
		public final Vector3 velocity = new Vector3();
		public final Vector3 forces = new Vector3();
		public final Vector3 up = new Vector3();
		public float time, life;
		public Decal decal;
		public float angle;
		public float angularVelocity;
		public boolean additive;
		
		public ParticleUpdater updater;
		
		public Particle(DecalMaterial material) {
			decal = new Decal(material);
		}
		public boolean update(Camera camera, float delta) {
			
			// common update
			time += delta;
			
			// custom update 
			updater.configure(this, delta);
			
			// apply physics
			velocity.mulAdd(forces, delta);
			position.mulAdd(velocity, delta);
			angle += angularVelocity * delta;
			
			// update decal
			decal.setPosition(position);
			// TODO don't muate camera, simple compute locally
			camera.up.set(Vector3.Y);
			camera.normalizeUp();
			decal.lookAt(camera.position, up.set(camera.up).rotate(camera.direction, angle));
			
			
			if(additive){
				decal.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			}else{
				decal.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			}
			
			return time <= life;
		}
	}
	
	
	
	private static final Camera dummyCamera = new PerspectiveCamera();
	private DecalBatch batch;
	public final Array<Particle> particles = new Array<Particle>();
	private Pool<Particle> particlePool;
	public final Array<Emitter> emitters = new Array<Emitter>();
	private CameraGroupStrategy groupStrategy;
	private Comparator<Decal> sorter;
	private Vector3 cameraPosition = new Vector3();
	private Vector3 cameraDirection = new Vector3();
	private Vector3 cameraOffset = new Vector3();
	
	public BillboardParticles() {
		sorter = new Comparator<Decal>() {
			@Override
			public int compare (Decal o1, Decal o2) {
				float dist1 = groupStrategy.getCamera().position.dst(o1.getPosition());
				float dist2 = groupStrategy.getCamera().position.dst(o2.getPosition());
				return (int)Math.signum(dist2 - dist1);
			}
		};
		batch = new DecalBatch(groupStrategy = new CameraGroupStrategy(dummyCamera, sorter){
			@Override
			public void beforeGroups() {
				super.beforeGroups();
				// Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
			}
			@Override
			public void beforeGroup(int group, Array<Decal> contents) {
				if (group == 1 || group == 2) {
					Gdx.gl.glEnable(GL20.GL_BLEND);
					contents.sort(sorter);
				} 
			}
			@Override
			public int decideGroup(Decal decal) {
				if(decal.getMaterial().getDstBlendFactor() == GL20.GL_ONE_MINUS_SRC_ALPHA){
					return 1;
				}
				if(decal.getMaterial().getDstBlendFactor() == GL20.GL_ONE){
					return 2;
				}
				return 0;
			}
		});
		
		particlePool = new Pool<Particle>(){
			@Override
			protected Particle newObject() {
				Particle p = new Particle(new DecalMaterial());
				return p;
			}
		};
	}
	
	public void reset(Camera camera, boolean prefill, int maxSteps){
		particlePool.freeAll(particles);
		particles.clear();
		if(prefill){
			
			boolean modeGlobal = true;
			
			if(modeGlobal){
				float duration = 0;
				for(Emitter em : emitters){
					em.time = 0;
					em.emitTime = 0;
					duration = Math.max(duration, em.prefillDuration);
				}
				float d = maxSteps > 0 ? Math.max(1/60f, duration / maxSteps) : duration;
				for(float t=0 ; t<duration ; t+=d){
					update(camera, d);
				}
			}else{
				// FIXME doesn't work : need to update particles with steps in side emitter update.
				Array<Particle> addedPartciles = new Array<Particle>();
				for(Emitter em : emitters){
					em.time = 0;
					em.emitTime = 0;
					float duration = em.prefillDuration;
					if(duration > 0){
						updateEmitter(em, camera, duration);
						
						float d = maxSteps > 0 ? Math.max(1/60f, duration / maxSteps) : duration;
						for(float t=0 ; t<duration ; t+=d){
							for(Particle p : particles){
								p.update(camera, d);
							}
						}
						
						addedPartciles.addAll(particles);
						particles.clear();
					}
				}
				particles.addAll(addedPartciles);
			}
			
			
		}
	}
	
	private void updateEmitter(Emitter em, Camera camera, float delta){
		em.time += delta;
		em.emitTime += em.emitFrequency * delta;
		while(em.emitTime >= 1){
			em.emitTime	-= 1f;
			Particle p = particlePool.obtain();
			
			// common config
			p.time = 0; // em.emitTime / em.emitFrequency;
			p.position.set(em.position);
			
			// custom config
			em.configurator.configure(em, p);
			
			particles.add(p);
		}
	}
	
	public void update(Camera camera, float delta){
		groupStrategy.setCamera(camera);
		
		for(Emitter em : emitters){
			updateEmitter(em, camera, delta);
		}
		
		for(int i=particles.size-1 ; i>=0 ; i--){
			Particle p = particles.get(i);
			if(!p.update(camera, delta)){
				particles.removeIndex(i);
				particlePool.free(p);
			}
		}
		cacheValid = false;
		particles.sort((a,b)->Float.compare(b.position.dst(camera.position), a.position.dst(camera.position)));
		cameraPosition.set(camera.position);
		cameraDirection.set(camera.direction);
	}
	
	public void render(){
		Gdx.gl.glDepthMask(false);
		for(Particle p : particles) batch.add(p.decal);
		batch.flush();
		Gdx.gl.glDepthMask(true);
	}

	private boolean cacheValid;
	private final DecalCache cache = new DecalCache(new ShaderProvider() {
		private DecalShader shader;
		@Override
		public void dispose() {
		}
		@Override
		public Shader getShader(Renderable renderable) {
			if(shader == null){
				shader = new DecalShader(renderable);
				shader.init();
			}
			return shader;
		}
	});
	
	
	private static class DecalShader extends BaseShader {
		private ShaderProgram createDefaultShader () {
			String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
					+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
					+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
					+ "uniform mat4 u_projViewTrans;\n" //
					+ "varying vec4 v_color;\n" //
					+ "varying vec2 v_texCoords;\n" //
					+ "\n" //
					+ "void main()\n" //
					+ "{\n" //
					+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
					+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
					+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
					+ "   gl_Position =  u_projViewTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
					+ "}\n";
			String fragmentShader = "#ifdef GL_ES\n" //
					+ "precision mediump float;\n" //
					+ "#endif\n" //
					+ "varying vec4 v_color;\n" //
					+ "varying vec2 v_texCoords;\n" //
					+ "uniform sampler2D u_diffuseTexture;\n" //
					+ "void main()\n"//
					+ "{\n" //
					+ "  vec4 color = v_color * texture2D(u_diffuseTexture, v_texCoords);\n" //
					+ "  gl_FragColor = vec4(pow(color.rgb, vec3(4.0, 4.0, 4.0)) * 100.0, color.a);\n" //
					+ "}";
			
			ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
			if (!shader.isCompiled()) throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
			
			return shader;
		}
		
		private int u_projViewTrans;
		private int u_texture;
		private Renderable renderable;

		public DecalShader(Renderable renderable) {
			this.renderable = renderable;
			u_projViewTrans = register(Inputs.projViewTrans, Setters.projViewTrans);
			u_texture = register(Inputs.diffuseTexture, Setters.diffuseTexture);
		}
		
		@Override
		public void init() {
			program = createDefaultShader();
			init(program, renderable);
		}
		
		@Override
		public void render(Renderable renderable, Attributes combinedAttributes) {
			BlendingAttribute blending = combinedAttributes.get(BlendingAttribute.class, BlendingAttribute.Type);
			context.setBlending(true, blending.sourceFunction, blending.destFunction);
			context.setCullFace(0);
			context.setDepthMask(false);
			super.render(renderable, combinedAttributes);
		}
		
		@Override
		public int compareTo(Shader other) {
			return 0;
		}
		
		@Override
		public boolean canRender(Renderable instance) {
			return instance.userData instanceof DecalMaterial;
		}
	};
	
	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		if(!cacheValid){
			cacheValid = true;
			cache.begin();
			for(Particle p : particles){
				cache.add(p.decal);
			}
			cache.end();
		}
		int index = renderables.size;
		cache.getRenderables(renderables, pool);
		for( ; index < renderables.size ; index++){
			Renderable r = renderables.get(index);
			DecalMaterial decalMat = (DecalMaterial)r.userData;
			if(decalMat.getDstBlendFactor() == GL20.GL_ONE){
				r.worldTransform.setToTranslation(cameraOffset.set(cameraPosition).mulAdd(cameraDirection, 1));
			}else{
				r.worldTransform.setToTranslation(cameraOffset.set(cameraPosition).mulAdd(cameraDirection, 2));
			}
		}
	}
}
