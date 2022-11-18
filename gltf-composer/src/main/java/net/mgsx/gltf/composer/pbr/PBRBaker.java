package net.mgsx.gltf.composer.pbr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gdx.MgdxGame;
import net.mgsx.gdx.desktop.MGdxDekstopApplication;
import net.mgsx.gltf.exporters.GLTFExporter;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRVolumeAttribute;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class PBRBaker extends MgdxGame {

	public static void main(String[] args) {
		// new PBRBaker().testBake();
		new MGdxDekstopApplication(new PBRBaker(args));
	}
	
	private void testCompose(){
		FileHandle base = Gdx.files.local("pbr-example/raw_materials");
		SceneAsset asset = new GLTFLoader().load(base.child("materials.gltf"));
		
		ObjectMap<Texture, String> textureToName = new ObjectMap<Texture, String>();
		
		for(Material mat : asset.scene.model.materials){
			System.out.println(mat.id + ":");
			
			// Base color
			FileHandle baseColorFile = base.child(mat.id + "-colors.png");
			if(baseColorFile.exists()){
				Texture baseColorTexture = new Texture(baseColorFile, false); // TODO mipmaps
				textureToName.put(baseColorTexture, baseColorFile.nameWithoutExtension());
				System.out.println("base color texture detected");
				mat.set(PBRColorAttribute.createBaseColorFactor(Color.WHITE));
				mat.set(PBRTextureAttribute.createBaseColorTexture(baseColorTexture));
				
			}else{
				System.out.println("base color not found");
				Color color = mat.get(PBRColorAttribute.class, PBRColorAttribute.BaseColorFactor).color;
				System.out.println("using uniform color: " + color);
			}
			
			// Emissive
			FileHandle emissiveFile = base.child(mat.id + "-emissive.png");
			if(emissiveFile.exists()){
				Texture missiveTexture = new Texture(emissiveFile, false); // TODO mipmaps
				textureToName.put(missiveTexture, emissiveFile.nameWithoutExtension());
				System.out.println("emissive texture detected");
				mat.set(PBRColorAttribute.createEmissive(Color.WHITE));
				mat.set(PBRTextureAttribute.createEmissiveTexture(missiveTexture));
				
			}else{
				System.out.println("emissive not found");
				ColorAttribute attr = mat.get(ColorAttribute.class, ColorAttribute.Emissive);
				Color color = attr != null ? attr.color : Color.BLACK;
				System.out.println("using uniform color: " + color);
			}
			
			// normals
			FileHandle normalsFile = base.child(mat.id + "-normals.png");
			if(normalsFile.exists()){
				Texture normalTexture = new Texture(normalsFile, false); // TODO mipmaps
				textureToName.put(normalTexture, normalsFile.nameWithoutExtension());
				System.out.println("normal texture detected");
				mat.set(PBRTextureAttribute.createNormalTexture(normalTexture));
				mat.set(PBRFloatAttribute.createNormalScale(1));
			}else{
				System.out.println("normals not found");
			}
			
			// AO-Metal-roughness
			Pixmap ORM = null;
			FileHandle metallicFile = base.child(mat.id + "-metallic.png");
			Pixmap metalPix = metallicFile.exists() ? new Pixmap(metallicFile) : null;
			if(metalPix != null){
				ORM = new Pixmap(metalPix.getWidth(), metalPix.getHeight(), Format.RGB888);
			}
			
			FileHandle roughnessFile = base.child(mat.id + "-roughness.png");
			Pixmap roughPix = roughnessFile.exists() ? new Pixmap(roughnessFile) : null;
			if(roughPix != null){
				if(ORM == null){
					ORM = new Pixmap(roughPix.getWidth(), roughPix.getHeight(), Format.RGB888);
				}else{
					if(ORM.getWidth() != roughPix.getWidth() || ORM.getHeight() != roughPix.getHeight()){
						throw new GdxRuntimeException("ORM maps mismatch");
					}
				}
			}
			
			FileHandle aoFile = base.child(mat.id + "-ao.png");
			Pixmap aoPix = aoFile.exists() ? new Pixmap(aoFile) : null;
			if(aoPix != null){
				if(ORM == null){
					ORM = new Pixmap(aoPix.getWidth(), aoPix.getHeight(), Format.RGB888);
				}else{
					if(ORM.getWidth() != aoPix.getWidth() || ORM.getHeight() != aoPix.getHeight()){
						throw new GdxRuntimeException("ORM maps mismatch");
					}
				}
			}
			
			if(ORM != null){
				ORM.setBlending(Blending.None);
				Color in = new Color();
				for(int y=0 ; y<ORM.getHeight() ; y++){
					for(int x=0 ; x<ORM.getWidth() ; x++){
						float roughness = 0.5f;
						float metal = 0f;
						float ao = 1f;
						if(metalPix != null){
							in.set(metalPix.getPixel(x, y));
							metal = in.r;
						}else{
							metal = mat.get(PBRFloatAttribute.class, PBRFloatAttribute.Metallic).value;
						}
						if(roughPix != null){
							in.set(roughPix.getPixel(x, y));
							roughness = in.r;
						}else{
							roughness = mat.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness).value;
						}
						if(aoPix != null){
							in.set(aoPix.getPixel(x, y));
							ao = in.r;
						}else{
							ao = 1;
						}
						ORM.drawPixel(x, y, Color.rgba8888(ao, roughness, metal, 1));
					}
				}
				Texture ORMTexture = new Texture(ORM);
				textureToName.put(ORMTexture, mat.id + "-orm");
				mat.set(PBRTextureAttribute.createMetallicRoughnessTexture(ORMTexture));
				mat.set(PBRFloatAttribute.createMetallic(1));
				mat.set(PBRFloatAttribute.createRoughness(1));
				if(aoPix != null){
					mat.set(PBRTextureAttribute.createOcclusionTexture(ORMTexture));
					mat.set(PBRFloatAttribute.createOcclusionStrength(1f));
				}
			}
			
			// Transmission
			// AO-Metal-roughness
			Pixmap TT = null;
			FileHandle transmissionFile = base.child(mat.id + "-transmission.png");
			Pixmap transmissionPix = transmissionFile.exists() ? new Pixmap(transmissionFile) : null;
			if(transmissionPix != null){
				TT = new Pixmap(transmissionPix.getWidth(), transmissionPix.getHeight(), Format.RGB888);
			}
			
			FileHandle thicknessFile = base.child(mat.id + "-thickness.png");
			Pixmap thicknessPix = thicknessFile.exists() ? new Pixmap(thicknessFile) : null;
			if(thicknessPix != null){
				if(TT == null){
					TT = new Pixmap(thicknessPix.getWidth(), thicknessPix.getHeight(), Format.RGB888);
				}else{
					if(TT.getWidth() != thicknessPix.getWidth() || TT.getHeight() != thicknessPix.getHeight()){
						throw new GdxRuntimeException("TT maps mismatch");
					}
				}
			}
			
			if(TT != null){
				TT.setBlending(Blending.None);
				Color in = new Color();
				for(int y=0 ; y<TT.getHeight() ; y++){
					for(int x=0 ; x<TT.getWidth() ; x++){
						float transmission;
						float thickness;
						if(transmissionPix != null){
							in.set(transmissionPix.getPixel(x, y));
							transmission = in.r;
						}else{
							PBRFloatAttribute attr = mat.get(PBRFloatAttribute.class, PBRFloatAttribute.TransmissionFactor);
							if(attr != null){
								transmission = attr.value;
							}else{
								transmission = 1;
							}
						}
						if(thicknessPix != null){
							in.set(thicknessPix.getPixel(x, y));
							thickness = in.r;
						}else{
							PBRVolumeAttribute attr = mat.get(PBRVolumeAttribute.class, PBRVolumeAttribute.Type);
							if(attr != null){
								thickness = attr.thicknessFactor;
							}else{
								thickness = 1;
							}
						}
						TT.drawPixel(x, y, Color.rgba8888(transmission, thickness, 0, 1));
					}
				}
				Texture TTTexture = new Texture(TT);
				textureToName.put(TTTexture, mat.id + "-tt");
				if(transmissionPix != null){
					mat.set(PBRTextureAttribute.createTransmissionTexture(TTTexture));
					mat.set(PBRFloatAttribute.createTransmissionFactor(1));
				}
				if(thicknessPix != null){
					mat.set(PBRTextureAttribute.createThicknessTexture(TTTexture));
//					PBRVolumeAttribute attr = mat.get(PBRVolumeAttribute.class, PBRVolumeAttribute.Type);
//					if(attr != null){
//						attr.thicknessFactor = 1;
//					}
				}
			}
			
			// simply recompose textures, and export as a new GLTF
		}
		
		
		boolean export = true;
		if(export){
			GLTFExporter exporter = new GLTFExporter(){
				@Override
				protected String getImageName(Texture texture) {
					return textureToName.get(texture);
				}
			};
			FileHandle exportBase = base.child("gltf");
			exportBase.mkdirs();
			exporter.export(asset, exportBase.child("composed.gltf"));
		}
		asset.dispose();
	}

	private boolean testBake(){
		return 0 == new CommandRunner()
			.args("blender", "-b", "--python-console")
			.run((in,out,err)->{
				// TODO use a template !
				in.write("import bpy\n");
				in.write("print(bpy.data.objects[0].name)\n");
				in.write("print(bpy.data.objects[0].location)\n");
				in.close();
				String line;
				
				
				while((line = out.readLine()) != null){
					System.out.println(line);
				}
				while((line = err.readLine()) != null){
					System.err.println(line);
				}
			});
	}

	private String[] args;
	
	public PBRBaker(String... args) {
		this.args = args;
		
	}
	
	@Override
	public void create() {
		super.create();
		if(args.length > 0){
			String cmd = args[0];
			for(int i=1 ; i<args.length ; i++){
				// TODO options
			}
			if(cmd.equals("check")){
				if(!check()){
					System.err.println("Error");
				}
			}else if(cmd.equals("compose")){
				testCompose();
			}
		}else{
			printHelp();
		}
		Gdx.app.exit();
	}

	private boolean check() {
		return 0 == new CommandRunner()
			.args("blender", "-v")
			.run((in,out,err)->{
				String firstLine = out.readLine();
				String[] nameVersion = firstLine.split(" ", 2);
				if(!nameVersion[0].equals("Blender") || nameVersion.length < 2){
					throw new GdxRuntimeException("Expected Blender x.y.z, get " + nameVersion[0]);
				}
				String[] version = nameVersion[1].split("\\.", 3);
				if(version.length < 3){
					throw new GdxRuntimeException("error parsing version: " + nameVersion[1]);
				}
				String major = version[0];
				String minor = version[1];
				String patch = version[2];
				if(!major.equals("3")){
					throw new GdxRuntimeException("Expected Blender 3.x, get " + nameVersion[1]);
				}
				System.out.println("Success: " + firstLine);
			});
	}
	
	private boolean testScript(){
		return 0 == new CommandRunner()
			.args("blender", "-b", "--python-console")
			.run((in,out,err)->{
				in.write("import bpy\n");
				in.write("print(bpy.data.objects[0].name)\n");
				in.write("print(bpy.data.objects[0].location)\n");
				in.close();
				String line;
				
				
				while((line = out.readLine()) != null){
					System.out.println(line);
				}
				while((line = err.readLine()) != null){
					System.err.println(line);
				}
			});
	}
	
	private static class CommandRunner {
		
		@FunctionalInterface
		public interface CommandStream {
			public void process(BufferedWriter in, BufferedReader out, BufferedReader err) throws IOException;
		}
		
		Array<String> cmd = new Array<String>(String.class);
		public CommandRunner args(String ...args) {
			cmd.addAll(args);
			return this;
		}
		public int run(CommandStream callback) {
			try {
				Process p = Runtime.getRuntime().exec(cmd.toArray());
				BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()), 500000);
				BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()), 500000);
				BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
				callback.process(stdin, stdout, stderr);
				p.waitFor();
				//stdin.close();
				stdout.close();
				stderr.close();
				return p.exitValue();
			} catch (IOException e) {
				throw new GdxRuntimeException("Error executing " + cmd.toString(" "), e);
			} catch (InterruptedException e) {
				throw new GdxRuntimeException("Timeout executing " + cmd.toString(" "), e);
			}
		}
	}
	

	private void printHelp() {
		System.out.println("Usage: <command> [options]");
		System.out.println("Commands:");
		System.out.println("check:\t verify presence of Blender and version.");
		System.out.println("bake <input blender file> <out folder>:\tBake content to a folder.");

	}
}
