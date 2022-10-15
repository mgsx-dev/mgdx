package net.mgsx.gltfx.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.mgsx.gltfx.ShaderProgramUtils;

public abstract class ToneMappingShader extends ShaderProgram
{
	public static class GammaCorrectOnlyShader extends ToneMappingShader {
		public GammaCorrectOnlyShader(boolean gammaCorrect) {
			super("", gammaCorrect);
		}
	}
	public static class Reinhard extends ToneMappingShader {
		public Reinhard(boolean gammaCorrect) {
			super("#define REINHARD\n", gammaCorrect);
		}
	}
	public static class Exposure extends ToneMappingShader {
		private int u_exposure;
		public Exposure(boolean gammaCorrect) {
			super("#define EXPOSURE\n", gammaCorrect);
			u_exposure = getUniformLocation("u_exposure");
		}
		public void setExposure(float value){
			setUniformf(u_exposure, value);
		}
	}
	public static class GammaCompression extends ToneMappingShader {
		private int u_luminosity;
		private int u_contrast;
		public GammaCompression(boolean gammaCorrect) {
			super("#define GAMMA_COMPRESSION\n", gammaCorrect);
			u_luminosity = getUniformLocation("u_luminosity");
			u_contrast = getUniformLocation("u_contrast");
		}
		public void setLuminosity(float value){
			setUniformf(u_luminosity, value);
		}
		public void setContrast(float value){
			setUniformf(u_contrast, value);
		}
	}

	public ToneMappingShader(String fragmentPrefix, boolean gammaCorrect) {
		super(
			Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(),
			fragmentPrefix +
			(gammaCorrect ? "#define GAMMA_CORRECTION 2.2\n" : "") +
			Gdx.files.classpath("shaders/tone-mapping.fs.glsl").readString()
		);
		ShaderProgramUtils.check(this);
	}
}
