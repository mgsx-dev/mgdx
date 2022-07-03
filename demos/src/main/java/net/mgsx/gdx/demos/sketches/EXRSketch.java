package net.mgsx.gdx.demos.sketches;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.util.tinyexr.EXRChannelInfo;
import org.lwjgl.util.tinyexr.EXRHeader;
import org.lwjgl.util.tinyexr.EXRImage;
import org.lwjgl.util.tinyexr.EXRVersion;
import org.lwjgl.util.tinyexr.TinyEXR;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.g2d.RGBE;
import net.mgsx.gdx.graphics.g2d.RGBE.Header;

// POC based on : http://forum.lwjgl.org/index.php?topic=6757.msg35684#msg35684
// TODO copy half to float conv
// TODO layer, mipmaps, ect...
// compatiblity : https://github.com/syoyo/tinyexr#features
//
public class EXRSketch extends ScreenAdapter {

	String exrPath = "/home/germain/git/mgdx/demos/assets/textures/demo2/table_mountain_2_4k.exr";

	public EXRSketch(){
		if(false){
			try {
				testSave();
			} catch (IOException e) {
				throw new GdxRuntimeException(e);
			}
		}
		testLoad();
	}
	
	public void testSave()throws IOException{
		FileHandle file = Gdx.files.absolute("/home/germain/git/mgdx/demos/assets/textures/demo2/table_mountain_2_4k.hdr");
		
		
		DataInputStream in = new DataInputStream(new BufferedInputStream(file.read()));
		Header hdrHeader = RGBE.readHeader(in);
		int numPixels = hdrHeader.getWidth() * hdrHeader.getHeight();
		byte[] hdrData = new byte[numPixels * 4];
		RGBE.readPixelsRawRLE(in, hdrData, 0, hdrHeader.getWidth(), hdrHeader.getHeight());
		
		int components = 3;
		FloatBuffer buffer = BufferUtils.newFloatBuffer(hdrHeader.getWidth() * hdrHeader.getHeight() * components);
    	float [] pixels = new float[components];
    	for(int i=0 ; i<hdrData.length ; i+=4){
    		RGBE.rgbe2float(pixels, hdrData, i);
    		buffer.put(pixels);
    	}
    	buffer.flip();
    	
	    EXRHeader exr_header = EXRHeader.create();
	    TinyEXR.InitEXRHeader(exr_header);
	 
	    exr_header.num_channels(3);
	    
	    int inType = TinyEXR.TINYEXR_PIXELTYPE_FLOAT;
	    
	    int outType = TinyEXR.TINYEXR_PIXELTYPE_FLOAT;
	    
	    exr_header.pixel_types(ints(inType,inType,inType));
	    exr_header.requested_pixel_types(ints(outType,outType,outType));
	    
	    EXRChannelInfo.Buffer rb = EXRChannelInfo.create(3); // .Buffer(org.lwjgl.BufferUtils.createByteBuffer(EXRChannelInfo.SIZEOF * 3));
	    exr_header.channels(rb);
	    rb.get(0).name(bytes("R"));
	    rb.get(1).name(bytes("G"));
	    rb.get(2).name(bytes("B"));
	    
	    
	    EXRImage exr_image = EXRImage.create();
	    TinyEXR. InitEXRImage(exr_image);
	    
	    exr_image.num_channels(3);
	    
	    int nbPixels = hdrHeader.getWidth() * hdrHeader.getHeight();
	    FloatBuffer cR = org.lwjgl.BufferUtils.createFloatBuffer(nbPixels);
	    FloatBuffer cG = org.lwjgl.BufferUtils.createFloatBuffer(nbPixels);
	    FloatBuffer cB = org.lwjgl.BufferUtils.createFloatBuffer(nbPixels);
	    buffer.rewind();
	    for(int i=0 ; i<nbPixels ; i++){
	    	cR.put(buffer.get());
	    	cG.put(buffer.get());
	    	cB.put(buffer.get());
	    }
	    cR.flip();
	    cG.flip();
	    cB.flip();
	    
	    PointerBuffer pb = org.lwjgl.BufferUtils.createPointerBuffer(3);
	    pb.put(cR);
	    pb.put(cG);
	    pb.put(cB);
	    pb.flip();
	    exr_image.images(pb);
	    
	    exr_image.width(hdrHeader.getWidth());
	    exr_image.height(hdrHeader.getHeight());
	    
	    
	    
	    PointerBuffer err = org.lwjgl.BufferUtils.createPointerBuffer(1);
	    int ret = TinyEXR.SaveEXRImageToFile(exr_image, exr_header, exrPath, err);
	    
	    System.out.println(ret);
	}
	
