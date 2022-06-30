package net.mgsx.gdx.graphics.g2d;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferCubemapBuilder;
import com.badlogic.gdx.graphics.glutils.GLOnlyTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.g2d.RGBE.Header;

public class HDRI {
	
	private Texture textureRaw;

	public void loadHDR(FileHandle file){
		DataInputStream in = null;
		try{
			try{
				in = new DataInputStream(new BufferedInputStream(file.read()));
				Header hdrHeader = RGBE.readHeader(in);
				int numPixels = hdrHeader.getWidth() * hdrHeader.getHeight();
				byte[] hdrData = new byte[numPixels * 4];
				RGBE.readPixelsRawRLE(in, hdrData, 0, hdrHeader.getWidth(), hdrHeader.getHeight());
				
				// decode
				createTexture(hdrHeader, hdrData);
			}finally{
				if(in != null) in.close();
			}
		}catch(IOException e){
			throw new GdxRuntimeException(e);
		}
	}
	
	public int getWidth(){
		return textureRaw.getWidth();
	}
	public int getHeight(){
		return textureRaw.getHeight();
	}
	
	public Cubemap createEnvMap(int size){
		return equirectangularToCube(textureRaw, size, 1f / 2.2f); // TODO this is gamma correction actually... maybe we don't want to do that here...
	}
	
	private void createTexture(Header hdrHeader, byte[] hdrData){
		GLOnlyTextureData data = new GLOnlyTextureData(hdrHeader.getWidth(), hdrHeader.getHeight(), 0, GL30.GL_RGB32F, GL30.GL_RGB, GL30.GL_FLOAT);
		textureRaw = new Texture(data);
    	FloatBuffer buffer = BufferUtils.newFloatBuffer(hdrHeader.getWidth() * hdrHeader.getHeight() * 3);
    	float [] pixels = new float[3];
    	for(int i=0 ; i<hdrData.length ; i+=4){
    		RGBE.rgbe2float(pixels, hdrData, i);
    		buffer.put(pixels);
    	}
    	buffer.flip();
    	textureRaw.bind();
    	Gdx.gl.glTexImage2D(textureRaw.glTarget, 0, GL30.GL_RGB32F, hdrHeader.getWidth(), hdrHeader.getHeight(), 0, GL30.GL_RGB, GL30.GL_FLOAT, buffer);
	}
	
	private Cubemap equirectangularToCube(Texture hdrTexture, int size, float exposure){
		ShaderProgram rectToCubeShader = loadShader("shaders/hdri/cubemap-make", "#define GAMMA_CORRECTION\n");
		ShapeRenderer shapes = new ShapeRenderer(20, rectToCubeShader);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		
		int width = size;
		int height = size;
		
		FrameBufferCubemapBuilder b = new FrameBufferCubemapBuilder(width, height);
		b.addColorTextureAttachment(GL30.GL_RGB32F, GL30.GL_RGB, GL30.GL_FLOAT);
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
		
		return fboEnv.getColorBufferTexture();
	}
	
	private static ShaderProgram loadShader(String name, String options){
		ShaderProgram shader = new ShaderProgram(
				options + Gdx.files.classpath(name + ".vs.glsl").readString(), 
				options + Gdx.files.classpath(name + ".fs.glsl").readString());
		if(!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());
		if(shader.getLog().length() > 0) Gdx.app.error(HDRI.class.getSimpleName(), shader.getLog());
		return shader;
	}
	
}
