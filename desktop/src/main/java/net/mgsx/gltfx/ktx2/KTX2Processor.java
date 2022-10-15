package net.mgsx.gltfx.ktx2;

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.LongArray;

import net.mgsx.gltfx.ktx2.ByteBufferOutputStream;
import net.mgsx.gltfx.ktx2.KHRDataFormat;
import net.mgsx.gltfx.ktx2.KTX2Format;
import net.mgsx.gltfx.ktx2.KTX2Data.CompressionMode;
import net.mgsx.gltfx.ktx2.KTX2Data.ImageFace;
import net.mgsx.gltfx.ktx2.KTX2Data.ImageLayer;
import net.mgsx.gltfx.ktx2.KTX2Data.ImageLevel;
import net.mgsx.gltfx.ktx2.KTX2Data.MipMapMode;
import net.mgsx.gltfx.ktx2.KTX2Data.TextureCompression;

/**
 * https://www.khronos.org/registry/KTX/specs/2.0/ktxspec_v2.html 
 *
 */
public class KTX2Processor {

	// returns all higher 16 bits as 0 for all results
	public static int fromFloat( float fval )
	{
	    int fbits = Float.floatToIntBits( fval );
	    int sign = fbits >>> 16 & 0x8000;          // sign only
	    int val = ( fbits & 0x7fffffff ) + 0x1000; // rounded value

	    if( val >= 0x47800000 )               // might be or become NaN/Inf
	    {                                     // avoid Inf due to rounding
	        if( ( fbits & 0x7fffffff ) >= 0x47800000 )
	        {                                 // is or must become NaN/Inf
	            if( val < 0x7f800000 )        // was value but too large
	                return sign | 0x7c00;     // make it +/-Inf
	            return sign | 0x7c00 |        // remains +/-Inf or NaN
	                ( fbits & 0x007fffff ) >>> 13; // keep NaN (and Inf) bits
	        }
	        return sign | 0x7bff;             // unrounded not quite Inf
	    }
	    if( val >= 0x38800000 )               // remains normalized value
	        return sign | val - 0x38000000 >>> 13; // exp - 127 + 15
	    if( val < 0x33000000 )                // too small for subnormal
	        return sign;                      // becomes +/-0
	    val = ( fbits & 0x7fffffff ) >>> 23;  // tmp exp for subnormal calc
	    return sign | ( ( fbits & 0x7fffff | 0x800000 ) // add subnormal bit
	         + ( 0x800000 >>> val - 102 )     // round depending on cut off
	      >>> 126 - val );   // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
	}
	
	private static void configureCompression(KTX2Processor p, CompressionMode compressionMode){
		switch(compressionMode){
		case None:
			p.supercompressionScheme = KTX2Format.SUPERCOMPRESSION_NONE;
			break;
		case ZLIB:
			p.supercompressionScheme = KTX2Format.SUPERCOMPRESSION_ZLIB;
			break;
		default:
			throw new GdxRuntimeException("not supported " + compressionMode);
		}
	}
	
	private static void configureFormat(KTX2Processor p, Format format){
		boolean useETC2 = p.textureCompression == TextureCompression.ETC2;
		// TODO allow choosing A1 or A8 for alpha, and also SRGB/RGB
		switch(format){
		case RGBA8888:
			p.vkFormat = useETC2 ? KTX2Format.VK_FORMAT_ETC2_R8G8B8A8_UNORM_BLOCK : KTX2Format.VK_FORMAT_R8G8B8A8_UNORM;
			// GL30.GL_COMPRESSED_RGBA8_ETC2_EAC;
			p.typeSize = 1;
			p.stride = 4;
			break;
		case RGB888:
			p.vkFormat = useETC2 ? KTX2Format.VK_FORMAT_ETC2_R8G8B8_UNORM_BLOCK : KTX2Format.VK_FORMAT_R8G8B8_UNORM;
			// GL30.GL_COMPRESSED_RGB8_ETC2;
			p.typeSize = 1;
			p.stride = 3;
			break;
		case Alpha:
		case Intensity:
			p.vkFormat = KTX2Format.VK_FORMAT_R8_UNORM;
			p.typeSize = 1;
			p.stride = 1;
			break;
		case LuminanceAlpha:
			p.vkFormat = KTX2Format.VK_FORMAT_R8G8_UNORM;
			p.typeSize = 1;
			p.stride = 2;
			break;
		case RGBA4444:
			p.vkFormat = KTX2Format.VK_FORMAT_R4G4B4A4_UNORM_PACK16;
			p.typeSize = 2;
			p.stride = 2;
			break;
		case RGB565:
			p.vkFormat = KTX2Format.VK_FORMAT_R5G6B5_UNORM_PACK16;
			p.typeSize = 2;
			p.stride = 2;
		}
	}
	
