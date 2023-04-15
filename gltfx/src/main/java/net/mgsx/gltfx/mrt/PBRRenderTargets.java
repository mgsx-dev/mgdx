package net.mgsx.gltfx.mrt;

import com.badlogic.gdx.Gdx;

import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.utils.ShaderParser;
import net.mgsx.gltfx.GLFormat;

public class PBRRenderTargets extends RenderTargets
{
	public static final Usage GLOBAL_POSITION = new Usage("POSITION_LOCATION");
	public static final Usage LOCAL_POSITION = new Usage("LOCAL_POSITION_LOCATION");
	public static final Usage NORMAL = new Usage("NORMAL_LOCATION");
	public static final Usage BASE_COLOR = new Usage("BASE_COLOR_LOCATION");
	public static final Usage EMISSIVE = new Usage("EMISSIVE_LOCATION");
	public static final Usage ORM = new Usage("ORM_LOCATION");
	
	public static final Usage DIFFUSE = new Usage("DIFFUSE_LOCATION");
	public static final Usage SPECULAR = new Usage("SPECULAR_LOCATION");
	public static final Usage TRANSMISSION = new Usage("TRANSMISSION_LOCATION");
	

	public void configure(PBRShaderConfig config){
		config.glslVersion = "#version 330\n"; // TODO use version utils ! (GLSLVersion)
		
		String mrtOptions = buildOptions();
		
		config.fragmentShader = mrtOptions +
				ShaderParser.parse(Gdx.files.classpath("shaders/pbr/pbr.fs.glsl"))
			;
		config.vertexShader =mrtOptions +
				ShaderParser.parse(Gdx.files.classpath("shaders/pbr/pbr.vs.glsl"));
	}
	
	public void addColors() {
		addLayer(COLORS, GLFormat.RGBA8, defaultSamples);
	}

	public void addEmissive() {
		addLayer(EMISSIVE, GLFormat.RGB16);
	}

	public void addBaseColor() {
		addLayer(BASE_COLOR, GLFormat.RGB8);
	}

	// TODO use RGBA, see https://learnopengl.com/Advanced-Lighting/Deferred-Shading
	// > Note that we use GL_RGBA16F over GL_RGB16F as GPUs generally prefer 4-component formats over 3-component formats due to byte alignment; some drivers may fail to complete the framebuffer otherwise.
	public void addPosition(boolean hq) {
		addLayer(GLOBAL_POSITION, hq ? GLFormat.RGB32 : GLFormat.RGB16);
	}

	public void addNormal(boolean hq) {
		addLayer(NORMAL, hq ? GLFormat.RGB32 : GLFormat.RGB16);
	}

	public void addORM() {
		addLayer(ORM, GLFormat.RGB8);
	}

	public void setDepth(boolean hasAttachment) {
		if(hasAttachment){
			addLayer(DEPTH, GLFormat.DEPTH24); // TODO doesn't work.
			// super.setDepth(GLFormat.DEPTH24);
		}else{
			super.setDepth(GLFormat.DEPTH24);
		}
	}

}