	private IntBuffer ints(int...values){
		IntBuffer b = BufferUtils.newIntBuffer(values.length);
		b.put(values);
		b.flip();
		return b;
	}
	private ByteBuffer bytes(String value){
		ByteBuffer b = BufferUtils.newByteBuffer(value.length()+1);
		b.put(value.getBytes());
		b.put((byte)0);
		b.flip();
		return b;
	}
	
	private void testLoad(){
    	
	    PointerBuffer err = org.lwjgl.BufferUtils.createPointerBuffer(1);

		
		 exrPath = "/usr/lib/blender-2.93/2.93/datafiles/studiolights/world/night.exr";
		
	   // exrPath = "/usr/lib/blender-2.93/2.93/datafiles/studiolights/world/forest.exr"
	    
		EXRVersion exr_version = EXRVersion.create();
		 
	    int ret = TinyEXR.ParseEXRVersionFromFile(exr_version, exrPath);
	    if (ret != TinyEXR.TINYEXR_SUCCESS) {
	        System.out.println("Invalid EXR file: " + exrPath);
	    }
	    System.out.println(exr_version.version());
	    System.out.println( exr_version.tiled());
	    System.out.println( exr_version.multipart());
	    System.out.println(exr_version.non_image());
	    
		
	    EXRHeader exr_header = EXRHeader.create();
	    TinyEXR.InitEXRHeader(exr_header);
	 
	    ret = TinyEXR.ParseEXRHeaderFromFile(exr_header, exr_version, exrPath, err);
	    if (ret != TinyEXR.TINYEXR_SUCCESS) {
	        System.out.println("EXR parse error: " + err.get(0));
	    }	
	    
//	    IntBuffer b = exr_header.pixel_types();
//	    // 2 2 2
//	    System.out.println(b.get());
//	   //  exr_header.num_channels()
//	    
	    
	    EXRImage exr_image = EXRImage.create();
	    TinyEXR. InitEXRImage(exr_image);
	 
	    PointerBuffer err2 = org.lwjgl.BufferUtils.createPointerBuffer(1);
	    ret = TinyEXR.LoadEXRImageFromFile(exr_image, exr_header, exrPath, err2);
	    if (ret != TinyEXR.TINYEXR_SUCCESS) {
	        System.out.println("EXR load error: " + err.get(0));
	    }
	 
	    PointerBuffer images = exr_image.images();
	    int w = exr_image.width();
	    int h = exr_image.height();
	    int c = exr_image.num_channels();
	 
	    System.out.println(w + " x " + h + " x " + c);
	 
	    FloatBuffer cR = images.getFloatBuffer(0, w * h);    
	    FloatBuffer cG = images.getFloatBuffer(1, w * h);    
	    FloatBuffer cB = images.getFloatBuffer(2, w * h);    
	    
	    int nbPix = w*h;
	    
	    Pixmap pixmap = new Pixmap(w, h, Format.RGB888);
	    ByteBuffer pxBuf = pixmap.getPixels();
	    pxBuf.rewind();
	    for(int i=0 ; i<nbPix ; i++){
	    	// System.out.println(i);
	    	pxBuf.put((byte)MathUtils.round(cR.get() * 255));
	    	pxBuf.put((byte)MathUtils.round(cG.get() * 255));
	    	pxBuf.put((byte)MathUtils.round(cB.get() * 255));
	    }
	    pxBuf.flip();
	    
	    PixmapIO.writePNG(Gdx.files.absolute("/home/germain/git/mgdx/demos/assets/textures/demo2/table_mountain_2_4k.png"), pixmap);
	    
	}
}
