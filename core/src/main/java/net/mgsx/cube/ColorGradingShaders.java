package net.mgsx.cube;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.mgsx.gdx.graphics.Texture3D;
import net.mgsx.gdx.graphics.glutils.TextureUtils;
import net.mgsx.gdx.utils.ShaderProgramUtils;

public class ColorGradingShaders {

	public static class ColorGrading3D extends ShaderProgram {
		public ColorGrading3D() {
			super(Gdx.files.classpath("shaders/sprite-batch.vs.glsl"), 
				Gdx.files.classpath("shaders/lut-3d.fs.glsl"));
			ShaderProgramUtils.check(this);
		}
		
		public void setLUT(Texture3D lut){
			setUniformi("u_textureLUT", 1);
			lut.bind(1);
			TextureUtils.setActive(0);
		}
	}
	
	public static class ColorGrading2D extends ShaderProgram {
		public ColorGrading2D() {
			super(Gdx.files.classpath("shaders/sprite-batch.vs.glsl"), 
				Gdx.files.classpath("shaders/lut-2d.fs.glsl"));
			ShaderProgramUtils.check(this);
		}
		
		public void setLUT(Texture lut){
			setUniformf("u_dim", lut.getHeight());
			setUniformi("u_textureLUT", 1);
			lut.bind(1);
			TextureUtils.setActive(0);
		}
	}
	
	public static class ColorGrading2DHQ extends ShaderProgram {
		public ColorGrading2DHQ() {
			super(Gdx.files.classpath("shaders/sprite-batch.vs.glsl"), 
				Gdx.files.classpath("shaders/lut-2d-hq.fs.glsl"));
			ShaderProgramUtils.check(this);
		}
		
		public void setLUT(Texture[] rgbLUTs){
			setUniformf("u_dim", rgbLUTs[0].getHeight());
			setUniformi("u_textureLUTR", 1);
			setUniformi("u_textureLUTG", 2);
			setUniformi("u_textureLUTB", 3);
			rgbLUTs[0].bind(1);
			rgbLUTs[1].bind(2);
			rgbLUTs[2].bind(3);
			TextureUtils.setActive(0);
		}
	}
	
}
