/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.glutils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.GL31;

/**
 * <p>
 * A {@link VertexData} implementation based on OpenGL vertex buffer objects.
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate a new OpenGL vertex buffer object.
 * <p>
 * The data is bound via glVertexAttribPointer() according to the attribute aliases specified via {@link VertexAttributes} in the
 * constructor.
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 *
 * @author mzechner, Dave Clayton <contact@redskyforge.com> */
public class SSBO implements Disposable {
	private FloatBuffer floatBuffer;
	private ByteBuffer byteBuffer;
	private boolean ownsBuffer;
	private int bufferHandle;
	private int usage;

	/** Constructs a new interleaved VertexBufferObject.
	 *
	 * @param isStatic whether the vertex data is static.
	 * @param numVertices the maximum number of vertices
	 * @param attributes the {@link VertexAttribute}s. */
	public SSBO (boolean isStatic, int numBytes) {
		bufferHandle = Gdx.gl20.glGenBuffer();

		ByteBuffer data = BufferUtils.newUnsafeByteBuffer(numBytes);
		((Buffer)data).limit(0);
		setBuffer(data, true);
		usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
	}

	protected SSBO (int usage, ByteBuffer data, boolean ownsBuffer, int vertexSize) {
		bufferHandle = Gdx.gl20.glGenBuffer();

		setBuffer(data, ownsBuffer);
		this.usage = usage;
	}

	/** Low level method to reset the buffer and attributes to the specified values. Use with care!
	 * @param data
	 * @param ownsBuffer
	 * @param value */
	protected void setBuffer (Buffer data, boolean ownsBuffer) {
		if (this.ownsBuffer && byteBuffer != null) BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
		if (data instanceof ByteBuffer)
			byteBuffer = (ByteBuffer)data;
		else
			throw new GdxRuntimeException("Only ByteBuffer is currently supported");
		this.ownsBuffer = ownsBuffer;

		final int l = byteBuffer.limit();
		((Buffer)byteBuffer).limit(byteBuffer.capacity());
		floatBuffer = byteBuffer.asFloatBuffer();
		((Buffer)byteBuffer).limit(l);
		((Buffer)floatBuffer).limit(l / 4);
	}

	private void bufferChanged () {
		Gdx.gl.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, bufferHandle);
		Gdx.gl.glBufferData(GL31.GL_SHADER_STORAGE_BUFFER, byteBuffer.limit(), byteBuffer, usage);
	}

	public void setData (float[] vertices, int offset, int count) {
		BufferUtils.copy(vertices, byteBuffer, count, offset);
		((Buffer)floatBuffer).position(0);
		((Buffer)floatBuffer).limit(count);
		bufferChanged();
	}
	public void setData (float[] vertices) {
		setData(vertices, 0, vertices.length);
	}

	public void updateData (int targetOffset, float[] vertices, int sourceOffset, int count) {
		final int pos = byteBuffer.position();
		((Buffer)byteBuffer).position(targetOffset * 4);
		BufferUtils.copy(vertices, sourceOffset, count, byteBuffer);
		((Buffer)byteBuffer).position(pos);
		((Buffer)floatBuffer).position(0);
		bufferChanged();
	}

	/** Invalidates the VertexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss. */
	public void invalidate () {
		bufferHandle = Gdx.gl20.glGenBuffer();
	}

	/** Disposes of all resources this VertexBufferObject uses. */
	@Override
	public void dispose () {
		GL20 gl = Gdx.gl20;
		gl.glBindBuffer(GL31.GL_SHADER_STORAGE_BUFFER, 0);
		gl.glDeleteBuffer(bufferHandle);
		bufferHandle = 0;
		if (ownsBuffer) BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
	}

	public int getHandle() {
		return bufferHandle;
	}
}
