package net.mgsx.gdx.demos.sketches;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.BufferUtils;

import net.mgsx.gdx.Mgdx;
import net.mgsx.gdx.graphics.GL32;

public class GL32DebugControlSketch extends ScreenAdapter
{
	public GL32DebugControlSketch() {
		
		// Debug with GL32
		
		Gdx.gl.glEnable(GL32.GL_DEBUG_OUTPUT); checkGL();
		
		Texture texture = new Texture(64, 64, Format.RGBA8888); checkGL();
		// MGdx.gl32.glPushDebugGroup(GL32.GL_DEBUG_SOURCE_APPLICATION, 1, "gp1");
		Mgdx.gl32.glObjectLabel(GL20.GL_TEXTURE, texture.getTextureObjectHandle(), "myTexture"); checkGL();
//				MGdx.gl32.glDebugMessageCallback(new DebugProc() {
//					@Override
//					public void onMessage(int source, int type, int id, int severity, String message) {
//						System.out.println(message);
//					}
//				});
		Mgdx.gl32.glDebugMessageInsert(GL32.GL_DEBUG_SOURCE_APPLICATION, GL32.GL_DEBUG_TYPE_OTHER, GL32.GL_DEBUG_SEVERITY_NOTIFICATION, 123, -1, "mgdx");
		checkGL();
		
		IntBuffer buf = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL32.GL_DEBUG_LOGGED_MESSAGES, buf); checkGL();
		int n = buf.get();
		
		IntBuffer sources = BufferUtils.newIntBuffer(n);
		IntBuffer types = BufferUtils.newIntBuffer(n);
		IntBuffer ids = BufferUtils.newIntBuffer(n);
		IntBuffer severities = BufferUtils.newIntBuffer(n);
		IntBuffer lengths = BufferUtils.newIntBuffer(n);
		
		Gdx.gl.glGetIntegerv(GL32.GL_MAX_DEBUG_MESSAGE_LENGTH, buf); checkGL();
		ByteBuffer messageLog = BufferUtils.newByteBuffer(buf.get());
		
		Mgdx.gl32.glGetDebugMessageLog(n, sources, types, ids, severities, lengths, messageLog);
		
		 checkGL();
		 
		while(sources.hasRemaining()){
			int source = sources.get();
			int type = types.get();
			int id = ids.get();
			int severity = severities.get();
			int length = lengths.get();
			byte[] bytes = new byte[messageLog.remaining()];
			messageLog.get(bytes);
		    String newContent = new String(bytes, Charset.forName("UTF8"));
			System.out.println(source + " " + type + " " + id + " " + severity + " " + length + " " + newContent);
		}
	}
	
	private void checkGL() {
		int err = Gdx.gl.glGetError();
		if(err != GL20.GL_NO_ERROR){
			System.out.println("error " + err);
		}
	}
}
