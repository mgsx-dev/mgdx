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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.utils.Array;
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
import net.mgsx.ktx2.KTX2Data.MipMapMode;
import net.mgsx.ktx2.KTX2Processor;
import net.mgsx.ktx2.KTX2TextureData;

public class IBL implements Disposable
{
	public static class IBLBakingOptions {
		public GLFormat format = GLFormat.RGB16;
		public int envSize = 2048;
		public int radSize = 512;
		public int irdSize = 32;
	}
	
	public Cubemap environmentCubemap;
	
	/** AKA irradiance map, Lambertian */
	public Cubemap diffuseCubemap;
	
	/** AKA radiance map, GGX */
	public Cubemap specularCubemap;
	
	public Texture brdfLUT;
	
	public static boolean useKtx2 = true;
	public static boolean useCompression = true;
	
	public static IBL fromHDR(FileHandle hdrFile, boolean useCache){
		return fromHDR(hdrFile, new IBLBakingOptions(), useCache);
	}
	
	public static IBL fromHDR(FileHandle hdrFile, IBLBakingOptions options, boolean useCache){
		
		IBL ibl = new IBL();
//		long ptime = System.currentTimeMillis();
		
		ibl.environmentCubemap = fromCache(hdrFile, "env", options.envSize, false, options.format, useCache, ()->{
			Texture hdri = new HDRILoader().load(hdrFile, GLFormat.RGB32); // TODO 16 or 32 ??
			EnvironmentMapBaker envBaker = new EnvironmentMapBaker(hdri);
			Cubemap map = envBaker.createEnvMap(options.envSize, options.format, true);
			envBaker.dispose();
			hdri.dispose();
			return map;
		});
		
		ibl.specularCubemap = fromCache(hdrFile, "specular", options.radSize, true, options.format, useCache, ()->{
			RadianceBaker radBaker = new RadianceBaker();
			Cubemap map = radBaker.createRadiance(ibl.environmentCubemap, options.radSize, options.format);
			radBaker.dispose();
			return map;
		});
		
		ibl.diffuseCubemap = fromCache(hdrFile, "diffuse", options.irdSize, false, options.format, useCache, ()->{
			IrradianceBaker irdBaker = new IrradianceBaker();
			Cubemap map = irdBaker.createIrradiance(ibl.environmentCubemap, options.irdSize, options.format);
			irdBaker.dispose();
			return map;
		});
		
//		System.out.println(System.currentTimeMillis() - ptime);
		
		ibl.loadDefaultLUT();
		
		return ibl;
		
	}
	
	private static Cubemap fromCache(FileHandle source, String tag, int size, boolean mipmaps, GLFormat format, boolean enabled, Supplier<Cubemap> baker){
		if(useKtx2){
			return fromCacheKtx2(source, tag, size, mipmaps, format, enabled, baker);
		}else{
			return fromCacheRaw(source, tag, size, mipmaps, format, enabled, baker);
		}
	}
	private static Cubemap fromCacheKtx2(FileHandle source, String tag, int size, boolean mipmaps, GLFormat format,
			boolean enabled, Supplier<Cubemap> baker) {
		FileHandle cache = Gdx.files.local("cache/ibl-" + source.nameWithoutExtension() + 
				"-" + tag + ".ktx2");
		Cubemap map = null;
		if(cache.exists() && enabled){
			KTX2TextureData data = new KTX2TextureData(cache);
			data.prepare();
			if(data.getWidth() == size && data.getHeight() == size && data.getTarget() == GL20.GL_TEXTURE_CUBE_MAP){
				map = new Cubemap(data);
				if(mipmaps){
					map.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
				}else{
					map.setFilter(TextureFilter.Linear, TextureFilter.Linear);
				}
			}
		}
		if(map == null){
			map = baker.get();
			if(enabled){
				writeCacheKtx2(map, cache, size, mipmaps, format);
			}
		}
		return map;
	}
	private static void writeCacheKtx2(Cubemap map, FileHandle file, int size, boolean mipmaps, GLFormat format) {
		exportToKtx2(map, file, mipmaps, format, useCompression);
	}
	public static void exportToKtx2(Cubemap map, FileHandle file, boolean mipmaps, GLFormat format, boolean compression) {
		int size = map.getWidth(); // assuming all faces are square.
		int mipmapCount = mipmaps ? RadianceBaker.sizeToPOT(size)+1 : 1;
		int w = size;
		int h = size;
		Array<ByteBuffer> buffers = new Array<ByteBuffer>();
		GLFormat gpuFormat = format.pack();
		map.bind();
		for(int l=0 ; l<mipmapCount ; l++){
			int bufferSize = w*h*gpuFormat.bppGpu;
			for(int f=0 ; f<6 ; f++){
				ByteBuffer buffer = BufferUtils.newByteBuffer(bufferSize);
				Mgdx.glMax.glGetTexImage(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X+f, l, gpuFormat.format, gpuFormat.type, buffer);
				buffers.add(buffer);
			}
			w/=2;
			h/=2;
		}
		KTX2Processor.exportCubemap(file, buffers, size, size, mipmapCount, 1, format.internalFormat, mipmaps ? MipMapMode.RAW : MipMapMode.NONE, compression);
	}

