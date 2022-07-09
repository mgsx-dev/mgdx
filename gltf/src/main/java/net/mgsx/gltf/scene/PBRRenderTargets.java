package net.mgsx.gltf.scene;

import com.badlogic.gdx.Gdx;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;

public class PBRRenderTargets extends RenderTargets
{
	public static final Usage GLOBAL_POSITION = new Usage("POSITION_LOCATION");
	public static final Usage LOCAL_POSITION = new Usage("LOCAL_POSITION_LOCATION");
	public static final Usage NORMAL = new Usage("NORMAL_LOCATION");
	public static final Usage BASE_COLOR = new Usage("BASE_COLOR_LOCATION");
	public static final Usage EMISSIVE = new Usage("EMISSIVE_LOCATION");
	public static final Usage ORM = new Usage("ORM_LOCATION");

	public void configure(PBRShaderConfig config){
//		config.vertexShader = 
//				ShaderProgramUtils.getCompatibilityHeader(ShaderStage.vertex, GLSLVersion.OpenGL330) +
//				Gdx.files.classpath("gltfx/gdx-pbr-mrt.vs.glsl").readString();
		config.glslVersion = "#version 330\n";
		config.fragmentShader = 
				// ShaderProgramUtils.getCompatibilityHeader(ShaderStage.fragment, GLSLVersion.OpenGL330) +
				buildOptions() +
				Gdx.files.classpath("shaders/gdx-pbr-mrt.fs.glsl").readString();
	}
	
	// 
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
			addLayer(DEPTH, GLFormat.DEPTH24);
		}else{
			super.setDepth(GLFormat.DEPTH24);
		}
	}

}
