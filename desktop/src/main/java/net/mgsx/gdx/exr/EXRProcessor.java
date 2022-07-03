package net.mgsx.gdx.exr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.util.tinyexr.EXRAttribute;
import org.lwjgl.util.tinyexr.EXRChannelInfo;
import org.lwjgl.util.tinyexr.EXRHeader;
import org.lwjgl.util.tinyexr.EXRImage;
import org.lwjgl.util.tinyexr.TinyEXR;

import com.badlogic.gdx.utils.BufferUtils;

public class EXRProcessor {
	
	public enum EXRType {
		RGB16F(TinyEXR.TINYEXR_PIXELTYPE_HALF), 
		RGB32F(TinyEXR.TINYEXR_PIXELTYPE_FLOAT),
		RGB32I(TinyEXR.TINYEXR_PIXELTYPE_UINT);
		public final int tinyExrPixelType;
		private EXRType(int tinyExrPixelType) {
			this.tinyExrPixelType = tinyExrPixelType;
		}
	}
	public enum EXRCompression {
		NONE(TinyEXR.TINYEXR_COMPRESSIONTYPE_NONE), 
		PIZ(TinyEXR.TINYEXR_COMPRESSIONTYPE_PIZ),
		RLE(TinyEXR.TINYEXR_COMPRESSIONTYPE_RLE),
		ZFP(TinyEXR.TINYEXR_COMPRESSIONTYPE_ZFP),
		ZIP(TinyEXR.TINYEXR_COMPRESSIONTYPE_ZIP),
		ZIPS(TinyEXR.TINYEXR_COMPRESSIONTYPE_ZIPS),
		;
		public final int tinyExrCompression;
		private EXRCompression(int tinyExrCompression) {
			this.tinyExrCompression = tinyExrCompression;
		}
	}
	
	public static class ZFPConfiguration{
		// public int mode = TinyEXR.TINYEXR_ZFP_COMPRESSIONTYPE_RATE;
		public Double compressionRate;
		public Integer compressionPrecision;
		public Double compressionTolerance;
	}
	
	public void save(String exrPath, int width, int height, FloatBuffer rgbBuffer, EXRType outputType, EXRCompression compression, ZFPConfiguration config) throws IOException{
		
		EXRHeader exr_header = null;
		EXRImage exr_image = null;
		PointerBuffer pb = null;
		EXRChannelInfo.Buffer rb = null;
		try{
			int numChannels = 3;
			
			exr_header = EXRHeader.create();
		    TinyEXR.InitEXRHeader(exr_header);
		    exr_header.num_channels(numChannels);
		    int inType = TinyEXR.TINYEXR_PIXELTYPE_FLOAT;
		    int outType = outputType.tinyExrPixelType;
		    
		    // set pixel format
		    IntBuffer pixel_types = BufferUtils.newIntBuffer(numChannels);
		    IntBuffer requested_pixel_types = BufferUtils.newIntBuffer(numChannels);
		    for(int i=0 ; i<numChannels ; i++){
		    	pixel_types.put(inType);
		    	requested_pixel_types.put(outType);
		    }
		    pixel_types.flip();
		    requested_pixel_types.flip();
		    exr_header.pixel_types(pixel_types);
		    exr_header.requested_pixel_types(requested_pixel_types);
		    
		    exr_header.compression_type(compression.tinyExrCompression);
		    
		    if(compression == EXRCompression.ZFP){
		    	EXRAttribute.Buffer attributes = EXRAttribute.create(1);
		    	exr_header.custom_attributes(attributes);
		    	if(config.compressionRate != null){
		    		attributes.apply(a->a.set(toNTS("zfpCompressionRate"), toNTS("double"), doubleBuffer(config.compressionRate)));
		    	}else if(config.compressionPrecision != null){
		    		attributes.apply(a->a.set(toNTS("zfpCompressionPrecision"), toNTS("int32"), intBuffer(config.compressionPrecision)));
		    	}else if(config.compressionTolerance != null){
		    		attributes.apply(a->a.set(toNTS("zfpCompressionTolerance"), toNTS("double"), doubleBuffer(config.compressionTolerance)));
		    	}
		    }
		    
		    // set channels info
		    rb = EXRChannelInfo.create(numChannels);
		    exr_header.channels(rb);
		    rb.get(0).name(toNTS("R"));
		    rb.get(1).name(toNTS("G"));
		    rb.get(2).name(toNTS("B"));
		    
		    
		    exr_image = EXRImage.create();
		    TinyEXR. InitEXRImage(exr_image);
		    exr_image.num_channels(numChannels);
		    
		    int nbPixels = width * height;
		    FloatBuffer cR = org.lwjgl.BufferUtils.createFloatBuffer(nbPixels);
		    FloatBuffer cG = org.lwjgl.BufferUtils.createFloatBuffer(nbPixels);
		    FloatBuffer cB = org.lwjgl.BufferUtils.createFloatBuffer(nbPixels);
		    rgbBuffer.rewind();
		    for(int i=0 ; i<nbPixels ; i++){
		    	cR.put(rgbBuffer.get());
		    	cG.put(rgbBuffer.get());
		    	cB.put(rgbBuffer.get());
		    }
		    cR.flip();
		    cG.flip();
		    cB.flip();
		    
		    pb = org.lwjgl.BufferUtils.createPointerBuffer(3);
		    pb.put(cR);
		    pb.put(cG);
		    pb.put(cB);
		    pb.flip();
		    exr_image.images(pb);
		    
		    exr_image.width(width);
		    exr_image.height(height);
		    
		    PointerBuffer err = org.lwjgl.BufferUtils.createPointerBuffer(1);
		    int ret = TinyEXR.SaveEXRImageToFile(exr_image, exr_header, exrPath, err);
		    if(ret != TinyEXR.TINYEXR_SUCCESS){
		    	String cause = err.getStringUTF8();
		    	throw new IOException("save error: " + cause);
		    }
		}finally{
			if(exr_header != null) TinyEXR.FreeEXRHeader(exr_header);
			if(exr_image != null) TinyEXR.FreeEXRImage(exr_image);
			// TODO other?
		}
	}
	
	private ByteBuffer doubleBuffer(double value) {
		ByteBuffer b = BufferUtils.newByteBuffer(8);
		b.putDouble(value);
		b.flip();
		return b;
	}
	private ByteBuffer intBuffer(int value) {
		ByteBuffer b = BufferUtils.newByteBuffer(4);
		b.putInt(value);
		b.flip();
		return b;
	}

	private static ByteBuffer toNTS(String value){
		ByteBuffer b = BufferUtils.newByteBuffer(value.length()+1);
		b.put(value.getBytes());
		b.put((byte)0);
		b.flip();
		return b;
	}
}
