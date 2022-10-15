package net.mgsx.gltfx.ktx2;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltfx.ktx2.KTX2Data.CompressionMode;
import net.mgsx.gltfx.ktx2.KTX2Data.MipMapMode;
import net.mgsx.gltfx.ktx2.KTX2Data.TextureCompression;

/**
 * cubemap example:
 * tmp/rgba.ktx2 --zlib --cubemap assets/textures/demo1/environment/environment_posx.jpg assets/textures/demo1/environment/environment_negx.jpg assets/textures/demo1/environment/environment_posy.jpg assets/textures/demo1/environment/environment_negy.jpg assets/textures/demo1/environment/environment_posz.jpg assets/textures/demo1/environment/environment_negz.jpg
 * 
 * 
 * @author mgsx
 *
 */
public class KTX2App {
	public static void main(String[] args) {
		
		GdxNativesLoader.load();
		
		// Runtime resolve Lwjgl3 backend
		// Gdx.files = new Lwjgl3Files();
		try {
			Class<Files> clazz = (Class<Files>)Class.forName("Lwjgl3Files");
			Gdx.files = clazz.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new GdxRuntimeException(e);
		}
		
		Array<String> options = new Array<String>(args);
		
		String output = options.removeIndex(0);
		
		TextureCompression textureCompression = TextureCompression.None;
		boolean cubemap = false;
		MipMapMode mipmapMode = MipMapMode.NONE;
		CompressionMode compressionMode = CompressionMode.None;
		while(options.first().startsWith("-")){
			String option = options.removeIndex(0);
			if(option.equals("--zlib") || option.equals("-z")){
				compressionMode = CompressionMode.ZLIB;
			}
			if(option.equals("--cubemap")){
				cubemap = true;
			}
			if(option.equals("--etc2")){
				textureCompression = TextureCompression.ETC2;
			}
			// TODO other options
		}
		
		Array<String> inputs = options;
		if(inputs.size == 0){
			printHelp();
			return;
		}
		
		if(cubemap){
			Array<Pixmap> pixmaps = new Array<Pixmap>();
			for(int i=0 ; i<6 ; i++) pixmaps.add(new Pixmap(Gdx.files.local(inputs.get(i))));
			KTX2Processor.exportCubemap(Gdx.files.local(output), mipmapMode, compressionMode, pixmaps);
			for(Pixmap pixmap : pixmaps) pixmap.dispose();
		}else{
			String input = inputs.first();
			Pixmap pixmap = new Pixmap(Gdx.files.local(input));
			KTX2Processor.export(Gdx.files.local(output), pixmap, mipmapMode, compressionMode, textureCompression);
			pixmap.dispose();
		}
		
	}
	
	private static void printHelp(){
		System.out.println("java -jar ktx2.jar output [options...] inputs...");
		System.out.println("--zlib, -z: use ZLIB compression");
		// System.out.println("-f, --format: VK");
	}
}
