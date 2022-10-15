package net.mgsx.gltfx.ktx2;

import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.Array;

public class KTX2Data {
	public enum MipMapMode {
		NONE, RAW, RUNTIME, GENERATE
	}
	
	public enum CompressionMode {
		None, ZLIB
	}
	
	public enum TextureCompression {
		None, ETC2
	}
	
	public static class ImageFace {
		public byte [] data;
		public ByteBuffer buffer;
	}
	public static class ImageLayer {
		public final Array<ImageFace> faces = new Array<ImageFace>();
	}

	public static class ImageLevel {
		public final Array<ImageLayer> layers = new Array<ImageLayer>();
		long offset;
		long size;
		long uncompressedSize;
		byte [] compressedData;
	}
}
