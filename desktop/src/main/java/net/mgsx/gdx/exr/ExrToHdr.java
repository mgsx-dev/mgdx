package net.mgsx.gdx.exr;

import java.io.IOException;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class ExrToHdr {
	public static void main(String[] args) {
		
		if(args.length < 2){
			printHelp();
			System.exit(1);
			return;
		}
		
		String inputFile = args[0];
		String outputFile = args[1];
		
		try {
			convert(inputFile, outputFile);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
		
	}

	public static void convert(String inputFile, String outputFile) throws IOException {
		EXRLoader loader = new EXRLoader();
		loader.load(inputFile);
		// TODO missing HDRSave...
	}

	private static void printHelp() {
		System.out.println("Usage: ExrToHdr inputExrFile outputHdrPath");
	}
}
