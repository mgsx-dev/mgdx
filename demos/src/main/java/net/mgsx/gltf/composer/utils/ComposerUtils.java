package net.mgsx.gltf.composer.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.mgsx.gdx.graphics.GL32;
import net.mgsx.gdx.graphics.glutils.ColorUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.core.Composition.FogOptions;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;

public class ComposerUtils {
	public static void fitCameraToScene(GLTFComposerContext ctx){
		
		
		if(ctx.scene == null){
			// default camera
			ctx.cameraManager.set(Vector3.Zero, new Vector3(0,0,1), .1f, 100f);
			
			return;
		}

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
		if(ctx.compo.shadows){
			ctx.keyLight = new DirectionalShadowLight(ctx.compo.shadowSize, ctx.compo.shadowSize);
		}else{
			ctx.keyLight = new DirectionalLightEx();
		}
		ctx.keyLight.set(oldLight);
		ctx.sceneManager.environment.add(ctx.keyLight);
		ctx.invalidateShaders();
	}

	public static void updateShadowSize(GLTFComposerContext ctx) {
		if(ctx.keyLight instanceof DirectionalShadowLight){
			((DirectionalShadowLight)ctx.keyLight).setShadowMapSize(ctx.compo.shadowSize, ctx.compo.shadowSize);
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
	
	public static String glTypeString(int glType){
		switch(glType){
		case GL20.GL_BYTE: return "GL_BYTE";
		case GL20.GL_UNSIGNED_BYTE: return "GL_UNSIGNED_BYTE";
		case GL20.GL_SHORT: return "GL_SHORT";
		case GL20.GL_UNSIGNED_SHORT: return "GL_UNSIGNED_SHORT";
		case GL20.GL_INT: return "GL_INT";
		case GL20.GL_UNSIGNED_INT: return "GL_UNSIGNED_INT";
		case GL30.GL_HALF_FLOAT: return "GL_HALF_FLOAT";
		case GL20.GL_FLOAT: return "GL_FLOAT";
		default: return "???";
		}
	}

	public static void updateShadowBias(GLTFComposerContext ctx, float value) {
		ctx.compo.shadowBias = value;
		PBRFloatAttribute a = ctx.sceneManager.environment.get(PBRFloatAttribute.class, PBRFloatAttribute.ShadowBias);
		if(a != null){
			a.value = ctx.compo.shadowBias;
		}else{
			 ctx.sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, ctx.compo.shadowBias));
		}
	}

	public static void enabledSkybox(GLTFComposerContext ctx, boolean enabled) {
		ctx.compo.skyBoxEnabled = enabled;
		if(enabled){
			ctx.sceneManager.setSkyBox(ctx.skyBox);
		}else{
			ctx.sceneManager.setSkyBox(null);
		}
	}

	public static void setAmbientFactor(GLTFComposerContext ctx, float value) {
		Color c = ctx.sceneManager.environment.get(ColorAttribute.class, ColorAttribute.AmbientLight).color;
		c.r = c.g = c.b = value;
		
		ctx.compo.ambiantStrength = value;
		
		// apply also to skybox
		syncSkyboxAmbientFactor(ctx);
	}
	public static void setSkyboxOpacity(GLTFComposerContext ctx, float value) {
		Color c = ctx.sceneManager.environment.get(ColorAttribute.class, ColorAttribute.AmbientLight).color;
		c.a = value;
		
		ctx.compo.skyBoxColor.set(c);
		
		// apply also to skybox
		syncSkyboxAmbientFactor(ctx);
	}
	public static void syncSkyboxAmbientFactor(GLTFComposerContext ctx) {
		if(ctx.skyBox != null){
			ColorAttribute diffuseAttribute = ctx.skyBox.environment.get(ColorAttribute.class, ColorAttribute.Diffuse);
			if(diffuseAttribute != null){
				ColorUtils.hdrSet(
					diffuseAttribute.color,
					ctx.sceneManager.environment.get(ColorAttribute.class, ColorAttribute.AmbientLight).color);
			}
		}
	}

	public static void setEmissiveIntensity(GLTFComposerContext ctx, float value) {
		ctx.compo.emissiveIntensity = value;
		PBRFloatAttribute emissive = ctx.sceneManager.environment.get(PBRFloatAttribute.class, PBRFloatAttribute.EmissiveIntensity);
		if(emissive == null){
			ctx.sceneManager.environment.set(PBRFloatAttribute.createEmissiveIntensity(value));
		}else{
			emissive.value = value;
		}
	}

	public static void enableFog(GLTFComposerContext ctx, boolean enabled) {
		ctx.compo.fogEnabled = enabled;
		if(enabled){
			FogOptions o = ctx.compo.fog;
			float range = ctx.cameraManager.getCamera().far;
			ctx.sceneManager.environment.set(FogAttribute.createFog(o.near * range, o.far * range, o.exponent));
			ctx.sceneManager.environment.set(ColorAttribute.createFog(o.color));
		}else{
			ctx.sceneManager.environment.remove(FogAttribute.FogEquation);
			ctx.sceneManager.environment.remove(ColorAttribute.Fog);
		}
	}

	public static void applyFog(GLTFComposerContext ctx) {
		if(ctx.compo.fogEnabled && ctx.compo.fog != null){
			FogOptions o = ctx.compo.fog;
			FogAttribute e = ctx.sceneManager.environment.get(FogAttribute.class, FogAttribute.FogEquation);
			ColorAttribute c = ctx.sceneManager.environment.get(ColorAttribute.class, ColorAttribute.Fog);
			float range = ctx.cameraManager.getCamera().far;
			if(e != null) e.set(o.near * range, o.far * range, o.exponent);
			if(c != null) c.color.set(o.color);
		}
	}

	public static void attachCamera(GLTFComposerContext ctx, String cameraName) {
		ctx.cameraAttachment = null;
		if(cameraName != null){
			Node node = ctx.scene.modelInstance.getNode(cameraName);
			if(node != null){
				Camera camera = ctx.scene.cameras.get(node);
				if(camera != null){
					ctx.cameraAttachment = node;
				}
			}
		}
	}
}
