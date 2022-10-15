package net.mgsx.gltf.composer.core;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltfx.gfx.Bloom.BloomOptions;
import net.mgsx.ibl.IBL;
import net.mgsx.ibl.IBL.IBLBakingOptions;

public class Composition {

	public transient FileHandle file;
	
	// linked scenes
	public transient final Array<SceneAsset> sceneAssets = new Array<SceneAsset>();
	public Array<String> scenesPath = new Array<String>();

	// linked IBL and skybox
	public String hdrPath;
	public IBLBakingOptions iblBaking = new IBLBakingOptions();
	public String envPath, diffusePath, specularPath;
	
	public transient IBL ibl;
	
	// overall parameters
	boolean hdr = true;
	public Color clearColor = new Color(.2f,.2f,.2f,0f);
	
	public static class ToneMappingOptions{
		public enum ToneMappingMode {
			REINHARD, EXPOSURE, GAMMA_COMPRESSION
		}
		public ToneMappingMode mode = ToneMappingMode.EXPOSURE;
		public float exposure = 1, luminosity = 1, contrast = 1;
	}
	public ToneMappingOptions toneMapping = new ToneMappingOptions();
	
	public boolean bloomEnabled;
	public BloomOptions bloom = new BloomOptions();
	
	public boolean shadows = false;
	public int shadowSize = 2048;
	public float shadowBias = 1f / 255f;

	public boolean skyBoxEnabled = true;
	public float envRotation = 0;
	public float skyboxBlur = 0;
	public Color skyBoxColor = new Color(1,1,1,1);
	public float ambiantStrength = 1;
	public float emissiveIntensity = 1;

	public static class FogOptions {
		public float near = 0;
		public float far = 1;
		public float exponent = 1;
		public Color color = new Color(1,1,1,1);
	}
	public boolean fogEnabled = false;
	public FogOptions fog = new FogOptions();
	
	public static class LightConfig {
		public Color color = new Color(Color.WHITE);
		public Vector3 direction = new Vector3(0,-1,0);
		public float intensity = 3;
		public DirectionalLightEx configure(DirectionalLightEx light){
			return light.set(color, direction, intensity);
		}
		public void set(DirectionalLightEx light) {
			color.set(light.baseColor);
			direction.set(light.direction);
			intensity = light.intensity;
		}
	}
	public LightConfig keyLight = new LightConfig();

	public static class CameraConfig {
		public float fieldOfView = 67, near = 0.01f, far = 200f;
		public Vector3 position = new Vector3();
		public Vector3 target = new Vector3(0,0,-1);
		public Vector3 up = new Vector3(Vector3.Y);
		public PerspectiveCamera configure(PerspectiveCamera camera){
			camera.fieldOfView = fieldOfView;
			camera.near = near;
			camera.far = far;
			camera.position.set(position);
			camera.up.set(up);
			camera.lookAt(target);
			camera.update();
			return camera;
		}
		public CameraConfig set(PerspectiveCamera camera, Vector3 target) {
			fieldOfView = camera.fieldOfView;
			near = camera.near;
			far = camera.far;
			position.set(camera.position);
			up.set(camera.up);
			this.target.set(target);
			return this;
		}
	}
	public CameraConfig camera = new CameraConfig();
	public ObjectMap<String, CameraConfig> views = new ObjectMap<String, Composition.CameraConfig>();

	public static class FXAAOptions {
		public float reduceMin = 1f / 128f, reduceMul = 1f / 8f, spanMax = 8f;
	}
	public FXAAOptions fxaa = new FXAAOptions();
	public boolean fxaaEnabled;
	
}
