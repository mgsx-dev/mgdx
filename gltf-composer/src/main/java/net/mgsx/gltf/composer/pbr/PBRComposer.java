package net.mgsx.gltf.composer.pbr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;

import net.mgsx.gdx.MgdxGame;
import net.mgsx.gdx.desktop.MGdxDekstopApplication;
import net.mgsx.gltf.exporters.GLTFExporter;
import net.mgsx.gltf.exporters.GLTFExporterConfig;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/**
 * Command line tool in order to generate textured materials from palette.
 * 
 * Conventions :
 * from input png : first pixels until full transparent defines the palette. Other pixels are the base map.
 * from input materials : materials are named as "Mx-name" with x the zero-based index and name is free (eg. M3-glass).
 * from input scene : all materials using input png texture as base color is converted to a combined material.
 * to output scene : textures are named like base texture plus layer suffix (eg. -albedo).
 * 
 * TODO provide another mode where all materials are baked into one by combining parts ? 
 * 		problem is we don't know which meshes to combine together... need a naming convention.
 * 
 * @author mgsx
 *
 */
public class PBRComposer extends MgdxGame {

	public static void main(String[] args) {
		
		new MGdxDekstopApplication(new PBRComposer(args));
	}

	private static void printHelp() {
		System.out.println("Usage: PBRComposer <input png> <input materials> <input scene> <output scene>");
	}

	private String[] args;
	
	public PBRComposer(String[] args) {
		this.args = args;
	}
	
	@Override
	public void create() {
		super.create();
		process();
		Gdx.app.exit();
	}

	private void process() {
		if(args.length < 4){
			printHelp();
			return;
		}
		FileHandle inputMapFile = Gdx.files.local(args[0]);
		FileHandle inputMaterialsFile = Gdx.files.local(args[1]);
		FileHandle inputSceneFile = Gdx.files.local(args[2]);
		FileHandle outputSceneFile = Gdx.files.local(args[3]);
		
		Pixmap inputMap = new Pixmap(inputMapFile);
		SceneAsset inputMaterials = new GLTFLoader().load(inputMaterialsFile);
		SceneAsset inputScene = new GLTFLoader().load(inputSceneFile);
		
		// first parse filename
		String baseName = inputMapFile.nameWithoutExtension();
		
		// make palette
		IntMap<Material> pixelMap = new IntMap<Material>();
		int width = inputMap.getWidth();
		int height = inputMap.getHeight();
		boolean paletteOver = false;
		for(int row=0 ; row<height && !paletteOver ; row++){
			for(int col=0 ; col<width ; col++){
				int pixel = inputMap.getPixel(col, row);
				if((pixel & 0xFF) == 0){
					paletteOver = true;
					break;
				}
				int index = row * width + col;
				Material foundMaterial = null;
				for(Material material : inputMaterials.scene.model.materials){
					if(material.id.startsWith("M" + index + "-")){
						if(foundMaterial != null) throw new GdxRuntimeException("input material name conflicts for index " + index);
						foundMaterial = material;
					}
				}
				if(foundMaterial != null){
					if(pixelMap.containsKey(pixel)){
						throw new GdxRuntimeException("duplicated palette entry at " + col + "," + row);
					}
					pixelMap.put(pixel, foundMaterial);
				}
			}
		}
		
		// TODO expand with emissive, etc...
		
		// create material
		Material result = inputScene.scene.model.materials.first();
		Pixmap baseColor = new Pixmap(width, height, Format.RGBA8888);
		baseColor.setBlending(Blending.None);
		Pixmap orm = new Pixmap(width, height, Format.RGB888);
		orm.setBlending(Blending.None);
		
		// apply palette
		Color baseColorValue = new Color();
		for(int y=0 ; y<height ; y++){
			for(int x=0 ; x<width ; x++){
				int color = inputMap.getPixel(x, y);
				// skip full transparent
				if((color & 0xFF)== 0) continue;
				
				Material material = pixelMap.get(color);
				if(material == null){
					throw new GdxRuntimeException("no material found for color " + color + " at " + x + "," + y);
				}
				{
					PBRColorAttribute a = material.get(PBRColorAttribute.class, PBRColorAttribute.BaseColorFactor);
					if(a != null){
						baseColorValue.set(a.color);
					}else{
						baseColorValue.set(Color.WHITE);
					}
					baseColor.drawPixel(x, y, Color.rgba8888(baseColorValue));
				}
				float metallic = 0;
				float rougness = .5f;
				float ambientOcclusion = 1;
				{
					PBRFloatAttribute a = material.get(PBRFloatAttribute.class, PBRFloatAttribute.Metallic);
					if(a != null){
						metallic = a.value;
					}
				}
				{
					PBRFloatAttribute a = material.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness);
					if(a != null){
						rougness = a.value;
					}
				}
				
				orm.drawPixel(x, y, Color.rgba8888(ambientOcclusion, rougness, metallic, 1));
			}
		}
		
		Texture baseColorTexture = new Texture(baseColor);
		
		{
			PBRTextureAttribute a = PBRTextureAttribute.createBaseColorTexture(baseColorTexture);
			// XXX keep original and workaround for TODO  material exporter (null filter should be handled)
			a.textureDescription.minFilter = TextureFilter.Nearest;
			a.textureDescription.magFilter = TextureFilter.Nearest;
			a.textureDescription.uWrap =
			a.textureDescription.vWrap = TextureWrap.ClampToEdge;
			
			result.set(a);
		}
		
		Texture ormTexture = new Texture(orm);
		{
			PBRTextureAttribute a = PBRTextureAttribute.createMetallicRoughnessTexture(ormTexture);
			// XXX keep original and workaround for TODO  material exporter (null filter should be handled)
			a.textureDescription.minFilter = TextureFilter.Nearest;
			a.textureDescription.magFilter = TextureFilter.Nearest;
			a.textureDescription.uWrap =
			a.textureDescription.vWrap = TextureWrap.ClampToEdge;
					
			result.set(a);
		}
		
		// save as GLTF
		GLTFExporterConfig config = new GLTFExporterConfig();
		new GLTFExporter(config){
			protected String getImageName(Texture texture) {
				if(texture == baseColorTexture){
					return baseName + "-albedo";
				}
				if(texture == ormTexture){
					return baseName + "-orm";
				}
				return super.getImageName(texture);
			}
		}.export(inputScene, outputSceneFile);
	}
}
