package net.mgsx.ibl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.GLFormat;

public class RadianceBaker implements Disposable {
	
	public static int sizeToPOT(int size) {
		return MathUtils.round((float)(Math.log(size) / Math.log(2.0)));
	}
	
	private static final Matrix4 matrix = new Matrix4();
	
	private ShaderProgram randianceShader;
	private Mesh boxMesh;

	public RadianceBaker() {
		randianceShader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-radiance.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-radiance.fs.glsl"));
		if(!randianceShader.isCompiled()) throw new GdxRuntimeException(randianceShader.getLog());
		
		MeshBuilder mb = new MeshBuilder();
		mb.begin(Usage.Position, GL20.GL_TRIANGLES);
		BoxShapeBuilder.build(mb, 0,0,0,1,1,1);
		boxMesh = mb.end();
	}

	@Override
	public void dispose() {
		randianceShader.dispose();
		boxMesh.dispose();
	}
	
	public Cubemap createRadiance(Cubemap cubemap, int baseSize, GLFormat format){
		int mipMapLevels = sizeToPOT(baseSize) + 1;
		
		FlexCubemapData fcd = new FlexCubemapData(baseSize, baseSize, format, mipMapLevels);
		Cubemap map = new Cubemap(fcd);
		FlexFrameBuffer flexFbo = new FlexFrameBuffer();
		flexFbo.bind();
		cubemap.bind();
		
		for(int level=0 ; level<mipMapLevels ; level++){
			int size = 1 << (mipMapLevels - level - 1);
			flexFbo.setViewport(size,size);
			for(int s=0 ; s<6 ; s++){
				flexFbo.setAttachment(0, map, level, s);
				Gdx.gl.glClearColor(0, 0, 0, 0);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				
				CubemapSide side = CubemapSide.values()[s];
				renderSideRadiance(side, level, mipMapLevels);
				
			}
		}
		flexFbo.resetViewport();
		flexFbo.dispose();
		map.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
		return map;
	}
	
	public Array<Pixmap> createPixmaps(Cubemap cubemap, int baseSize){
		int mipMapLevels = sizeToPOT(baseSize);
		Pixmap[] maps = new Pixmap[mipMapLevels * 6];
		int index = 0;
		cubemap.bind();
		for(int level=0 ; level<mipMapLevels ; level++){
			int size = 1 << (mipMapLevels - level - 1);
			FrameBuffer fbo = new FrameBuffer(Format.RGB888, size, size, false);
			fbo.begin();
			for(int s=0 ; s<6 ; s++){
				Gdx.gl.glClearColor(0, 0, 0, 0);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				
				CubemapSide side = CubemapSide.values()[s];
				renderSideRadiance(side, level, mipMapLevels+1);
				
				maps[index] = Pixmap.createFromFrameBuffer(0, 0, size, size);
				index++;
			}
			fbo.end();
			fbo.dispose();
		}
		return new Array<Pixmap>(maps);
	}
	
	private void renderSideRadiance(CubemapSide side, int mip, int maxMipLevels) {
		
		ShaderProgram shader = randianceShader;
		
		shader.bind();
		shader.setUniformi("environmentMap", 0);
		matrix.setToProjection(.1f, 10f, 90, 1);
		shader.setUniformMatrix("projection", matrix);
		matrix.setToLookAt(side.direction, side.up);
		shader.setUniformMatrix("view", matrix);
		
		float roughness = (float)mip / (float)(maxMipLevels - 1);
	    shader.setUniformf("roughness", roughness);
		
	    boxMesh.render(shader, GL20.GL_TRIANGLES);
	}
	
}
