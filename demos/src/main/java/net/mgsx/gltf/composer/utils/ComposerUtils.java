package net.mgsx.gltf.composer.utils;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;

public class ComposerUtils {
	public static void fitCameraToScene(GLTFComposerContext ctx){
		
		Vector3 position = ctx.cameraManager.getCamera().position.cpy();
		
		BoundingBox box = ctx.sceneBounds;
		
		Vector3 center = box.getCenter(new Vector3());
		position.set(box.max).sub(center).scl(3).add(center);
		
		float size = Math.max(box.getWidth(), Math.max(box.getHeight(), box.getDepth()));
		float near = size / 1000f;
		float far = size * 30f;
		
		ctx.cameraManager.set(position, center, near, far);
	}

	public static void recreateLight(GLTFComposerContext ctx) {
		ctx.sceneManager.environment.remove(ctx.keyLight);
		DirectionalLightEx oldLight = ctx.keyLight;
		if(ctx.shadows){
			ctx.keyLight = new DirectionalShadowLight(ctx.shadowSize, ctx.shadowSize);
		}else{
			ctx.keyLight = new DirectionalLightEx();
		}
		ctx.keyLight.set(oldLight);
		ctx.sceneManager.environment.add(ctx.keyLight);
		ctx.invalidateShaders();
	}

	public static void updateShadowSize(GLTFComposerContext ctx) {
		if(ctx.keyLight instanceof DirectionalShadowLight){
			((DirectionalShadowLight)ctx.keyLight).setShadowMapSize(ctx.shadowSize, ctx.shadowSize);
		}
	}
}