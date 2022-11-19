package net.mgsx.gltf.composer.pbr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRVolumeAttribute;

// TODO transmitting materials should stay separated (maybe another palette)
public class PBRPainter implements Disposable
{

	private final Color color = new Color();
	private final Color colorFactor = new Color();
	private Pixmap baseColorMap;
	private Pixmap ormMap;
	private Pixmap normalMap;
	private Pixmap emissiveMap;
	private Pixmap ttMap;
	private ObjectMap<Texture, Pixmap> pixmaps = new ObjectMap<Texture, Pixmap>();
	private int width;
	private int height;
	
	public PBRPainter(int width, int height, Array<Material> materials){
		this.width = width;
		this.height = height;
		
		// TODO
		// check every materials to determine which maps should be created
		// and find some scaling factors
		// eg. we don't want transmission layer if there is no transmission.
		
		
		baseColorMap = createMap(width, height, Format.RGBA8888);
		ormMap = createMap(width, height, Format.RGB888);
		normalMap = createMap(width, height, Format.RGB888);
		emissiveMap = createMap(width, height, Format.RGB888);
		ttMap = createMap(width, height, Format.RGB888);
	}
	
	private Pixmap createMap(int width, int height, Format format) {
		Pixmap map = new Pixmap(width, height, Format.RGBA8888);
		map.setBlending(Blending.None);
		return map;
	}
	
	@Override
	public void dispose() {
		baseColorMap.dispose();
		ormMap.dispose();
		normalMap.dispose();
		emissiveMap.dispose();
	}

