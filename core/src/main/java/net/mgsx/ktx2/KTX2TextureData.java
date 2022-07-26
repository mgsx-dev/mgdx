package net.mgsx.ktx2;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.CubemapData;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.TextureArrayData;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.LittleEndianInputStream;

import net.mgsx.gdx.graphics.Texture3D;
import net.mgsx.gdx.graphics.Texture3DData;
import net.mgsx.ktx2.KTX2Data.ImageFace;
import net.mgsx.ktx2.KTX2Data.ImageLayer;
import net.mgsx.ktx2.KTX2Data.ImageLevel;

// TODO no need for little endian classes, unsafe ByteBuffer is enough

// example: https://github.com/KhronosGroup/glTF-Sample-Environments/blob/master/doge2/lambertian/diffuse.ktx2
public class KTX2TextureData implements TextureData, CubemapData, Texture3DData, TextureArrayData {
	
	private int width;
	private int height;
	private int depth;
	private FileHandle file;
	
	/** all texture parts ordered by level, layer, face */
	private Array<ImageLevel> levels = new Array<ImageLevel>();
	private int layerCount;
	private int faceCount;
	private int levelCount;
	private boolean layered;
	private int glInternalFormat;
	private int glType;
	private int glFormat;
	private int glTarget;
	private boolean compressed;
	
	public KTX2TextureData(FileHandle file) {
		this.file = file;
	}
	
