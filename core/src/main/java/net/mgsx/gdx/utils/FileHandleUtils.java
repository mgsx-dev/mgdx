package net.mgsx.gdx.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

public class FileHandleUtils {

	public static ByteBuffer asByteBuffer(FileHandle file, boolean gzip, int size) {
		return asByteBuffer(file.read(), gzip, size);
	}

	public static ByteBuffer asByteBuffer(InputStream stream, boolean gzip, int size) {
		try {
			GZIPInputStream zip = new GZIPInputStream(stream);
			ByteBuffer buffer = BufferUtils.newByteBuffer(size);
			StreamUtils.copyStream(zip, buffer);
			return buffer;
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

}
