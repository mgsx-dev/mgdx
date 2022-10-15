package net.mgsx.gltf.composer.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gdx.utils.FrameBufferUtils;
import net.mgsx.gltf.composer.GLTFComposerContext;
import net.mgsx.gltf.composer.GLTFComposerModule;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltfx.GLFormat;
import net.mgsx.gltfx.mrt.PBRRenderTargets;

public class GouraudModule implements GLTFComposerModule
{
	@Override
	public void show(GLTFComposerContext ctx) {
		ctx.colorShaderConfig.manualSRGB = SRGB.FAST;
		ctx.colorShaderConfig.manualGammaCorrection = true;
		
		ctx.colorShaderConfig.vertexShader = Gdx.files.classpath("net/mgsx/gltf/shaders/default.vs.glsl").readString();
		ctx.colorShaderConfig.fragmentShader = Gdx.files.classpath("net/mgsx/gltf/shaders/default.fs.glsl").readString();
		ctx.invalidateShaders();

		ctx.fbo.clear();
		ctx.fbo.setDepth(false);
		ctx.fbo.replaceLayer(PBRRenderTargets.COLORS, GLFormat.RGBA8);
		ctx.invalidateFBO();
	}
	
	@Override
	public void render(GLTFComposerContext ctx) {
		ctx.sceneManager.renderShadows();
		
		ctx.fbo.ensureScreenSize();
		ctx.fbo.begin();
		ScreenUtils.clear(ctx.compo.clearColor, true);
		ctx.sceneManager.renderColors();
		ctx.fbo.end();
		
		ctx.batch.disableBlending();
		FrameBufferUtils.blit(ctx.batch, ctx.fbo.getColorBufferTexture(), ctx.ldrFbo);
		ctx.batch.enableBlending();
	}
}