	private void read() throws IOException{
		LittleEndianInputStream in = new LittleEndianInputStream(file.read());
		long read = 0;
		for(int i=0 ; i<KTX2Format.FileIdentifier.length ; i++){
			if(in.readByte() != KTX2Format.FileIdentifier[i]) throw new IOException("bad magic");
		}
		read += KTX2Format.FileIdentifier.length;
		
		// Pixels format
		int vkFormat = in.readInt(); read += 4;
		/*
		if(vkFormat == KTX2Format.VK_FORMAT_R16G16B16A16_SFLOAT){
			// TODO
		}
		else if(vkFormat == KTX2Format.VK_FORMAT_R8G8B8A8_UNORM){
			
		}
		else if(vkFormat != KTX2Format.VK_FORMAT_UNDEFINED) throw new GdxRuntimeException("Vulkan not supported");
		*/

		int vkFormatSize = in.readInt(); read += 4;
		/*
		if(vkFormatSize == 2){
			// TODO
		}else if(vkFormatSize == 1){
			
		}
		else if(vkFormatSize != KTX2Format.VK_FORMAT_UNDEFINED_TYPE_SIZE) throw new GdxRuntimeException("Vulkan not supported");
		*/
		
		width = in.readInt(); read += 4;
		height = in.readInt(); read += 4;
		depth = in.readInt(); read += 4;
		layerCount = in.readInt(); read += 4;
		faceCount = in.readInt(); read += 4;
		levelCount = in.readInt(); read += 4;
		
		layered = true;
		if(layerCount == 0){
			layerCount = 1;
			layered = false;
		}
		
		int superCompression = in.readInt(); read += 4;
		if(superCompression == KTX2Format.SUPERCOMPRESSION_NONE){
			
		}else if(superCompression == KTX2Format.SUPERCOMPRESSION_ZLIB){
			
		}else{
			throw new GdxRuntimeException("Super compression scheme not supported: " + superCompression);
		}
		
		int dfdByteOffset = in.readInt(); read += 4;
		int dfdByteLength = in.readInt(); read += 4;
		int kvdByteOffset = in.readInt(); read += 4;
		int kvdByteLength = in.readInt(); read += 4;
		long sgdByteOffset = in.readLong(); read += 8;
		long sgdByteLength = in.readLong(); read += 8;
		
		
		int nbLevels = levelCount; // TODO levelCount 0 means, generate mipmaps at runtime...
		for(int i=0 ; i<nbLevels ; i++){
			long levelOffset = in.readLong(); read += 8;
			long levelSize = in.readLong(); read += 8;
			long uncompressedLevelSize = in.readLong(); read += 8;
			
			ImageLevel level = new ImageLevel();
			level.offset = levelOffset;
			level.size = levelSize;
			level.uncompressedSize = uncompressedLevelSize;
			levels.add(level);
			for(int layer=0 ; layer<layerCount ; layer++){
				ImageLayer l = new ImageLayer();
				level.layers.add(l);
				for(int face=0 ; face<faceCount ; face++){
					ImageFace f = new ImageFace();
					l.faces.add(f);
					
					// evaluate size of this chunk
					int baseSize = (int)uncompressedLevelSize / faceCount;
					
					// XXX verify size
					int expSize = width * height;
					expSize = expSize >> (i*2);
					expSize *= 8;
					
					f.buffer = BufferUtils.newByteBuffer(baseSize);
				}
			}
		}
		
		int dfdTotalSize = in.readInt(); read += 4;
		
		// parse format
		KHRDataFormat format = new KHRDataFormat();
		read += format.decode(in);
		
		// TODO if(kvdByteOffset - dfdByteOffset != dfdTotalSize) throw new GdxRuntimeException("bad offsets");
		// skip kvd
		int pad = 0;
		while(read < kvdByteOffset + kvdByteLength){
			int kvLength = in.readInt(); read += 4;
			byte[] kv = new byte[kvLength];
			in.read(kv); read += kvLength;
			pad += kvLength + 4;
			int np = 0;
			for( ; np<kvLength && kv[np] != 0 ; np++);
			String k = new String(kv, 0, np);
			int npp = np+1;
			for(np++ ; np<kvLength && kv[np] != 0 ; np++);
			String v = new String(kv, npp, np - npp);
			System.out.println(k + ": " + v);
			int sk = pad % 4;
			if(sk != 0) {
				read += 4 - sk;
				in.skipBytes(4 - sk);
			}
		}
		
		// TODO more format
		if(vkFormat == KTX2Format.VK_FORMAT_R32G32B32A32_SFLOAT){
			glInternalFormat = GL30.GL_RGBA32F;
			glType = GL30.GL_FLOAT;
			glFormat = GL30.GL_RGBA;
		}else if(vkFormat == KTX2Format.VK_FORMAT_R32G32B32_SFLOAT){
			glInternalFormat = GL30.GL_RGB32F;
			glType = GL30.GL_FLOAT;
			glFormat = GL30.GL_RGB;
		}else if(vkFormat == KTX2Format.VK_FORMAT_R16G16B16A16_SFLOAT){
			glInternalFormat = GL30.GL_RGBA16F;
			glType = GL30.GL_HALF_FLOAT;
			glFormat = GL30.GL_RGBA;
		}else if(vkFormat == KTX2Format.VK_FORMAT_R16G16B16_SFLOAT){
			glInternalFormat = GL30.GL_RGB16F;
			glType = GL30.GL_HALF_FLOAT;
			glFormat = GL30.GL_RGB;
		}else if(vkFormat == KTX2Format.VK_FORMAT_R8G8B8A8_UNORM){
			glInternalFormat = GL30.GL_RGBA8;
			glType = GL30.GL_UNSIGNED_BYTE;
			glFormat = GL30.GL_RGBA;
		}else if(vkFormat == KTX2Format.VK_FORMAT_R8G8B8_UNORM){
			glInternalFormat = GL30.GL_RGB8;
			glType = GL30.GL_UNSIGNED_BYTE;
			glFormat = GL30.GL_RGB;
		}else if(vkFormat == KTX2Format.VK_FORMAT_R8_UNORM){
			glInternalFormat = GL30.GL_R8;
			glType = GL30.GL_UNSIGNED_BYTE;
			glFormat = GL30.GL_RED;
		}else if(vkFormat == KTX2Format.VK_FORMAT_R8G8_UNORM){
			glInternalFormat = GL30.GL_RG8;
			glType = GL30.GL_UNSIGNED_BYTE;
			glFormat = GL30.GL_RG;
		}else if(vkFormat == KTX2Format.VK_FORMAT_R4G4B4A4_UNORM_PACK16){
			glInternalFormat = GL30.GL_RGBA4;
			glType = GL30.GL_UNSIGNED_SHORT_4_4_4_4;
			glFormat = GL30.GL_RGBA;
		}else if(vkFormat == KTX2Format.VK_FORMAT_R5G6B5_UNORM_PACK16){
			glInternalFormat = GL30.GL_RGB565;
			glType = GL30.GL_UNSIGNED_SHORT_5_6_5;
			glFormat = GL30.GL_RGB;
		}else if(vkFormat == KTX2Format.VK_FORMAT_ETC2_R8G8B8A8_UNORM_BLOCK){
			glInternalFormat = GL30.GL_COMPRESSED_RGBA8_ETC2_EAC;
			glType = 0;
			glFormat = GL30.GL_RGBA;
			compressed = true;
		}else if(vkFormat == KTX2Format.VK_FORMAT_ETC2_R8G8B8_UNORM_BLOCK){
			glInternalFormat = GL30.GL_COMPRESSED_RGB8_ETC2;
			glType = 0;
			glFormat = GL30.GL_RGB;
			compressed = true;
		}else{
			throw new GdxRuntimeException("unsupported vk format: " + vkFormat);
		}
		
		// read pixel data
		byte [] buffer = new byte[4096];
		for(int level=0 ; level<levelCount ; level++){
			ImageLevel levelData = levels.get(levelCount-1-level);
			long skip = levelData.offset - read;
			if(skip > 0){
				in.skip(skip);
				read += skip;
			}
			
			if(superCompression == KTX2Format.SUPERCOMPRESSION_ZLIB){
				Inflater inflater = new Inflater(true);
				byte [] compressedData = new byte[(int)levelData.size];
				in.read(compressedData); read += compressedData.length;
				inflater.setInput(compressedData);
				for(int layer=0 ; layer<layerCount ; layer++){
					for(int face=0 ; face<faceCount ; face++){
						ImageFace f = levelData.layers.get(layer).faces.get(face);
						byte [] data = new byte[f.buffer.capacity()];
						try {
							inflater.inflate(data);
						} catch (DataFormatException e) {
							throw new IOException(e);
						}
						f.buffer.put(data);
						f.buffer.flip();
					}
				}
				
			}else{
				for(int layer=0 ; layer<layerCount ; layer++){
					for(int face=0 ; face<faceCount ; face++){
						ImageFace f = levelData.layers.get(layer).faces.get(face);
						int capacity = f.buffer.capacity();
						// TODO padding ?
						while(capacity > 0){
							int r = in.read(buffer, 0, Math.min(capacity, buffer.length)); 
							read += r;
							if(r < 0) throw new EOFException();
							f.buffer.put(buffer, 0, r);
							capacity -= r;
						}
						f.buffer.flip();
					}
				}
			}
			
		}
		
		
		
		if(faceCount == 6){
			glTarget = GL20.GL_TEXTURE_CUBE_MAP;
		}else if(faceCount == 1){
			if(depth != 0){
				glTarget = GL30.GL_TEXTURE_3D;
			}else{
				if(layered){
					glTarget = GL30.GL_TEXTURE_2D_ARRAY;
				}else{
					glTarget = GL20.GL_TEXTURE_2D;
				}
			}
		}else{
			throw new GdxRuntimeException("face count invalid: " + faceCount);
		}
		
		if(levelCount != 1) useMipMaps = true;

		in.close();
	}
	
