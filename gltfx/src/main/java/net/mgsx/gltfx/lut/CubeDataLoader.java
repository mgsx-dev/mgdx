package net.mgsx.gltfx.lut;

import java.nio.FloatBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltfx.lut.CubeData.Type;

/**
 * Note that i didn't found official Adobe specification for this file format.
 * Few sources i found : 
 * https://helpx.adobe.com/photoshop/using/export-color-lookup-tables.html
 * https://en.wikipedia.org/wiki/3D_lookup_table
 * https://forum.blackmagicdesign.com/viewtopic.php?f=21&t=40284
 * https://github.com/thibauts/parse-cube-lut/blob/master/index.js
 * 
 * Some example files:
 * https://www.rocketstock.com/free-after-effects-templates/35-free-luts-for-color-grading-videos/
 * 
 * @author mgsx
 *
 */
public class CubeDataLoader {

	public CubeData load(FileHandle file){
		CubeData data = new CubeData();
		String content = file.readString();
		String[] lines = content.split("\n");
		FloatBuffer floatBuffer = null;
		for(String line : lines){
			String sline = line.trim();
			if(sline.length() == 0){
				// skip empty line
				continue;
			}
			char c = sline.charAt(0);
			if(c == '#'){
				// skip comments
				continue;
			}
			String[] tokens = sline.split(" ");
			if(data.buffer == null && (c >= 'A' && c <= 'Z' || c>='a' && c<='z')){
				String name = tokens[0];
				if(name.equals("TITLE")){
					data.title = tokens[1];
				}else if(name.equals("LUT_3D_SIZE")){
					data.type = Type.TYPE_3D;
					data.dimSize = Integer.parseInt(tokens[1]);
				}else if(name.equals("LUT_1D_SIZE")){
					data.type = Type.TYPE_1D;
					data.dimSize = Integer.parseInt(tokens[1]);
				}
				else if(name.equals("DOMAIN_MIN")){
					data.domainMin = new Vector3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
				}
				else if(name.equals("DOMAIN_MAX")){
					data.domainMax = new Vector3(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
				}else{
					System.err.println("CUBE file, unknown token: " + name);
				}
			}else{
				// entries
				if(data.buffer == null){
					if(data.type == null || data.dimSize == null /*|| data.domainMin == null || data.domainMax == null*/){
						throw new GdxRuntimeException("missing info");
					}
					int nbEntries = data.type == Type.TYPE_1D ? data.dimSize : data.dimSize * data.dimSize * data.dimSize;
					
					data.buffer = BufferUtils.newByteBuffer(nbEntries * 3 * Float.BYTES);
					floatBuffer = data.buffer.asFloatBuffer();
				}
				if(floatBuffer.position() + tokens.length - 1 > floatBuffer.capacity()){
					throw new GdxRuntimeException("unexpected data");
				}
				for(int i=0 ; i<tokens.length ; i++){
					float value = Float.parseFloat(tokens[i]);
					floatBuffer.put(value);
				}
			}
		}
		
		if(floatBuffer.capacity() != floatBuffer.position()){
			throw new GdxRuntimeException("incomplete data");
		}
		
		data.buffer.rewind();
		
		return data;
	}
}