	public void paint(int x, int y, Material material) {
		// Base color
		{
			PBRColorAttribute a = material.get(PBRColorAttribute.class, PBRColorAttribute.BaseColorFactor);
			if(a != null){
				colorFactor.set(a.color);
			}else{
				colorFactor.set(Color.WHITE);
			}
			PBRTextureAttribute map = material.get(PBRTextureAttribute.class, PBRTextureAttribute.BaseColorTexture);
			if(map != null){
				Pixmap pix = findPixmap(map.textureDescription.texture);
				getPixel(color, pix, x, y);
			}else{
				color.set(0.5f, 0.5f, 1f, 1);
			}
			baseColorMap.drawPixel(x, y, Color.rgba8888(color.mul(colorFactor)));
		}
		// ORM
		{
			colorFactor.r = 1; // default AO
			{
				PBRFloatAttribute a = material.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness);
				if(a != null){
					colorFactor.g = a.value;
				}
			}
			{
				PBRFloatAttribute a = material.get(PBRFloatAttribute.class, PBRFloatAttribute.Metallic);
				if(a != null){
					colorFactor.b = a.value;
				}
			}
			PBRTextureAttribute map = material.get(PBRTextureAttribute.class, PBRTextureAttribute.MetallicRoughnessTexture);
			if(map != null){
				Pixmap pix = findPixmap(map.textureDescription.texture);
				getPixel(color, pix, x, y);
			}else{
				color.set(1, 1, 1, 1);
			}
			// TODO Occlusion map
			ormMap.drawPixel(x, y, Color.rgba8888(color.mul(colorFactor)));
		}
		// Normals
		{
			PBRTextureAttribute map = material.get(PBRTextureAttribute.class, PBRTextureAttribute.NormalTexture);
			if(map != null){
				Pixmap pix = findPixmap(map.textureDescription.texture);
				getPixel(color, pix, x, y);
			}else{
				color.set(0.5f, 0.5f, 1f, 1);
			}
			normalMap.drawPixel(x, y, Color.rgba8888(color));
		}
		// Emissive
		{
			ColorAttribute a = material.get(ColorAttribute.class, ColorAttribute.Emissive);
			if(a != null){
				colorFactor.set(a.color);
			}else{
				colorFactor.set(Color.WHITE);
			}
			PBRTextureAttribute map = material.get(PBRTextureAttribute.class, PBRTextureAttribute.EmissiveTexture);
			if(map != null){
				Pixmap pix = findPixmap(map.textureDescription.texture);
				getPixel(color, pix, x, y);
			}else{
				color.set(0,0,0,1);
			}
			emissiveMap.drawPixel(x, y, Color.rgba8888(color.mul(colorFactor)));
		}
		// Transmission
		{
			{
				PBRFloatAttribute a = material.get(PBRFloatAttribute.class, PBRFloatAttribute.TransmissionFactor);
				if(a != null){
					colorFactor.r = a.value;
				}else{
					colorFactor.r = 0;
				}
				PBRTextureAttribute map = material.get(PBRTextureAttribute.class, PBRTextureAttribute.TransmissionTexture);
				if(map != null){
					Pixmap pix = findPixmap(map.textureDescription.texture);
					getPixel(color, pix, x, y);
					colorFactor.r *= color.r;
				}
			}
			{
				PBRVolumeAttribute a = material.get(PBRVolumeAttribute.class, PBRVolumeAttribute.Type);
				if(a != null){
					colorFactor.g = a.thicknessFactor;
				}else{
					colorFactor.g = 1;
				}
				PBRTextureAttribute map = material.get(PBRTextureAttribute.class, PBRTextureAttribute.ThicknessTexture);
				if(map != null){
					Pixmap pix = findPixmap(map.textureDescription.texture);
					getPixel(color, pix, x, y);
					colorFactor.g *= color.g;
				}
			}
			colorFactor.b = 0;
			colorFactor.a = 1;
			ttMap.drawPixel(x, y, Color.rgba8888(colorFactor));
		}
	}
	
	private void getPixel(Color color, Pixmap pix, int x, int y) {
		if(pix.getWidth() != width || pix.getHeight() != height){
			// nearest sampling
			float u = (float)x / (float)width;
			float v = (float)y / (float)height;
			int sx = MathUtils.floor(u * pix.getWidth());
			int sy = MathUtils.floor(v * pix.getHeight());
			color.set(pix.getPixel(sx, sy));
		}else{
			color.set(pix.getPixel(x, y));
		}
	}

	private Pixmap findPixmap(Texture texture) {
		Pixmap pix = pixmaps.get(texture);
		if(pix == null){
			TextureData td = texture.getTextureData();
			if(!td.isPrepared()) td.prepare();
			pix = td.consumePixmap();
			pixmaps.put(texture, pix);
		}
		return pix;
	}

	public ObjectMap<Texture, String> patchMaterial(Material result){
		ObjectMap<Texture, String> namesMap = new ObjectMap<Texture, String>();
		setLayer(namesMap, result, PBRTextureAttribute.BaseColorTexture, baseColorMap, "-color");
		setLayer(namesMap, result, PBRTextureAttribute.MetallicRoughnessTexture, ormMap, "-orm");
		// TODO use same map for AO
		setLayer(namesMap, result, PBRTextureAttribute.NormalTexture, normalMap, "-normal");
		setLayer(namesMap, result, PBRTextureAttribute.EmissiveTexture, emissiveMap, "-emissive");
		setLayer(namesMap, result, PBRTextureAttribute.TransmissionTexture, ttMap, "-tt");
		// TODO use same map for thickness
		
		result.set(PBRColorAttribute.createBaseColorFactor(Color.WHITE));
		result.set(PBRFloatAttribute.createMetallic(1));
		result.set(PBRFloatAttribute.createRoughness(1));
		result.set(PBRFloatAttribute.createOcclusionStrength(1));
		result.set(PBRFloatAttribute.createNormalScale(1));
		result.set(ColorAttribute.createEmissive(Color.WHITE));
		result.set(PBRFloatAttribute.createEmissiveIntensity(1)); // TODO depends if there are in inputs materials ?
		result.set(PBRFloatAttribute.createTransmissionFactor(1)); // TODO depends id transmiting material
		
		return namesMap;
	}

	private void setLayer(ObjectMap<Texture, String> namesMap, Material result, long attributeType,
			Pixmap map, String suffix) {
		Texture texture = new Texture(map);
		
		{
			PBRTextureAttribute a = new PBRTextureAttribute(attributeType, texture);
			// XXX keep original and workaround for TODO  material exporter (null filter should be handled)
			
			// TODO which filter to use (should be computed from input materials)
			
			a.textureDescription.minFilter = TextureFilter.Nearest;
			a.textureDescription.magFilter = TextureFilter.Nearest;
			
			
			a.textureDescription.minFilter = TextureFilter.Linear;
			a.textureDescription.magFilter = TextureFilter.Linear;
			
			
			
			a.textureDescription.uWrap =
			a.textureDescription.vWrap = TextureWrap.ClampToEdge;
			
			result.set(a);
		}
		
		namesMap.put(texture, result.id + suffix);
	}


}
