package net.mgsx.gltf.composer.utils;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.mgsx.gdx.graphics.GL32;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
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

	public static String primitiveString(int primitiveType) {
		switch(primitiveType){
		case GL20.GL_POINTS: return "GL_POINTS";
		case GL20.GL_LINES: return "GL_LINES";
		case GL20.GL_LINE_STRIP: return "GL_LINE_STRIP";
		case GL20.GL_LINE_LOOP: return "GL_LINE_LOOP";
		case GL20.GL_TRIANGLES: return "GL_TRIANGLES";
		case GL20.GL_TRIANGLE_STRIP: return "GL_TRIANGLE_STRIP";
		case GL20.GL_TRIANGLE_FAN: return "GL_TRIANGLE_FAN";
		case GL32.GL_PATCHES: return "GL_PATCHES";
		case GL32.GL_QUADS: return "GL_QUADS";
		default: return "UNKNOWN";
		}
	}

	public static void updateShadowBias(GLTFComposerContext ctx, float value) {
		ctx.shadowBias = value;
		PBRFloatAttribute a = ctx.sceneManager.environment.get(PBRFloatAttribute.class, PBRFloatAttribute.ShadowBias);
		if(a != null){
			a.value = ctx.shadowBias;
		}else{
			 ctx.sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, ctx.shadowBias));
		}
	}

	public static void enabledSkybox(GLTFComposerContext ctx, boolean enabled) {
		ctx.skyBoxEnabled = enabled;
		if(enabled){
			ctx.sceneManager.setSkyBox(ctx.skyBox);
		}else{
			ctx.sceneManager.setSkyBox(null);
		}
	}
}
