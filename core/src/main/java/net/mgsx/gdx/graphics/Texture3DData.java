package net.mgsx.gdx.graphics;

public interface Texture3DData  {

	/** @return whether the TextureData is prepared or not. */
	public boolean isPrepared ();

	/** Prepares the TextureData for a call to {@link #consume3DData()}. This method can be called from a non OpenGL thread and
	 * should thus not interact with OpenGL. */
	public void prepare ();

	/** @return the width of the pixel data */
	public int getWidth ();

	/** @return the height of the pixel data */
	public int getHeight ();

	/** @return the depth of this Texture3D */
	public int getDepth();

	/** @return the internal format of this Texture3D */
	public int getInternalFormat();

	/** @return the GL type of this Texture3D*/
	public int getGLType();
	
	/** @return whether to generate mipmaps or not. */
	public boolean useMipMaps ();
	
	/** Uploads the pixel data to the OpenGL ES texture. The caller must bind an OpenGL ES texture. A
	 * call to {@link #prepare()} must preceed a call to this method. Any internal data structures created in {@link #prepare()}
	 * should be disposed of here. */
	public void consume3DData();

	/** @return whether this implementation can cope with a EGL context loss. */
	public boolean isManaged ();
}
