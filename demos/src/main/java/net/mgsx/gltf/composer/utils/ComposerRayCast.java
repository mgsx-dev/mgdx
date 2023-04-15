package net.mgsx.gltf.composer.utils;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import net.mgsx.gdx.graphics.cameras.BlenderCamera.CameraRayCast;
import net.mgsx.gdx.graphics.glutils.MeshUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.scene3d.scene.Scene;

public class ComposerRayCast implements CameraRayCast {

	private final GLTFComposerContext ctx;
	private float [] triangles;
	
	
	public ComposerRayCast(GLTFComposerContext ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public boolean rayCast(Ray ray, Vector3 position) {
		if(ctx.scene != null){
			return rayCast(ray, position, ctx.scene);
		}
		return false;
	}

	private boolean rayCast(Ray ray, Vector3 position, Scene scene) {
		// TODO cache ?
//		if(triangles == null){
//		}
		scene.modelInstance.calculateTransforms();
		triangles = MeshUtils.extractTriangles(scene.modelInstance);
		return Intersector.intersectRayTriangles(ray, triangles, position);
	}

}
