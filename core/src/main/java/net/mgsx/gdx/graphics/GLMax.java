package net.mgsx.gdx.graphics;

import java.nio.Buffer;

public interface GLMax extends GL32 {

//	void glGetBufferSubData(int target, int offset, Buffer buffer);
	
	void glGetNamedBufferSubData(int buffer, int offset, Buffer data);

	void glGetTexImage(int target, int level, int glFormat, int glType, Buffer pixels);

}
