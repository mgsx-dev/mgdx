package net.mgsx.gdx.math;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class TransformUtils {
	private static final Vector3 
		tangent = new Vector3(),
		up = new Vector3(),
		direction = new Vector3();

	public static Matrix4 setSticky(Matrix4 transform, Camera camera, Vector3 offset){
		return setSticky(transform, camera, offset.x, offset.y, offset.z);
	}

	private static Matrix4 setSticky(Matrix4 transform, Camera camera, float x, float y, float z) {
		// rotation
		direction.set(camera.direction).nor();
		tangent.set(camera.direction).crs(camera.up).nor();
		up.set(tangent).crs(camera.direction).nor();
		// final transform
		return transform
		.set(tangent, up, direction, Vector3.Zero).tra()
		.setTranslation(camera.position)
		.translate(x,y,z);
	}
}
