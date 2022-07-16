package net.mgsx.gdx.demos.sketches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gltf.composer.core.CameraTransform;
import net.mgsx.gltf.composer.core.Composition;
import net.mgsx.gltf.composer.core.Composition.CameraConfig;
import net.mgsx.gltf.composer.core.CompositionAssetLoader;
import net.mgsx.gltf.composer.core.CompositionLoader;
import net.mgsx.gltf.composer.core.CompositionManager;
import net.mgsx.gltf.loaders.glb.GLBAssetLoader;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GLTFXCinematicSketch extends ScreenAdapter {

	// compositor is able to play a composition.
	private final CompositionManager compositor = new CompositionManager();
	
	// this is our composition loaded from a file
	private Composition composition;
	
	// demonstrate transition between stored views
	private CameraConfig lastView, currentView;
	private float transitionTime;

	public GLTFXCinematicSketch() {
		String compoPath = "compos/cinematic1.json";
		
		// Asset manager example
		boolean useAssetManagr = false;
		if(useAssetManagr){
			AssetManager assetManager = new AssetManager();
			assetManager.setLoader(Composition.class, new CompositionAssetLoader());
			assetManager.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
			assetManager.setLoader(SceneAsset.class, ".glb", new GLBAssetLoader());
			assetManager.load(compoPath, Composition.class);
			assetManager.finishLoading();
			composition = assetManager.get(compoPath, Composition.class);
		}
		// Direct loading example
		else{
			composition = new CompositionLoader().load(Gdx.files.internal(compoPath));
		}
		
		compositor.setComposition(composition);
		currentView = composition.camera;
	}
	
	@Override
	public void render(float delta) {
		// user switch to view via keyboard
		if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)){
			setPOV("base");
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.UP)){
			setPOV("close");
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
			setPOV("down");
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
			setPOV("fov");
		}
		
		// play transition between views
		float duration = .5f;
		transitionTime += delta / duration;
		if(lastView  != null && currentView != null && transitionTime <= 1){
			float t = Interpolation.pow3Out.apply(transitionTime);
			CameraTransform.lerp(compositor.camera, lastView, currentView, t);
		}
		
		// update and render the composition
		ScreenUtils.clear(Color.CLEAR, true);
		compositor.update(delta);
		compositor.render();
	}

	// starts a transition between last view to a new one
	private void setPOV(String name) {
		CameraConfig view = composition.views.get(name);
		lastView = currentView;
		currentView = view;
		transitionTime = 0;
	}
}
