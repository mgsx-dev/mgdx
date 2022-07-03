package net.mgsx.gltf.composer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.mgsx.gdx.graphics.cameras.BlenderCamera;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.ibl.IBL;

public class GLTFComposerContext {
	public Skin skin;
	public Stage stage;
	
	public PBRShaderConfig colorShaderConfig;
	public DepthShader.Config depthShaderConfig;
	public SceneManager sceneManager;
	public SceneSkybox skyBox;
	public IBL ibl;
	public DirectionalLightEx keyLight = new DirectionalLightEx();
	
	public SceneAsset asset;
	public Scene scene;
	
	public final Color clearColor = new Color(.2f,.2f,.2f,0f);
	public BlenderCamera cameraManager;


	public boolean sceneJustChanged = false;

	private boolean shadersValid = false;
	
	public void invalidateShaders(){
		shadersValid = false;
	}
	
	public void validate(){
		if(!shadersValid){
			shadersValid = true;

			colorShaderConfig.numDirectionalLights = 1;
			colorShaderConfig.numPointLights = 0;
			colorShaderConfig.numSpotLights = 0;
			
			if(asset != null){
				colorShaderConfig.numBones = asset.maxBones;
				depthShaderConfig.numBones = asset.maxBones;
			}else{
				colorShaderConfig.numBones = 0;
				depthShaderConfig.numBones = 0;
			}
			
			sceneManager.setShaderProvider(new PBRShaderProvider(colorShaderConfig));
			sceneManager.setDepthShaderProvider(new PBRDepthShaderProvider(depthShaderConfig));
					
			sceneManager.environment.remove(keyLight);
			sceneManager.environment.add(keyLight);
			
			if(skyBox != null){
				sceneManager.setSkyBox(null);
				skyBox.dispose();
			}
			if(ibl != null){
				skyBox = new SceneSkybox(ibl.getEnvironmentCubemap(), colorShaderConfig.manualSRGB, colorShaderConfig.manualGammaCorrection);
				sceneManager.setSkyBox(skyBox);
			}
		}
	}

}
