package net.mgsx.gdx.gltf.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import net.mgsx.gdx.graphics.g2d.HDRI;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;

public class IBL implements Disposable
{
	private Cubemap diffuseCubemap;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private Texture brdfLUT;

	public IBL() {
		
	}
	
	public void loadHDRI(FileHandle hdrFile){
		HDRI hdri = new HDRI();
		hdri.loadHDR(hdrFile);
		int w = hdri.getWidth();
		int h = hdri.getHeight();
		int s = Math.min(w, h);
		s = Math.min(4096, s); // limit to 4k
		if(environmentCubemap != null){
			environmentCubemap.dispose();
		}
		environmentCubemap = hdri.createEnvMap(s);
	}
	
	public Cubemap getEnvironmentCubemap() {
		return environmentCubemap;
	}
	
	public void apply(SceneManager sceneManager){
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
		
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));

		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
		
		sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0f));
		
		sceneManager.setAmbientLight(1f);
	}
	
	public void load(String folderPath, String extension, boolean loadEnv){
		diffuseCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				folderPath + "/diffuse/diffuse_", "." + extension, EnvironmentUtil.FACE_NAMES_NEG_POS);
		
		if(loadEnv){
			environmentCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
					folderPath + "/environment/environment_", "." + extension, EnvironmentUtil.FACE_NAMES_NEG_POS);
		}
		
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
