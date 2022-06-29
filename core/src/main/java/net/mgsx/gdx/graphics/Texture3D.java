package net.mgsx.gdx.graphics;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.glutils.GLOnlyTexture3DData;

/**
 * Open GLES wrapper for Texture3D
 * @author MGSX */
public class Texture3D extends GLTexture {

	final static Map<Application, Array<Texture3D>> managedTexture3Ds = new HashMap<Application, Array<Texture3D>>();

	private Texture3DData data;

	protected TextureWrap rWrap = TextureWrap.ClampToEdge;

	public Texture3D (int width, int height, int depth, int glFormat, int glInternalFormat, int glType, boolean useMipMaps){
		this(new GLOnlyTexture3DData(width, height, depth, glFormat, glInternalFormat, glType, useMipMaps));
	}

	public Texture3D (Texture3DData data) {
		super(GL30.GL_TEXTURE_3D, Gdx.gl.glGenTexture());

		if (Gdx.gl30 == null) {
			throw new GdxRuntimeException("TextureArray requires a device running with GLES 3.0 compatibilty");
		}

		load(data);

		if (data.isManaged()) addManagedTexture(Gdx.app, this);
	}

	private void load (Texture3DData data) {
		if (this.data != null && data.isManaged() != this.data.isManaged())
			throw new GdxRuntimeException("New data must have the same managed status as the old data");
		this.data = data;

		bind();

		if (!data.isPrepared()) data.prepare();

		data.consume3DData();

		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap, rWrap);

		Gdx.gl.glBindTexture(glTarget, 0);
	}
	
	public void download(){
		bind();
		data.downloadData();
	}
	
	public void upload() {
		bind();
		data.consume3DData();
	}

	@Override
	public int getWidth () {
		return data.getWidth();
	}

	@Override
	public int getHeight () {
		return data.getHeight();
	}

	@Override
	public int getDepth () {
		return data.getDepth();
	}

	@Override
	public boolean isManaged () {
		return data.isManaged();
	}

	@Override
	protected void reload () {
		if (!isManaged()) throw new GdxRuntimeException("Tried to reload an unmanaged TextureArray");
		glHandle = Gdx.gl.glGenTexture();
		load(data);
	}

	private static void addManagedTexture (Application app, Texture3D texture) {
		Array<Texture3D> managedTextureArray = managedTexture3Ds.get(app);
		if (managedTextureArray == null) managedTextureArray = new Array<Texture3D>();
		managedTextureArray.add(texture);
		managedTexture3Ds.put(app, managedTextureArray);
	}


	/** Clears all managed TextureArrays. This is an internal method. Do not use it! */
	public static void clearAllTextureArrays (Application app) {
		managedTexture3Ds.remove(app);
	}

	/** Invalidate all managed TextureArrays. This is an internal method. Do not use it! */
	public static void invalidateAllTextureArrays (Application app) {
		Array<Texture3D> managedTextureArray = managedTexture3Ds.get(app);
		if (managedTextureArray == null) return;

		for (int i = 0; i < managedTextureArray.size; i++) {
			Texture3D textureArray = managedTextureArray.get(i);
			textureArray.reload();
		}
	}

	public static String getManagedStatus () {
		StringBuilder builder = new StringBuilder();
		builder.append("Managed TextureArrays/app: { ");
		for (Application app : managedTexture3Ds.keySet()) {
			builder.append(managedTexture3Ds.get(app).size);
			builder.append(" ");
		}
		builder.append("}");
		return builder.toString();
	}

	/** @return the number of managed TextureArrays currently loaded */
	public static int getNumManagedTextureArrays () {
		return managedTexture3Ds.get(Gdx.app).size;
	}

	public void setWrap(TextureWrap u, TextureWrap v, TextureWrap r) {
		this.rWrap = r;
		super.setWrap(u, v);
		Gdx.gl.glTexParameteri(glTarget, GL30.GL_TEXTURE_WRAP_R, r.getGLEnum());
	}
	public void unsafeSetWrap (TextureWrap u, TextureWrap v, TextureWrap r, boolean force) {
		unsafeSetWrap(u, v, force);
		if (r != null && (force || rWrap != r)) {
			Gdx.gl.glTexParameteri(glTarget, GL30.GL_TEXTURE_WRAP_R, u.getGLEnum());
			rWrap = r;
		}
	}
	public void unsafeSetWrap (TextureWrap u, TextureWrap v, TextureWrap r) {
		unsafeSetWrap(u, v, r, false);
	}

}
