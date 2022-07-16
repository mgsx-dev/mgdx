package net.mgsx.gltf.composer.core;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.gltf.composer.core.Composition.CameraConfig;

public class CameraTransform {
	private static final Vector3 vec3 = new Vector3();
	
	public static void lerp(PerspectiveCamera camera, CameraConfig src, CameraConfig dst, float alpha){
		camera.position.set(src.position).lerp(dst.position, alpha);
		camera.up.set(src.up).lerp(dst.up, alpha);
		camera.lookAt(vec3.set(src.target).lerp(dst.target, alpha));
		camera.near = MathUtils.lerp(src.near, dst.near, alpha);
		camera.far = MathUtils.lerp(src.far, dst.far, alpha);
		camera.fieldOfView = MathUtils.lerp(src.fieldOfView, dst.fieldOfView, alpha);
		camera.update();
	}
}
