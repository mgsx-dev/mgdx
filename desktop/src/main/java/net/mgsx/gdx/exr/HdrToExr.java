package net.mgsx.gdx.exr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;

import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gdx.exr.EXRProcessor.EXRCompression;
import net.mgsx.gdx.exr.EXRProcessor.EXRType;
import net.mgsx.gdx.exr.EXRProcessor.ZFPConfiguration;
import net.mgsx.gdx.graphics.g2d.HDRILoader;

public class HdrToExr {
	public static void main(String[] args) {
		if(args.length < 2){
			printHelp();
			System.exit(1);
			return;
		}
		int argIndex;
		EXRType type = EXRType.RGB16F;
		EXRCompression compression = EXRCompression.PIZ;
		ZFPConfiguration config = new ZFPConfiguration();
		
		for(argIndex=0 ; argIndex<args.length-2 ; argIndex++){
			String arg = args[argIndex];
			if(arg.equals("--rgb32i")){
				type = EXRType.RGB32I;
			}else if(arg.equals("--rgb16f")){
				type = EXRType.RGB16F;
			}else if(arg.equals("--rgb32f")){
				type = EXRType.RGB32F;
			}else if(arg.equals("--uncompressed")){
				compression = EXRCompression.NONE;
			}else if(arg.equals("--zip")){
				compression = EXRCompression.ZIP;
			}else if(arg.equals("--piz")){
				compression = EXRCompression.PIZ;
			}else if(arg.equals("--rle")){
				compression = EXRCompression.RLE;
			}else if(arg.equals("--zips")){
				compression = EXRCompression.ZIPS;
			}else if(arg.startsWith("--zfp-fixed")){
				compression = EXRCompression.ZFP;
				config.compressionRate = Double.parseDouble(arg.split("=")[1]);
			}else if(arg.startsWith("--zfp-precision")){
				compression = EXRCompression.ZFP;
				config.compressionPrecision = Integer.parseInt(arg.split("=")[1]);
			}else if(arg.startsWith("--zfp-tolerance")){
				compression = EXRCompression.ZFP;
				config.compressionTolerance = Double.parseDouble(arg.split("=")[1]);
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
			convert(inputFile, outputFile, type, compression, config);
			System.out.println("complete: " + outputFile);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static void convert(String inputFile, String outputFile, EXRType type, EXRCompression compression, ZFPConfiguration config) throws IOException {
		HDRILoader importer = new HDRILoader();
		importer.load(new FileInputStream(new File(inputFile)));
		FloatBuffer rgbBuffer = importer.createRGBBuffer().asFloatBuffer();
		
		EXRProcessor exporter = new EXRProcessor();
		exporter.save(outputFile, importer.getWidth(), importer.getHeight(), rgbBuffer, type, compression, config);
	}

	private static void printHelp() {
		System.out.println("Usage: HdrToExr [options] inputFile.hdr outputFile.exr");
		System.out.println("Format options:");
		System.out.println("--rgb32f export as 32 bits float");
		System.out.println("--rgb16f export as 16 bits half float (default)");
		System.out.println("--rgb32i export as 32 bits integer");
		System.out.println("Compression options:");
		System.out.println("--uncompressed");
		System.out.println("--piz (default)");
		System.out.println("--zip");
		System.out.println("--rle");
		System.out.println("--zips");
		System.out.println("--zfp-fixed=value enable ZFP with fixed rate based compression (double)");
		System.out.println("--zfp-precision=value enable ZFP with precision based compression (integer)");
		System.out.println("--zfp-tolerance enable ZFP with tolerance based compression (double)");
	}
}
