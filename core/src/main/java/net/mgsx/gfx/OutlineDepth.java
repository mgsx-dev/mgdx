package net.mgsx.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.mgsx.gltfx.ShaderProgramUtils;

public class OutlineDepth
{
	private ShaderProgram shader;
	private boolean distanceFalloffEnabled;
	public float distanceFalloff;
	public float size = 2;
	public float depthMin = .35f;
	public float depthMax = .9f;
	public final Color insideColor = new Color(0,0,0,.5f);
	public final Color outsideColor = new Color(0,0,0,1f);
	
	public OutlineDepth(boolean distanceFalloff) {
		this.distanceFalloffEnabled = distanceFalloff;
		String prefix = "";
		if(distanceFalloff){
			prefix += "#define DISTANCE_FALLOFF\n";
		}
		shader = new ShaderProgram(
				Gdx.files.classpath("shaders/outline-depth.vs.glsl").readString(),
				prefix + 
				Gdx.files.classpath("shaders/outline-depth.fs.glsl").readString());
		ShaderProgramUtils.check(shader);
	}
	
	public void render(SpriteBatch batch, Texture depthTexture, Camera camera){

		shader.bind();
		float size = 1 / this.size;
		
		// float depthMin = ui.outlineDepthMin.getValue() * .001f;
		float depthMin = (float)Math.pow(this.depthMin, 10); // 0.35f
		float depthMax = (float)Math.pow(this.depthMax, 10); // 0.9f
		
		// TODO use an integer instead and divide w and h
		shader.setUniformf("u_size", Gdx.graphics.getWidth() * size, Gdx.graphics.getHeight() * size);
		shader.setUniformf("u_depth_min", depthMin);
		shader.setUniformf("u_depth_max", depthMax);
		shader.setUniformf("u_inner_color", insideColor);
		shader.setUniformf("u_outer_color", outsideColor);
		
		if(distanceFalloffEnabled){
			
			float d = distanceFalloff;
			if(d <= 0){
				d = .001f;
			}
			shader.setUniformf("u_depthRange", camera.far / (camera.near * d));
		}
		
		batch.setShader(shader);
		batch.begin();
		batch.draw(depthTexture, 0, 0, 1, 1, 0f, 0f, 1f, 1f);
		batch.end();
		batch.setShader(null);
	}
}
