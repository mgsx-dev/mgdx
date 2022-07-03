package net.mgsx.gdx.exr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.graphics.GLFormat;
import net.mgsx.gdx.graphics.g2d.HDRILoader;
import net.mgsx.ktx2.KTX2Data.MipMapMode;
import net.mgsx.ktx2.KTX2Processor;

public class HdrToKtx2 {
	public static void main(String[] args) {
		if(args.length < 2){
			printHelp();
			System.exit(1);
			return;
		}
		int argIndex;

		GLFormat format = GLFormat.RGB16;
		boolean cubemap = false;
		boolean compress = true;
		MipMapMode mipmaps = MipMapMode.NONE;
		
		for(argIndex=0 ; argIndex<args.length-2 ; argIndex++){
			String arg = args[argIndex];
			if(arg.equals("--cubemap")){
				cubemap = true;
			}else if(arg.equals("--equirectangular")){
				cubemap = false;
			}else if(arg.equals("--rgb32f")){
				format = GLFormat.RGB32;
			}else if(arg.equals("--uncompressed")){
				compress = false;
			}else if(arg.equals("--mipmap-none")){
				mipmaps = MipMapMode.NONE;
			}else if(arg.equals("--mipmap-raw")){
				mipmaps = MipMapMode.RAW;
			}else if(arg.equals("--mipmap-generate")){
				mipmaps = MipMapMode.GENERATE;
			}else if(arg.equals("--mipmap-runtime")){
				mipmaps = MipMapMode.RUNTIME;
			}else{
				System.err.println("unexpected option: " + arg);
				printHelp();
				System.exit(2);
				return;
			}
		}
		
		String inputFile = args[argIndex++];
		String outputFile = args[argIndex++];
		try {
			if(cubemap){
				convertToCubemap(inputFile, outputFile, format, mipmaps, compress);
			}else{
				convert(inputFile, outputFile, format, mipmaps, compress);
			}
			System.out.println("complete: " + outputFile);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static void convertToCubemap(String inputFile, String outputFile, GLFormat format, MipMapMode mipmaps, boolean compress) throws IOException {
		HDRILoader importer = new HDRILoader();
		importer.load(new FileInputStream(new File(inputFile)));
		FloatBuffer rgbBuffer = importer.createRGBBuffer().asFloatBuffer();
		
		// convert equirect to cube map
		int inWidth = importer.getWidth();
		int inHeight = importer.getHeight();
		int width = inHeight;
		int height = width;
		Array<ByteBuffer> faces = new Array<ByteBuffer>(6);
		float [] pixel = new float[3];
		for(int i=0 ; i<6 ; i++){
			ByteBuffer face = BufferUtils.newByteBuffer(width * height * format.bppCpu);
			FloatBuffer faceFloats = face.asFloatBuffer();
			faces.add(face);
			for(int y=0 ; y<height ; y++){
				for(int x=0 ; x<width ; x++){
					// TODO convert cube to equi and maybe sample around
					int inX = 0;
					int inY = 0;
					rgbBuffer.position(inY * inWidth + inX);
					rgbBuffer.get(pixel);
					faceFloats.put(pixel);
				}
			}
			face.flip();
		}
		
		KTX2Processor.exportCubemap(new FileOutputStream(new File(outputFile)), 
				faces, width, height, 1, 1, format.internalFormat, mipmaps, compress);
	}
	
	public static void convert(String inputFile, String outputFile, GLFormat format, MipMapMode mipmaps, boolean compress) throws IOException {
		HDRILoader importer = new HDRILoader();
		importer.load(new FileInputStream(new File(inputFile)));
		ByteBuffer rgbBuffer = importer.createRGBBuffer();
		int width = importer.getWidth();
		int height = importer.getHeight();
		// convert to HALF_FLOAT !
		System.out.println("Convert to half float");
		if(format.internalFormat == GL30.GL_RGB16F || format.internalFormat == GL30.GL_RGBA16F){
			ByteBuffer halfBuffer = BufferUtils.newByteBuffer(width * height * format.bppGpu);
			for(int i=0, n=width*height * format.numComponents ; i<n ; i++){
				float f = rgbBuffer.getFloat();
				short hf = (short)KTX2Processor.fromFloat(f);
				halfBuffer.putShort(hf);
			}
			halfBuffer.flip();
			rgbBuffer = halfBuffer;
		}
		
		KTX2Processor.export(new FileOutputStream(new File(outputFile)), 
				rgbBuffer, width, height, format.internalFormat, mipmaps, compress);
	}

	private static void printHelp() {
		System.out.println("Usage: HdrToKtx2 [options] inputFile.hdr outputFile.ktx2");
		System.out.println("Format options:");
		// TODO
	}
}
