package net.mgsx.ibl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.graphics.g2d.HDRILoader;
import net.mgsx.gdx.graphics.glutils.FlexCubemapData;
import net.mgsx.gdx.utils.FileHandleUtils;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;

public class IBL implements Disposable
{
	private Cubemap diffuseCubemap;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private Texture brdfLUT;
	
	public static IBL fromHDR(FileHandle hdrFile, boolean useCache){
		
		GLFormat format = GLFormat.RGB32;
		final int envSize = 2048;
		final int radSize = 512;
		final int irdSize = 32;
		
		IBL ibl = new IBL();
		
		ibl.environmentCubemap = fromCache(hdrFile, "env", envSize, false, format, useCache, ()->{
			Texture hdri = new HDRILoader().load(hdrFile, GLFormat.RGB32);
			EnvironmentMapBaker envBaker = new EnvironmentMapBaker(hdri);
			Cubemap map = envBaker.createEnvMap(envSize, format, true);
			envBaker.dispose();
			hdri.dispose();
			return map;
		});
		
		
		ibl.specularCubemap = fromCache(hdrFile, "specualr", radSize, true, format, useCache, ()->{
			RadianceBaker radBaker = new RadianceBaker();
			Cubemap map = radBaker.createRadiance(ibl.environmentCubemap, radSize, format);
			radBaker.dispose();
			return map;
		});
		
		ibl.diffuseCubemap = fromCache(hdrFile, "diffuse", irdSize, false, format, useCache, ()->{
			IrradianceBaker irdBaker = new IrradianceBaker();
			Cubemap map = irdBaker.createIrradiance(ibl.environmentCubemap, irdSize, format);
			irdBaker.dispose();
			return map;
		});
		
		ibl.brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
		
		return ibl;
		
	}
	
	private static Cubemap fromCache(FileHandle source, String tag, int size, boolean mipmaps, GLFormat format, boolean enabled, Supplier<Cubemap> baker){
		
		// TODO use KTX2 instead !
		
		Cubemap map = null;
		FileHandle cache = null;
		if(enabled){
			int w = size;
			int h = size;
			int mipmapCount = mipmaps ? RadianceBaker.sizeToPOT(size)+1 : 1;
			cache = Gdx.files.local("cache/ibl-" + source.nameWithoutExtension() + 
					"-" + tag + "-" + w + "x" + h + "-" + format.internalFormat + ".dat");
			if(cache.exists()){
				FlexCubemapData data = new FlexCubemapData(w, h, mipmapCount, format, 
						FileHandleUtils.asByteBuffer(cache.read(), true, 
								w * h * 6 * format.bppCpu * mipmapCount));
				map = new Cubemap(data);
			}
		}
		if(map == null){
			map = baker.get();
			if(cache != null){
				writeCache(map, cache, format);
			}
		}
		return map;
	}
	
	private static void writeCache(Cubemap map, FileHandle file, GLFormat format) {
		map.bind();
		ByteBuffer buffer = BufferUtils.newByteBuffer(map.getWidth() * map.getHeight() * format.bppCpu);
		try {
			OutputStream output = file.write(false);
			GZIPOutputStream gzip = new GZIPOutputStream(output);
			for(int i=0 ; i<6 ; i++){
				Mgdx.glMax.glGetTexImage(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 0, format.format, format.type, buffer);
				byte [] tmp = new byte[buffer.limit()];
				buffer.get(tmp);
				buffer.rewind();
				gzip.write(tmp);
			}
			gzip.close();
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public Cubemap getEnvironmentCubemap() {
		return environmentCubemap;
	}
	
	public void apply(SceneManager sceneManager){
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
	}
	
	public void load(String folderPath, String extension){
		diffuseCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				folderPath + "/diffuse/diffuse_", "." + extension, EnvironmentUtil.FACE_NAMES_NEG_POS);
		
		environmentCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				folderPath + "/environment/environment_", "." + extension, EnvironmentUtil.FACE_NAMES_NEG_POS);
		
		specularCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				folderPath + "/specular/specular_", "_", "." + extension, 10, EnvironmentUtil.FACE_NAMES_NEG_POS);

		brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
	}

	@Override
	public void dispose() {
		diffuseCubemap.dispose();
		environmentCubemap.dispose();
		specularCubemap.dispose();
		brdfLUT.dispose();
	}
}