	public static void exportCubemap(FileHandle outFile, MipMapMode mipmapMode, CompressionMode compressionMode, Array<Pixmap> pixmaps) {
		KTX2Processor p = new KTX2Processor();
		Pixmap base = pixmaps.first();
		configureFormat(p, base.getFormat());
		
		ImageLevel level = new ImageLevel();
		ImageLayer layer = new ImageLayer();

		int faceBytes = base.getWidth() * base.getHeight() * p.stride;
		
		for(int i=0 ; i<6 ; i++){
			Pixmap map = pixmaps.get(i);
			ImageFace face = new ImageFace();
			layer.faces.add(face);
			face.data = new byte[faceBytes];
			map.getPixels().get(face.data);
		}
		
		level.layers.add(layer);
		p.levels.add(level);
		
		p.width = base.getWidth();
		p.height = base.getHeight();
		
		p.mipmapAtRuntime = mipmapMode == MipMapMode.RUNTIME;
		configureCompression(p, compressionMode);
		try {
			p.save(outFile);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	// There is no way to query internal format and type via OpenGL or via libgdx.
	// So it has to be provided by caller.
	public static void exportCubemap(FileHandle outFile, FrameBufferCubemap fbo, int glInternalFormat, MipMapMode mipmapMode) {
		
		KTX2Processor p = new KTX2Processor();
		
		int glFormat, glType;
		if(glInternalFormat == GL30.GL_RGB16F){
			glFormat = GL30.GL_RGB;
			glType = GL30.GL_HALF_FLOAT;
			p.vkFormat = KTX2Format.VK_FORMAT_R16G16B16_SFLOAT;
			p.typeSize = 2;
		}else if(glInternalFormat == GL30.GL_RGBA16F){
			glFormat = GL30.GL_RGBA;
			glType = GL30.GL_HALF_FLOAT;
			p.vkFormat = KTX2Format.VK_FORMAT_R16G16B16A16_SFLOAT;
			p.typeSize = 2;
		}else if(glInternalFormat == GL30.GL_RGB8 || glInternalFormat == GL20.GL_RGB){
			glFormat = GL20.GL_RGB;
			glType = GL20.GL_UNSIGNED_BYTE;
			p.vkFormat = KTX2Format.VK_FORMAT_R8G8B8_UNORM;
			p.typeSize = 1;
		}else if(glInternalFormat == GL30.GL_RGBA8 || glInternalFormat == GL20.GL_RGBA){
			glFormat = GL20.GL_RGBA;
			glType = GL20.GL_UNSIGNED_BYTE;
			p.vkFormat = KTX2Format.VK_FORMAT_R8G8B8A8_UNORM;
			p.typeSize = 1;
		}else{
			throw new GdxRuntimeException("format not supported: " + glInternalFormat);
		}
		
		ImageLevel level = new ImageLevel();
		ImageLayer layer = new ImageLayer();

		int faceBytes = fbo.getWidth() * fbo.getHeight() * 3 * 2;
		ByteBuffer pixels = BufferUtils.newByteBuffer(faceBytes);
		fbo.begin();
		while(fbo.nextSide()){
			pixels.position(0);
			Gdx.gl30.glReadPixels(0, 0, fbo.getWidth(), fbo.getHeight(), glFormat, glType, pixels);
			ImageFace face = new ImageFace();
			layer.faces.add(face);
			face.data = new byte[faceBytes];
			pixels.get(face.data);
		}
		fbo.end();
		
		level.layers.add(layer);
		p.levels.add(level);
		
		p.width = fbo.getWidth();
		p.height = fbo.getHeight();
		
		p.mipmapAtRuntime = mipmapMode == MipMapMode.RUNTIME;
		
		try {
			p.save(outFile);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	public static void exportCubemap(FileHandle outFile, Array<ByteBuffer> buffers, int width, int height, int mipmapCount, int layers, int glInternalFormat, MipMapMode mipmapMode, boolean compress) {
		exportCubemap(outFile.write(false), buffers, width, height, mipmapCount, layers, glInternalFormat, mipmapMode, compress);
	}
	public static void exportCubemap(OutputStream output, Array<ByteBuffer> buffers, int width, int height, int mipmapCount, int layers, int glInternalFormat, MipMapMode mipmapMode, boolean compress) {
		
		KTX2Processor p = new KTX2Processor();
		
		if(compress)
			p.supercompressionScheme = KTX2Format.SUPERCOMPRESSION_ZLIB;
		
		if(glInternalFormat == GL30.GL_RGB32F){
			p.vkFormat = KTX2Format.VK_FORMAT_R32G32B32_SFLOAT;
			p.typeSize = 4; // TODO not sure...
		}else if(glInternalFormat == GL30.GL_RGBA32F){
			p.vkFormat = KTX2Format.VK_FORMAT_R32G32B32A32_SFLOAT;
			p.typeSize = 4; // TODO not sure...
		}else if(glInternalFormat == GL30.GL_RGB16F){
			p.vkFormat = KTX2Format.VK_FORMAT_R16G16B16_SFLOAT;
			p.typeSize = 2;
		}else if(glInternalFormat == GL30.GL_RGBA16F){
			p.vkFormat = KTX2Format.VK_FORMAT_R16G16B16A16_SFLOAT;
			p.typeSize = 2;
		}else if(glInternalFormat == GL30.GL_RGB8 || glInternalFormat == GL20.GL_RGB){
			p.vkFormat = KTX2Format.VK_FORMAT_R8G8B8_UNORM;
			p.typeSize = 1;
		}else if(glInternalFormat == GL30.GL_RGBA8 || glInternalFormat == GL20.GL_RGBA){
			p.vkFormat = KTX2Format.VK_FORMAT_R8G8B8A8_UNORM;
			p.typeSize = 1;
		}else{
			throw new GdxRuntimeException("format not supported: " + glInternalFormat);
		}
		
		for(int l=0, i=0; l<mipmapCount && i<buffers.size ; l++){
			ImageLevel level = new ImageLevel();
			p.levels.add(level);
			for(int layerIndex=0 ; layerIndex<layers ; layerIndex++){
				ImageLayer layer = new ImageLayer();
				level.layers.add(layer);
				for(int f=0 ; f<6 ; f++, i++){
					ImageFace face = new ImageFace();
					layer.faces.add(face);
					ByteBuffer buffer = buffers.get(i);
					face.data = new byte[buffer.remaining()];
					buffer.get(face.data);
				}
			}
		}
		
		p.width = width;
		p.height = height;
		
		p.mipmapAtRuntime = mipmapMode == MipMapMode.RUNTIME;
		
		try {
			p.save(output);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
	public static void export(OutputStream output, ByteBuffer buffer, int width, int height, int glInternalFormat, MipMapMode mipmapMode, boolean compress) {
		Array<ByteBuffer> buffers = new Array<ByteBuffer>(1);
		buffers.add(buffer);
		export(output, buffers, width, height, 1, 1, glInternalFormat, mipmapMode, compress);
	}
	
	public static void export(OutputStream output, Array<ByteBuffer> buffers, int width, int height, int mipmapCount, int layers, int glInternalFormat, MipMapMode mipmapMode, boolean compress) {
		// TODO refactor with nFaces
		KTX2Processor p = new KTX2Processor();
		
		if(compress)
			p.supercompressionScheme = KTX2Format.SUPERCOMPRESSION_ZLIB;
		
		if(glInternalFormat == GL30.GL_RGB32F){
			p.vkFormat = KTX2Format.VK_FORMAT_R32G32B32_SFLOAT;
			p.typeSize = 4; // TODO not sure...
		}else if(glInternalFormat == GL30.GL_RGBA32F){
			p.vkFormat = KTX2Format.VK_FORMAT_R32G32B32A32_SFLOAT;
			p.typeSize = 4; // TODO not sure...
		}else if(glInternalFormat == GL30.GL_RGB16F){
			p.vkFormat = KTX2Format.VK_FORMAT_R16G16B16_SFLOAT;
			p.typeSize = 2;
		}else if(glInternalFormat == GL30.GL_RGBA16F){
			p.vkFormat = KTX2Format.VK_FORMAT_R16G16B16A16_SFLOAT;
			p.typeSize = 2;
		}else if(glInternalFormat == GL30.GL_RGB8 || glInternalFormat == GL20.GL_RGB){
			p.vkFormat = KTX2Format.VK_FORMAT_R8G8B8_UNORM;
			p.typeSize = 1;
		}else if(glInternalFormat == GL30.GL_RGBA8 || glInternalFormat == GL20.GL_RGBA){
			p.vkFormat = KTX2Format.VK_FORMAT_R8G8B8A8_UNORM;
			p.typeSize = 1;
		}else{
			throw new GdxRuntimeException("format not supported: " + glInternalFormat);
		}
		
		for(int l=0, i=0; l<mipmapCount && i<buffers.size ; l++){
			ImageLevel level = new ImageLevel();
			p.levels.add(level);
			for(int layerIndex=0 ; layerIndex<layers ; layerIndex++){
				ImageLayer layer = new ImageLayer();
				level.layers.add(layer);
				// single face
				ImageFace face = new ImageFace();
				layer.faces.add(face);
				ByteBuffer buffer = buffers.get(i);
				face.data = new byte[buffer.remaining()];
				buffer.get(face.data);
			}
		}
		
		p.width = width;
		p.height = height;
		
		p.mipmapAtRuntime = mipmapMode == MipMapMode.RUNTIME;
		
		try {
			p.save(output);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static void export(FileHandle outFile, Pixmap pixmap, MipMapMode mipmapMode, CompressionMode compressionMode, TextureCompression textureCompression) {
		KTX2Processor p = new KTX2Processor();
		configureCompression(p, compressionMode);
		
		ImageLevel level = new ImageLevel();
		ImageLayer layer = new ImageLayer();
		ImageFace face = new ImageFace();
		
		if(textureCompression == TextureCompression.ETC2){
			face.data = compressETC2(pixmap);
		}else{
			face.data = new byte[pixmap.getPixels().limit()];
			pixmap.getPixels().get(face.data);
		}
		
		layer.faces.add(face);
		level.layers.add(layer);
		p.levels.add(level);
		
		p.width = pixmap.getWidth();
		p.height = pixmap.getHeight();
		
		p.mipmapAtRuntime = mipmapMode == MipMapMode.RUNTIME;

		p.textureCompression = textureCompression;
		
		configureFormat(p, pixmap.getFormat());
		
		try {
			p.save(outFile);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	public int width, height, depth;
	
	private boolean mipmapAtRuntime;
	
	private int supercompressionScheme;
	
	private final KHRDataFormat format = new KHRDataFormat();
	
	public int vkFormat = KTX2Format.VK_FORMAT_UNDEFINED;
	public int typeSize = KTX2Format.VK_FORMAT_UNDEFINED_TYPE_SIZE;
	
	int stride;
	
	private TextureCompression textureCompression = TextureCompression.None;
	
	public final Array<ImageLevel> levels = new Array<ImageLevel>();
	
	public void save(FileHandle file) throws IOException
	{
		save(file.write(false));
	}
	public void save(OutputStream output) throws IOException {
//		DataOutput out = new LittleEndianOutputStream(output);
		DataOutput out = new ByteBufferOutputStream(output);
		
		compress();
		
//		Byte[12] identifier
		out.write(KTX2Format.FileIdentifier);
//		UInt32 vkFormat
		out.writeInt(vkFormat);
//		UInt32 typeSize
		out.writeInt(typeSize);
//		UInt32 pixelWidth
		out.writeInt(width);
//		UInt32 pixelHeight
		out.writeInt(height);
//		UInt32 pixelDepth
		out.writeInt(depth);
//		UInt32 layerCount
		int layoutCount = levels.first().layers.size;
		out.writeInt(layoutCount > 1 ? layoutCount : 0);
//		UInt32 faceCount
		out.writeInt(levels.first().layers.first().faces.size); // 6 for cubemaps, 1 for any other
//		UInt32 levelCount
		int levelCount = levels.size;
		// 1 for non mipmap, 0 for runtime generation, doesn't need all levels 
		out.writeInt(levelCount > 1 ? levelCount : (mipmapAtRuntime ? 0 : 1));
//		UInt32 supercompressionScheme
		out.writeInt(supercompressionScheme);
		
		
		
		int fileOffset = 12 + 4 * 9;
		
		// Index 
		int [] formatEncoded = format.encode();
		int dfdByteOffset = fileOffset + 4 * 4 + 8 * 2 + levels.size * 8 * 3;
		int dfdTotalSize = formatEncoded.length * 4 + 4; // including dfdTotalSize field.
		
//		UInt32 dfdByteOffset
		out.writeInt(dfdByteOffset);
//		UInt32 dfdByteLength
		out.writeInt(dfdTotalSize);
//		UInt32 kvdByteOffset
		out.writeInt(0);
//		UInt32 kvdByteLength
		out.writeInt(0);
//		UInt64 sgdByteOffset
		out.writeLong(0);
//		UInt64 sgdByteLength
		out.writeLong(0);
		// Level Index 
//		struct {
//		    UInt64 byteOffset
//		    UInt64 byteLength
//		    UInt64 uncompressedByteLength
//		} levels[max(1, levelCount)]
		long levelsOffsetBase = dfdByteOffset + dfdTotalSize;
		
		// compute size and offset before writing it
		// because lowest mip levels are written first
		LongArray levelOffsets = new LongArray();
		LongArray levelCompressedSizes = new LongArray();
		LongArray levelSizes = new LongArray();
		for(ImageLevel level : levels){
			long levelSize = 0;
			for(ImageLayer layer : level.layers){
				for(ImageFace face : layer.faces){
					levelSize += face.data.length;
				}
			}
			if(supercompressionScheme == KTX2Format.SUPERCOMPRESSION_NONE){
				levelCompressedSizes.add(levelSize);
			}else{
				levelCompressedSizes.add(level.compressedData.length);
			}
			levelSizes.add(levelSize);
			levelOffsets.add(0);
		}
		long computedLevelOffset = 0;
		for(int i=levels.size-1 ; i>=0 ; i--){
			levelOffsets.set(i, computedLevelOffset);
			computedLevelOffset += levelCompressedSizes.get(i);
		}
		
		for(int i=0 ; i<levels.size ; i++){
			out.writeLong(levelsOffsetBase + levelOffsets.get(i));
			out.writeLong(levelCompressedSizes.get(i));
			out.writeLong(levelSizes.get(i));
		}
		
		// Data Format Descriptor 
//		UInt32 dfdTotalSize
		out.writeInt(dfdTotalSize);
//		continue
//		    dfDescriptorBlock dfdBlock
//		          ︙
//		until dfdByteLength read
		for(int i : formatEncoded) out.writeInt(i);

		// Key/Value Data 
//		continue
//		    UInt32   keyAndValueByteLength
//		    Byte     keyAndValue[keyAndValueByteLength]
//		    align(4) valuePadding 
//		                    ︙
//		until kvdByteLength read
//		if (sgdByteLength > 0)
//		    align(8) sgdPadding

		// Supercompression Global Data 
//		Byte supercompressionGlobalData[sgdByteLength]

		// Mip Level Array 
//		for each mip_level in levelCount 
//		    Byte     levelImages[bytesOfLevelImages] 
//		end

		
//		align( lcm(texel_block_size, 4) ) mipPadding 
//		for each layer in max(1,layerCount)
//		   for each face in faceCount
//		       for each z_slice_of_blocks in num_blocks_z 
//		           for each row_of_blocks in num_blocks_y 
//		               for each block in num_blocks_x 
//		                   Byte data[format_specific_number_of_bytes] 
//		               end
//		           end
//		       end
//		   end
//		end
		
		// TODO write in reverse order ?
		for(int i=0 ; i<levels.size ; i++){
			
			// ImageLevel level = levels.get(levels.size-1-i);
			ImageLevel level = levels.get(levels.size-1-i);
			
			if(supercompressionScheme == KTX2Format.SUPERCOMPRESSION_NONE){
				for(ImageLayer layer : level.layers){
					for(ImageFace face : layer.faces){
						out.write(face.data);
					}
				}
			}else{
				out.write(level.compressedData);
			}
			
		}
		
		((FilterOutputStream)out).close();
	}

	private static byte[] compressETC2(Pixmap pixmap) {
		throw new GdxRuntimeException("ETC2 compression not implemented");
	}
	
	private void compress() throws IOException{
		if(textureCompression == TextureCompression.ETC2){
			throw new GdxRuntimeException("ETC2 compression not implemented");
		}
		
		if(supercompressionScheme == KTX2Format.SUPERCOMPRESSION_ZLIB){
			for(ImageLevel level : levels){
				Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
				int levelSize = 0;
				for(ImageLayer layer : level.layers){
					for(ImageFace face : layer.faces){
						levelSize += face.data.length;
					}
				}
				byte [] levelData = new byte[levelSize];
				int pos = 0;
				for(ImageLayer layer : level.layers){
					for(ImageFace face : layer.faces){
						System.arraycopy(face.data, 0, levelData, pos, face.data.length);
						pos += face.data.length;
					}
				}
				deflater.setInput(levelData);
				deflater.finish();
				
				byte[] buffer = new byte[4096 * 1024]; // TODO config
				int r;
				pos = 0;
				System.out.println("Deflate");
				while((r = deflater.deflate(buffer)) > 0){
					
					System.out.println("Deflate copy");
					// Case when compressed data is greater than uncompressed data (could be the case with noise textures)
					if(pos + r > levelData.length){
						byte[] tmp = new byte[levelData.length * 2];
						System.arraycopy(levelData, 0, tmp, 0, levelData.length);
						levelData = tmp;
					}
					
					System.arraycopy(buffer, 0, levelData, pos, r);
					pos += r;
				}
				System.out.println("Deflate finish");
				deflater.end();
				level.compressedData = new byte[pos];
				System.arraycopy(levelData, 0, level.compressedData, 0, pos);
			}
		}
	}
	
}
