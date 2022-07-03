package net.mgsx.gdx.exr;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyexr.EXRHeader;
import org.lwjgl.util.tinyexr.EXRImage;
import org.lwjgl.util.tinyexr.EXRVersion;
import org.lwjgl.util.tinyexr.TinyEXR;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.StreamUtils;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.graphics.glutils.FlexTextureData;

public class EXRLoader {

	private Array<ByteBuffer> imageChannels = new Array<ByteBuffer>();
	private IntArray channelsTypes = new IntArray();
	private int width, height, mipMapCount, layerCount, numChannels;
	
	public void load(FileHandle exrFile){
		try{
			if(exrFile.type() == FileType.Absolute || exrFile.type() == FileType.Local || exrFile.type() == FileType.External){
				load(exrFile.path());
			}else{
				load(exrFile.read());
			}
		}catch(IOException e){
			throw new GdxRuntimeException(e);
		}
	}
	
	public Texture createTexture(GLFormat format){
	    FloatBuffer pxBuf = BufferUtils.newFloatBuffer(width * height * format.bppCpu);
	    pxBuf.rewind();
	    int nbPix = width * height;
	    for(int i=0 ; i<nbPix ; i++){
	    	for(ByteBuffer channel : imageChannels){
	    		pxBuf.put(channel.getFloat());
	    	}
	    }
	    pxBuf.flip();
	    FlexTextureData data = new FlexTextureData(width, height, 0, format.internalFormat, format.format, format.type, pxBuf);
	    return new Texture(data);
	}
	
	public Array<ByteBuffer> getRawChannelsData() {
		return imageChannels;
	}
	
	public ByteBuffer convertToRawPixels(){
		int bpc = 4; // TODO how to convert?
		
		ByteBuffer interleavedPixels = BufferUtils.newByteBuffer(width * height * imageChannels.size * bpc);
		byte [] tmp = new byte[bpc];
		for(ByteBuffer cb : imageChannels){
			cb.rewind();
		}
		for(int i=0, n=width*height ; i<n ; i++){
			for(int c=0 ; c<imageChannels.size ; c++){
				imageChannels.get(c).get(tmp);
				interleavedPixels.put(tmp);
			}
		}
		
		return interleavedPixels;
	}
	
	public void load(InputStream stream) throws IOException {
		ByteBuffer buffer = BufferUtils.newByteBuffer(stream.available());
		StreamUtils.copyStream(stream, buffer);
		load(buffer);
	}

	public void load(ByteBuffer buffer) {
		
	}

	public void load(String exrPath) throws IOException{
		LoaderFile loader = new LoaderFile(exrPath);
		loader.load();
	}
	
	private abstract class Loader {
		
		public void load() throws IOException{
			EXRVersion exr_version = null;
			EXRHeader exr_header = null;
			EXRImage exr_image = null;
			PointerBuffer err = null;
			try{
				exr_version = EXRVersion.create();
				int ret = parseVersion(exr_version);
			    if (ret != TinyEXR.TINYEXR_SUCCESS) {
			    	throw new IOException(errorMessage("Invalid EXR file"));
			    }
	//		    System.out.println(exr_version.version());
	//		    System.out.println( exr_version.tiled());
	//		    System.out.println( exr_version.multipart());
	//		    System.out.println(exr_version.non_image());
			    
			    err = org.lwjgl.BufferUtils.createPointerBuffer(1);
				
			    exr_header = EXRHeader.create();
			    TinyEXR.InitEXRHeader(exr_header);
			    ret = parseHeader(exr_header, exr_version, err);
			    if (ret != TinyEXR.TINYEXR_SUCCESS) {
			    	throw new IOException(errorMessage("EXR parse error", err));
			    }
			    
			    // Read HALF channel as FLOAT.
			    // Read UINT channel as FLOAT.
		        for (int i = 0; i < exr_header.num_channels(); i++) {
		            if (exr_header.pixel_types().get(i) == TinyEXR.TINYEXR_PIXELTYPE_HALF) {
		                exr_header.requested_pixel_types().put(i, TinyEXR.TINYEXR_PIXELTYPE_FLOAT);
		            }else if (exr_header.pixel_types().get(i) == TinyEXR.TINYEXR_PIXELTYPE_UINT) {
		                exr_header.requested_pixel_types().put(i, TinyEXR.TINYEXR_PIXELTYPE_FLOAT);
		            }
		        }
			    
			    exr_image = EXRImage.create();
			    TinyEXR. InitEXRImage(exr_image);
			    ret = loadImage(exr_image, exr_header, err);
			    if (ret != TinyEXR.TINYEXR_SUCCESS) {
			    	throw new IOException(errorMessage("EXR read error", err));
			    }
			 
			    PointerBuffer images = exr_image.images();
			    width = exr_image.width();
			    height = exr_image.height();
			    numChannels = exr_image.num_channels();
			 
			    IntBuffer typesBuffer = exr_header.pixel_types();
			    
			    for(int c=0 ; c<numChannels ; c++){
			    	int channelType = typesBuffer.get();
			    	channelsTypes.add(channelType);
			    	if(channelType == TinyEXR.TINYEXR_PIXELTYPE_FLOAT){
			    		imageChannels.add(images.getByteBuffer(c, width * height * 4));
			    	}else if(channelType == TinyEXR.TINYEXR_PIXELTYPE_HALF){
			    		imageChannels.add(images.getByteBuffer(c, width * height * 2));
			    	}else if(channelType == TinyEXR.TINYEXR_PIXELTYPE_UINT){
			    		imageChannels.add(images.getByteBuffer(c, width * height * 4));
			    	}
			    }
			    
			}finally{
				if(exr_header != null) TinyEXR.FreeEXRHeader(exr_header);
				if(exr_image != null) TinyEXR.FreeEXRImage(exr_image);
				// TODO ? if(err != null) TinyEXR.FreeEXRErrorMessage(err.);
			}
		}


		protected String errorMessage(String message, PointerBuffer err) throws IOException {
			String reason = MemoryUtil.memUTF8(err.get());
			return errorMessage(message + ": " + reason);
		}


		protected String errorMessage(String message) {
			return message;
		}


		abstract protected int parseVersion(EXRVersion exr_version);
		abstract protected int parseHeader(EXRHeader exr_header, EXRVersion exr_version, PointerBuffer err);
		abstract protected int loadImage(EXRImage exr_image, EXRHeader exr_header, PointerBuffer err);
	}
	
	private class LoaderFile extends Loader {
		
		private String exrPath;
		
		public LoaderFile(String exrPath) {
			super();
			this.exrPath = exrPath;
		}

		@Override
		protected int parseVersion(EXRVersion exr_version) {
			return TinyEXR.ParseEXRVersionFromFile(exr_version, exrPath);
		}

		@Override
		protected int parseHeader(EXRHeader exr_header, EXRVersion exr_version, PointerBuffer err) {
			return TinyEXR.ParseEXRHeaderFromFile(exr_header, exr_version, exrPath, err);
		}

		@Override
		protected int loadImage(EXRImage exr_image, EXRHeader exr_header, PointerBuffer err) {
			return TinyEXR.LoadEXRImageFromFile(exr_image, exr_header, exrPath, err);
		}
		
	}
	
	private class LoaderMemory extends Loader {

		@Override
		protected int parseVersion(EXRVersion exr_version) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		protected int parseHeader(EXRHeader exr_header, EXRVersion exr_version, PointerBuffer err) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		protected int loadImage(EXRImage exr_image, EXRHeader exr_header, PointerBuffer err) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
}