	private static Cubemap fromCacheRaw(FileHandle source, String tag, int size, boolean mipmaps, GLFormat format, boolean enabled, Supplier<Cubemap> baker){
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
				writeCacheRaw(map, cache, mipmaps, format);
			}
		}
		return map;
	}

	private static void writeCacheRaw(Cubemap map, FileHandle file, boolean mipmaps, GLFormat format) {
		int w = map.getWidth();
		int h = map.getHeight();
		int mipmapCount = mipmaps ? RadianceBaker.sizeToPOT(Math.min(w, h))+1 : 1;
		map.bind();
		try {
			OutputStream output = file.write(false);
			GZIPOutputStream gzip = new GZIPOutputStream(output);
			for(int l=0 ; l<mipmapCount ; l++){
				ByteBuffer buffer = BufferUtils.newByteBuffer(w * h * format.bppCpu);
				for(int i=0 ; i<6 ; i++){
					Mgdx.glMax.glGetTexImage(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, l, format.format, format.type, buffer);
					byte [] tmp = new byte[buffer.limit()];
					buffer.get(tmp);
					buffer.rewind();
					gzip.write(tmp);
				}
				w /= 2;
				h /= 2;
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
		if(diffuseCubemap != null){
			sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
		}
		if(specularCubemap != null){
			sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
		}
		if(brdfLUT != null){
			sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
		}
	}
	
	public static void remove(SceneManager sceneManager) {
		sceneManager.environment.remove(PBRCubemapAttribute.DiffuseEnv);
		sceneManager.environment.remove(PBRCubemapAttribute.SpecularEnv);
		sceneManager.environment.remove(PBRTextureAttribute.BRDFLUTTexture);
	}
	
	public void load(String folderPath, String extension){
		diffuseCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				folderPath + "/diffuse/diffuse_", "." + extension, EnvironmentUtil.FACE_NAMES_NEG_POS);
		
		environmentCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				folderPath + "/environment/environment_", "." + extension, EnvironmentUtil.FACE_NAMES_NEG_POS);
		
		specularCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				folderPath + "/specular/specular_", "_", "." + extension, 10, EnvironmentUtil.FACE_NAMES_NEG_POS);

		loadDefaultLUT();
	}
	
	public void load(FileHandle folder, String extension){
		load(folder.path(), extension);
	}

	@Override
	public void dispose() {
		if(diffuseCubemap != null){
			diffuseCubemap.dispose();
		}
		if(environmentCubemap != null){
			environmentCubemap.dispose();
		}
		if(specularCubemap != null){
			specularCubemap.dispose();
		}
		if(brdfLUT != null){
			brdfLUT.dispose();
		}
	}

	public void loadDefaultLUT() {
		brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
	}

	public static IBL load(FileHandle envFile, FileHandle diffuseFile, FileHandle specularFile) {
		IBL ibl = new IBL();
		ibl.environmentCubemap = loadCubemap(envFile, false);
		ibl.diffuseCubemap = loadCubemap(diffuseFile, false);
		ibl.specularCubemap = loadCubemap(specularFile, true);
		ibl.loadDefaultLUT();
		return ibl;
	}

	private static Cubemap loadCubemap(FileHandle file, boolean mipmaps) {
		KTX2TextureData data = new KTX2TextureData(file);
		data.prepare();
		Cubemap map = new Cubemap(data);
		if(mipmaps){
			map.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
		}else{
			map.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		return map;
	}
}
