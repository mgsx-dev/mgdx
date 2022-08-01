package net.mgsx.gdx.demos.sketches;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.GL31;

public class GL31ProgramIntrospection extends ScreenAdapter
{
	public GL31ProgramIntrospection() {
		ShaderProgram shader = SpriteBatch.createDefaultShader();
		int program = shader.getHandle();
		
		IntBuffer int1 = BufferUtils.newIntBuffer(1);
		
		Mgdx.gl31.glGetProgramInterfaceiv(program, GL31.GL_UNIFORM, GL31.GL_ACTIVE_RESOURCES, int1);
		int nbRes = int1.get();
		
		for(int i=0 ; i<nbRes ; i++){
			String name = Mgdx.gl31.glGetProgramResourceName(program, GL31.GL_UNIFORM, i);
			Gdx.app.log("Gdx", name);
		}
		
		int u_texture_index = Mgdx.gl31.glGetProgramResourceIndex(program, GL31.GL_UNIFORM, "u_texture");
		Gdx.app.log("Gdx", "u_texture: index:" + u_texture_index);
		
		int resLoc = Mgdx.gl31.glGetProgramResourceLocation(program, GL31.GL_UNIFORM, "u_texture");
		Gdx.app.log("Gdx", "u_texture: loc:" + resLoc);
		
		// GL_TYPE
		IntBuffer props = BufferUtils.newIntBuffer(16);
		props.put(GL31.GL_TYPE);
		props.put(GL31.GL_LOCATION);
		props.put(GL31.GL_REFERENCED_BY_VERTEX_SHADER);
		props.put(GL31.GL_REFERENCED_BY_FRAGMENT_SHADER);
		props.flip();
		
		int1.clear();
		
		IntBuffer results = BufferUtils.newIntBuffer(16);
		
		Mgdx.gl31.glGetProgramResourceiv(program, GL31.GL_UNIFORM, u_texture_index, props, int1, results);
		int count = int1.get();
		for(int i=0 ; i<count ; i++){
			int type = results.get();
			int loc = results.get();
			boolean vs = results.get() == GL20.GL_TRUE;
			boolean fs = results.get() == GL20.GL_TRUE;
			boolean sampler2D = type == GL20.GL_SAMPLER_2D;
			Gdx.app.log("Gdx", "u_texture: sampler2D:" + sampler2D + " " + loc + " vertex:" + vs + " fragment:" + fs);
		}
		
		
		shader.dispose();
	}
}