	private void consumeData(){
		if(!prepared) throw new GdxRuntimeException("Not prepared");
		
		for(int level=0 ; level<levelCount ; level++){
			for(int layer=0 ; layer<layerCount ; layer++){
				for(int face=0 ; face<faceCount ; face++){
					ImageLevel levelData = levels.get(level);
					ByteBuffer data = levelData.layers.get(layer).faces.get(face).buffer;
					Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
					if(glTarget == GL30.GL_TEXTURE_3D){
						Gdx.gl30.glTexImage3D(glTarget, level, glInternalFormat, width, height, depth, 0, glFormat, glType, data);
					}else if(glTarget == GL30.GL_TEXTURE_2D_ARRAY){
						// TODO glTexImage3D avant ?
						Gdx.gl30.glTexSubImage3D(glTarget, level, 0, 0, layer, width, height, 1, glInternalFormat, glType, data);
					}else if(glTarget == GL20.GL_TEXTURE_CUBE_MAP){
						Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, level, glInternalFormat, width >> level, height >> level, 0, glFormat, glType, data);
					}else{
						if(compressed){
							Gdx.gl.glCompressedTexImage2D(glTarget, level, glInternalFormat, width, height, 0, data.capacity(), data);
						}else{
							Gdx.gl.glTexImage2D(glTarget, level, glInternalFormat, width, height, 0, glFormat, glType, data);
						}
					}
				}
			}
			// Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
		}
	}
	
	@Override
	public void consumeCustomData(int target) {
		if(faceCount != 1) throw new GdxRuntimeException("Doesn't contain 2D data");
		consumeData();
	}
	
	@Override
	public void consumeCubemapData() {
		if(faceCount != 6) throw new GdxRuntimeException("Doesn't contain cube map data");
		consumeData();
	}
	@Override
	public void consumeTextureArrayData() {
		if(!layered) throw new GdxRuntimeException("Doesn't contain 2D Array data");
		consumeData();
	}
	@Override
	public void consume3DData() {
		if(glTarget != GL30.GL_TEXTURE_3D) throw new GdxRuntimeException("Doesn't contain3D data");
		consumeData();
	}
	
	/**
	 * Example :
	 * 	int lb = f.buffer.get() & 0xFF;
	 *	int hb = f.buffer.get() & 0xFF;
	 *	int code = lb | (hb << 8);
	 *	float value = halfFloatToFloat(code);
	 * 
	 * @param hbits
	 * @return
	 */
	public static float halfFloatToFloat( int hbits )
	{
	    int mant = hbits & 0x03ff;            // 10 bits mantissa
	    int exp =  hbits & 0x7c00;            // 5 bits exponent
	    if( exp == 0x7c00 )                   // NaN/Inf
	        exp = 0x3fc00;                    // -> NaN/Inf
	    else if( exp != 0 )                   // normalized value
	    {
	        exp += 0x1c000;                   // exp - 15 + 127
	        if( mant == 0 && exp > 0x1c400 )  // smooth transition
	            return Float.intBitsToFloat( ( hbits & 0x8000 ) << 16
	                                            | exp << 13 | 0x3ff );
	    }
	    else if( mant != 0 )                  // && exp==0 -> subnormal
	    {
	        exp = 0x1c400;                    // make it normal
	        do {
	            mant <<= 1;                   // mantissa * 2
	            exp -= 0x400;                 // decrease exp by 1
	        } while( ( mant & 0x400 ) == 0 ); // while not normal
	        mant &= 0x3ff;                    // discard subnormal bit
	    }                                     // else +/-0 -> +/-0
	    return Float.intBitsToFloat(          // combine all parts
	        ( hbits & 0x8000 ) << 16          // sign  << ( 31 - 15 )
	        | ( exp | mant ) << 13 );         // value << ( 23 - 10 )
	}

	boolean prepared = false;
	private boolean useMipMaps;
	
	@Override
	public TextureDataType getType() {
		return TextureDataType.Custom;
	}

	@Override
	public boolean isPrepared() {
		return prepared;
	}

	@Override
	public void prepare() {
		try {
			read();
			prepared = true;
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public Pixmap consumePixmap() {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public boolean disposePixmap() {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Format getFormat() {
		throw new GdxRuntimeException("This TextureData implementation directly handles texture formats.");
	}

	@Override
	public boolean useMipMaps() {
		return useMipMaps;
	}

	@Override
	public boolean isManaged() {
		return true;
	}

	@Override
	public int getDepth() {
		return depth;
	}

	@Override
	public int getInternalFormat() {
		return glInternalFormat;
	}

	@Override
	public int getGLType() {
		return glType;
	}
	
	public int getTarget(){
		return glTarget;
	}
	
	public int getMipmapCount(){
		return levelCount;
	}

	public static GLTexture load(FileHandle file) {
		KTX2TextureData td = new KTX2TextureData(file);
		td.prepare();
		int glTarget = td.glTarget;
		if(glTarget == GL30.GL_TEXTURE_3D){
			return new Texture3D(td);
		}else if(glTarget == GL30.GL_TEXTURE_2D_ARRAY){
			return new TextureArray(td);
		}else if(glTarget == GL20.GL_TEXTURE_CUBE_MAP){
			return new Cubemap(td);
		}else{
			return new Texture(td);
		}
	}

}
