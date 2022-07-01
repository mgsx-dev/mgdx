package net.mgsx.ibl;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;

import net.mgsx.gdx.graphics.GL31;

public class FlexMesh {

	public static VertexAttribute position4f = new VertexAttribute(Usage.Position, 4, ShaderProgram.POSITION_ATTRIBUTE);
	public static VertexAttribute color4f = new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE);

	static void example()
	{
		// Define vertex layout
		VertexAttributes attributes = new VertexAttributes(FlexMesh.position4f, FlexMesh.color4f);
			
		// create mesh
		FlexMesh mesh = new FlexMesh(attributes);
		mesh.bind();
		mesh.createArrayBuffer(new float[]{
				0,0,0,1, 1,0,0,1,
				1,1,0,1, 1,1,0,1
			});
		mesh.unbind();
		
		// indexed mesh (optional)
		mesh.bind();
		mesh.createIndexBuffer(new int[]{0,1});
	
		// bind shader
		ShaderProgram shader = new ShaderProgram("vertex code", "fragment code");
		shader.bind();
		mesh.bind(shader.getHandle());
		
		// render mesh
		mesh.bind();
		mesh.render(GL20.GL_LINES, 0, 2);
		mesh.unbind();
	}
	
	private int vaoHandle;
	private final IntArray vboHandles = new IntArray();
	private int iboHandle;
	private final VertexAttributes attributes;

	public FlexMesh(VertexAttribute... attributes) {
		this(new VertexAttributes(attributes));
	}
	public FlexMesh(VertexAttributes attributes) {
		this.attributes = attributes;
		IntBuffer buf = BufferUtils.newIntBuffer(1);
		Gdx.gl30.glGenVertexArrays(1, buf);
		vaoHandle = buf.get();
	}
	
	public void bind(){
		Gdx.gl30.glBindVertexArray(vaoHandle);
	}
	public void unbind(){
		Gdx.gl30.glBindVertexArray(0);
	}
	
	public void createArrayBuffer(int sizeBytes, Buffer data, int usage){
		int vboHandle = Gdx.gl20.glGenBuffer();
		vboHandles.add(vboHandle);
		
		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vboHandle);
		Gdx.gl20.glBufferData(GL31.GL_ARRAY_BUFFER, sizeBytes, data, usage);
	}
	public void createArrayBuffer(int numVertics){
		createArrayBuffer(numVertics * attributes.vertexSize, null, GL20.GL_STATIC_DRAW);
	}
	public void createArrayBuffer(float [] data){
		int vboHandle = Gdx.gl20.glGenBuffer();
		vboHandles.add(vboHandle);
		FloatBuffer buffer = BufferUtils.newFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		createArrayBuffer(data.length * 4, buffer, GL30.GL_STATIC_DRAW);
	}
	
	public void createIndexBuffer(int[] indices) {
		ShortBuffer buffer = BufferUtils.newShortBuffer(indices.length * 4);
		for(int i=0 ; i<indices.length ; i++) buffer.put((short)indices[i]);
		createIndexBuffer(buffer);
	}
	public void createIndexBuffer(Buffer buffer) {
		iboHandle = Gdx.gl20.glGenBuffer();
		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, iboHandle);
		Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, buffer.limit(), buffer, GL20.GL_STATIC_DRAW);
	}
	
	/**
	 * Shader program should be bound prior a call
	 * @param glProgram
	 * @param attributes
	 */
	public void bind(int glProgram){
		// TODO cache to the shader !
		for(VertexAttribute attribute : attributes){
			int loc = Gdx.gl.glGetAttribLocation(glProgram, attribute.alias);
			if(loc < 0) throw new GdxRuntimeException("error");
			Gdx.gl.glEnableVertexAttribArray(loc);
			Gdx.gl.glVertexAttribPointer(loc, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize, attribute.offset);
		}
	}
	
	public void render(int glPrimitive, int firstElement, int numElements) {
		Gdx.gl20.glDrawArrays(glPrimitive, firstElement, numElements);
	}
	public int getArrayBuffer(int i) {
		return vboHandles.get(i);
	}
}
