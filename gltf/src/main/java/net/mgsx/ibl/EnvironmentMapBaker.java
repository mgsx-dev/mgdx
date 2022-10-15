package net.mgsx.ibl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferCubemapBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

import net.mgsx.gltfx.GLFormat;
import net.mgsx.gltfx.ShaderProgramUtils;

public class EnvironmentMapBaker implements Disposable
{
	private Texture hdrEquirectangular;
	private ShaderProgram rectToCubeShader;
	private ShapeRenderer shapes;
	
	public EnvironmentMapBaker(Texture hdrEquirectangular) {
		super();
		this.hdrEquirectangular = hdrEquirectangular;
		rectToCubeShader = loadShader("shaders/hdri/cubemap-make", "#define GAMMA_CORRECTION\n");
		shapes = new ShapeRenderer(20, rectToCubeShader);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
	}

	private Cubemap equirectangularToCube(Texture hdrTexture, int size, GLFormat format, float exposure){
		
		int width = size;
		int height = size;
		
		FrameBufferCubemapBuilder b = new FrameBufferCubemapBuilder(width, height);
		b.addColorTextureAttachment(format.internalFormat, format.format, format.type);
		FrameBufferCubemap fboEnv = b.build();
		
		fboEnv.begin();
		while(fboEnv.nextSide()){
			hdrTexture.bind();
			rectToCubeShader.bind();
			rectToCubeShader.setUniformi("u_hdr", 0);
			rectToCubeShader.setUniformf("u_exposure", exposure);
			
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			CubemapSide side = fboEnv.getSide();
			
			Matrix4 matrix = new Matrix4().setToLookAt(side.direction, side.up).tra();
			rectToCubeShader.setUniformMatrix("u_mat", matrix);
			shapes.begin(ShapeType.Filled);
			shapes.rect(0, 0, 1, 1);
			shapes.end();
		}
		fboEnv.end();
		Cubemap texture = fboEnv.getTextureAttachments().removeIndex(0);
		fboEnv.dispose();
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		return texture;
	}
	
	private static ShaderProgram loadShader(String name, String options){
		ShaderProgram shader = new ShaderProgram(
				options + Gdx.files.classpath(name + ".vs.glsl").readString(), 
				options + Gdx.files.classpath(name + ".fs.glsl").readString());
		ShaderProgramUtils.check(shader);
		return shader;
	}
	
	public Cubemap createEnvMap(int size, GLFormat format, boolean gammaCorrect){
		// apply gamma correction
		return equirectangularToCube(hdrEquirectangular, size, format, gammaCorrect ? 1.0f / 2.2f : 1);
	}
	
	@Override
	public void dispose() {
		rectToCubeShader.dispose();
		shapes.dispose();
	}

}
