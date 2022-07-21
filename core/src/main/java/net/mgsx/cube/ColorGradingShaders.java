package net.mgsx.cube;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;

import net.mgsx.gdx.graphics.Texture3D;
import net.mgsx.gdx.graphics.glutils.TextureUtils;
import net.mgsx.gdx.utils.ShaderProgramUtils;

public class ColorGradingShaders {

	public static class ColorGrading3D extends ShaderProgram {
		public ColorGrading3D() {
			super(Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(), 
				"#define USE_TEXTURE_3D\n" +
				Gdx.files.classpath("shaders/color-grading-lut.fs.glsl").readString());
			ShaderProgramUtils.check(this);
		}
		
		public void setLUT(Texture3D lut){
			setUniformf("u_invResolution", 1f / lut.getWidth());
			setUniformi("u_textureLUT", 1);
			lut.bind(1);
			TextureUtils.setActive(0);
		}
	}
	
	public static class ColorGrading2D extends ShaderProgram {
		public ColorGrading2D(boolean trilinear) {
			super(Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(), 
				(trilinear ? "#define TRILINEAR\n" : "") +
				Gdx.files.classpath("shaders/color-grading-lut.fs.glsl").readString());
			ShaderProgramUtils.check(this);
		}
		
		public void setLUT(Texture lut){
			int w = lut.getWidth();
			int h = lut.getHeight();
			if(w == h){
				int gridSize = MathUtils.round((float)Math.pow(w, 1.0 / 3.0));
				setUniformf("u_invResolution", (float)gridSize / (float)w);
				setUniformf("u_grid", gridSize, gridSize);
			}else if(w > h){
				setUniformf("u_invResolution", 1f / h);
				setUniformf("u_grid", h, 1);
			}else{
				setUniformf("u_invResolution", 1f / w);
				setUniformf("u_grid", 1, w);
			}
			
			setUniformi("u_textureLUT", 1);
			lut.bind(1);
			TextureUtils.setActive(0);
		}
	}
	
	public static class ColorGrading2DHQ extends ShaderProgram {
		public ColorGrading2DHQ() {
			super(Gdx.files.classpath("shaders/sprite-batch.vs.glsl").readString(), 
				"#define PACKED_FLOAT\n" +
				Gdx.files.classpath("shaders/color-grading-lut.fs.glsl").readString());
			ShaderProgramUtils.check(this);
		}
		
		public void setLUT(Texture[] rgbLUTs){
			int w = rgbLUTs[0].getWidth();
			int h = rgbLUTs[0].getHeight();
			if(w == h){
				int gridSize = MathUtils.round((float)Math.pow(w, 1.0 / 3.0));
				setUniformf("u_invResolution", (float)gridSize / (float)w);
				setUniformf("u_grid", gridSize, gridSize);
			}else if(w > h){
				setUniformf("u_invResolution", 1f / h);
				setUniformf("u_grid", h, 1);
			}else{
				setUniformf("u_invResolution", 1f / w);
				setUniformf("u_grid", 1, w);
			}
			
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
