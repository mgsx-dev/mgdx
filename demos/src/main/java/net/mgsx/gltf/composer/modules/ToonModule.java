package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene.PBRRenderTargets;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class ToonModule implements GLTFComposerModule
{
	@Override
	public void show(GLTFComposerContext ctx) {
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		ctx.colorShaderConfig.manualGammaCorrection = true;
		ctx.colorShaderConfig.glslVersion = "#version 330\n";
		ctx.colorShaderConfig.fragmentShader = Gdx.files.classpath("shaders/gdx-ceil.fs.glsl").readString();
		ctx.invalidateShaders();

		ctx.fbo.clear();
		ctx.fbo.setDepth(false);
		ctx.fbo.replaceLayer(PBRRenderTargets.COLORS, GLFormat.RGBA8);
		ctx.invalidateFBO();
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		ScreenUtils.clear(ctx.clearColor, true);
		ctx.sceneManager.render();
	}
}
