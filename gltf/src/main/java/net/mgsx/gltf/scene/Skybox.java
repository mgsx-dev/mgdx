package net.mgsx.gltf.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import net.mgsx.gltf.scene3d.attributes.PBRMatrixAttribute;
import net.mgsx.gltf.scene3d.scene.SceneRenderableSorter;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class Skybox extends SceneSkybox
{
	private final Matrix4 directionInverse = new Matrix4();
	public float lod;
	private Model quadModel;
	private Renderable quad;
	private ShaderProvider myShaderProvider;
	public final Environment environment = new Environment();
	
	// override to create special shader and special mesh
	public Skybox(Cubemap cubemap, SRGB manualSRGB, boolean gammaCorrection) {
		super(cubemap, manualSRGB, gammaCorrection);
		myShaderProvider = createShaderProvider(manualSRGB, gammaCorrection ? 2.2f : null);
		lod = 0;
		createQuad(cubemap);
	}
	
	
	private void createQuad(Cubemap cubemap) {
		float z = 0;
		quadModel = new ModelBuilder().createRect(
			-1,-1,z,
			1,-1,z,
			1,1,z,
			-1,1,z,
			0,0,-1,
			new Material(),
			VertexAttributes.Usage.Position);
		
		quad = quadModel.nodes.first().parts.first().setRenderable(new Renderable());
		
		// assign environment
		environment.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
		quad.environment = environment;
		
		// set hint to render last but before transparent ones
		quad.userData = SceneRenderableSorter.Hints.OPAQUE_LAST;
		
		// set material options : preserve background depth
		quad.material = new Material();
		quad.material.set(new DepthTestAttribute(false));
	}
	
	// override to get the quad environement instead of box
	@Override
	public SceneSkybox set(Cubemap cubemap){
		super.set(cubemap);
		quad.environment.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
		return this;
	}
	
	// override to compute quad transform
	@Override
	public void update(Camera camera, float delta){
		super.update(camera, delta);
		
		// https://webglfundamentals.org/webgl/lessons/webgl-skybox.html
		
		directionInverse.set(camera.view);
		directionInverse.setTranslation(0, 0, 1e-30f);
		quad.worldTransform.set(camera.projection).mul(directionInverse).inv();
	}

	private ShaderProvider createShaderProvider(SRGB manualSRGB, Float gammaCorrection){
		String prefix = "";
		if(manualSRGB != SRGB.NONE){
			prefix += "#define MANUAL_SRGB\n";
			if(manualSRGB == SRGB.FAST){
				prefix += "#define SRGB_FAST_APPROXIMATION\n";
			}
		}
		if(gammaCorrection != null){
			prefix += "#define GAMMA_CORRECTION " + gammaCorrection + "\n";
		}
		
		// TODO should be conditional
		prefix += "#define ENV_LOD\n";
		
		Config shaderConfig = new Config();
		String basePathName = "shaders/skybox-screen";
		shaderConfig.vertexShader = Gdx.files.classpath(basePathName + ".vs.glsl").readString();
		shaderConfig.fragmentShader = prefix + Gdx.files.classpath(basePathName + ".fs.glsl").readString();
		return new DefaultShaderProvider(shaderConfig){
			@Override
			protected Shader createShader(Renderable renderable) {
				String old = ShaderStage.fragment.prependCode;
				
				// TODO make the GLSL code compatible
				ShaderStage.fragment.prependCode = "#version 330\n";
				
				String shaderPrefix = DefaultShader.createPrefix(renderable, this.config);
				
				if(renderable.environment.has(PBRMatrixAttribute.EnvRotation)){
					shaderPrefix += "#define ENV_ROTATION\n";
				}
				
				SkyboxShader s = new SkyboxShader(renderable, config, shaderPrefix);
					
				ShaderStage.fragment.prependCode = old;
				return s;
			}
		};
	}
	
	private class SkyboxShader extends DefaultShader {
		private int u_lod;
		private int u_diffuse;
		
		public SkyboxShader(Renderable renderable, Config config, String prefix) {
			super(renderable, config, prefix);
			register(PBRShader.envRotationUniform, PBRShader.envRotationSetter);
		}
		
		@Override
		public void init() {
			super.init();
			u_lod = Gdx.gl.glGetUniformLocation(program.getHandle(), "u_lod");
			u_diffuse = Gdx.gl.glGetUniformLocation(program.getHandle(), "u_diffuseColor");
		}
		
		@Override
		protected void bindMaterial(Attributes attributes) {
			super.bindMaterial(attributes);
			if(u_lod >= 0) program.setUniformf(u_lod, lod);
			if(u_diffuse >= 0){
				ColorAttribute diffuseColor = attributes.get(ColorAttribute.class, ColorAttribute.Diffuse);
				if(diffuseColor != null){
					program.setUniformf(u_diffuse, diffuseColor.color.r, diffuseColor.color.g, diffuseColor.color.b, diffuseColor.color.a);
				}else{
					program.setUniformf(u_diffuse, 1,1,1,1);
				}
			}
		}
	}

	public void setLod(float value) {
		lod = value;
	}
	
	// override to set the quad shader instead of box
	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		// late shader creation in order to let user change some environment attributes.
		quad.shader = myShaderProvider.getShader(quad);
		renderables.add(quad);
	}

	// override to get the quad environement instead of box
	@Override
	public void setRotation(float azymuthAngleDegree) {
		PBRMatrixAttribute attribute = quad.environment.get(PBRMatrixAttribute.class, PBRMatrixAttribute.EnvRotation);
		if(attribute != null){
			attribute.set(azymuthAngleDegree);
		}else{
			quad.environment.set(PBRMatrixAttribute.createEnvRotation(azymuthAngleDegree));
		}
	}
	
	// override to get the quad environement instead of box
	@Override
	public void setRotation(Matrix4 envRotation) {
		PBRMatrixAttribute attribute = quad.environment.get(PBRMatrixAttribute.class, PBRMatrixAttribute.EnvRotation);
		if(envRotation != null){
			if(attribute != null){
				attribute.matrix.set(envRotation);
			}else{
				quad.environment.set(PBRMatrixAttribute.createEnvRotation(envRotation));
			}
		}else if(attribute != null){
			quad.environment.remove(PBRMatrixAttribute.EnvRotation);
		}
	}
}
