package net.mgsx.gltf;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.Scene;

public class GLTFMaterialUtils {
	public static void forceEmissiveTextureFactor(Scene scene, float scale){
		for(Material m : scene.modelInstance.materials){
			if(m.has(PBRTextureAttribute.EmissiveTexture)){
				ColorAttribute em = m.get(ColorAttribute.class, PBRColorAttribute.Emissive);
				if(em != null){
					em.color.r = em.color.g = em.color.b = scale;
				}
			}
		}
	}
	public static void forceMetallicRoughness(Scene scene, float metallic, float roughness){
		for(Material m : scene.modelInstance.materials){
			if(m.has(PBRFloatAttribute.Metallic)){
				m.get(PBRFloatAttribute.class, PBRFloatAttribute.Metallic).value = metallic;
			}
			if(m.has(PBRFloatAttribute.Roughness)){
				m.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness).value = roughness;
			}
		}
	}

}
