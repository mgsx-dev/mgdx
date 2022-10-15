package net.mgsx.gltfx.ktx2;

import java.io.DataInput;
import java.io.IOException;

import com.badlogic.gdx.utils.Array;

public class KHRDataFormat 
{
	final static int KHR_DF_SHIFT_VENDORID = 0;
	final static int KHR_DF_MASK_VENDORID = 0x1FFFF;
	final static int KHR_DF_VENDORID_KHRONOS = 0;

	final static int KHR_DF_SHIFT_DESCRIPTORTYPE = 17;
	final static int KHR_DF_MASK_DESCRIPTORTYPE = 0x7FFF;
	final static int KHR_DF_KHR_DESCRIPTORTYPE_BASICFORMAT = 0;
	
	final static int KHR_DF_SHIFT_DESCRIPTORBLOCKSIZE = 16;
	final static int KHR_DF_MASK_DESCRIPTORBLOCKSIZE = 0xFFFF;
	
	final static int KHR_DF_SHIFT_VERSIONNUMBER = 0;
	final static int KHR_DF_MASK_VERSIONNUMBER = 0xffff;
	
	final static int KHR_DF_SHIFT_MODEL = 0;
	final static int KHR_DF_MASK_MODEL = 0xff;
	final static int KHR_DF_MODEL_RGBSDA = 1;
	
	final static int KHR_DF_SHIFT_PRIMARIES = 8;
	final static int KHR_DF_MASK_PRIMARIES = 0xFF;
	final static int KHR_DF_PRIMARIES_BT709 = 1;
	final static int KHR_DF_PRIMARIES_SRGB = KHR_DF_PRIMARIES_BT709;
	
	
	final static int KHR_DF_SHIFT_TRANSFER = 16;
	final static int KHR_DF_MASK_TRANSFER = 0xFF;
	final static int KHR_DF_TRANSFER_LINEAR = 1;
	final static int KHR_DF_TRANSFER_SRGB = 2;
	
	final static int KHR_DF_SHIFT_FLAGS = 24;
	final static int KHR_DF_MASK_FLAGS = 0xFF;
	final static int KHR_DF_FLAG_ALPHA_PREMULTIPLIED = 1;
	final static int KHR_DF_FLAG_ALPHA_STRAIGHT = 0;
	
	
	static class SampleInfo {
		final static int KHR_DF_SAMPLE_DATATYPE_FLOAT = 0x80;
		final static int KHR_DF_SAMPLE_DATATYPE_SIGNED = 0x40;
		final static int KHR_DF_SAMPLE_DATATYPE_EXPONENT = 0x20;
		final static int KHR_DF_SAMPLE_DATATYPE_LINEAR = 0x10;
		
		// note: SD stands for Stencil and Depth
		final static int KHR_DF_CHANNEL_RGBSDA_RED = 1;
		final static int KHR_DF_CHANNEL_RGBSDA_GREEN = 2;
		final static int KHR_DF_CHANNEL_RGBSDA_BLUE = 4;
		final static int KHR_DF_CHANNEL_RGBSDA_STENCIL = 1 << 13;
		final static int KHR_DF_CHANNEL_RGBSDA_DEPTH = 1 << 14;
		final static int KHR_DF_CHANNEL_RGBSDA_ALPHA = 1 << 15;
		
		int dataType;
		int channelType;
		int bitLength;
		int bitOffset;
		int [] samplePosition = new int[4];
		int sampleLower = 0;
		int sampleUpper = 0xFFFFFFFF;
	}
	
	int vendorId = KHR_DF_VENDORID_KHRONOS;
	int descriptorType = KHR_DF_KHR_DESCRIPTORTYPE_BASICFORMAT;
	int versionNumber = 2;
	int colorModel = KHR_DF_MODEL_RGBSDA;
	int colorPrimaries = KHR_DF_PRIMARIES_SRGB;
	int transferFunction;
	int flags = KHR_DF_FLAG_ALPHA_STRAIGHT;
	int [] texelBlockDimension = new int[4];
	int [] bytesPlane = new int[8];
	
	final Array<SampleInfo> samplesInfo = new Array<SampleInfo>();
	
	public int[] encode(){
		// https://www.khronos.org/registry/DataFormat/specs/1.3/dataformat.1.3.html#_anchor_id_basicdescriptor_xreflabel_basicdescriptor_khronos_basic_data_format_descriptor_block
		int nbInts = 6 + samplesInfo.size * 4;
		int descriptorBlockSize = nbInts * 4;
		int [] f = new int[nbInts];
		f[0] = descriptorType << KHR_DF_SHIFT_DESCRIPTORTYPE | vendorId << KHR_DF_SHIFT_VENDORID;
		f[1] = descriptorBlockSize << KHR_DF_SHIFT_DESCRIPTORBLOCKSIZE | versionNumber << KHR_DF_SHIFT_VERSIONNUMBER;
		f[2] = flags << KHR_DF_SHIFT_FLAGS | transferFunction << KHR_DF_SHIFT_TRANSFER | colorPrimaries << KHR_DF_SHIFT_PRIMARIES | colorModel << KHR_DF_SHIFT_MODEL;
		f[3] = texelBlockDimension[3] << 24 | texelBlockDimension[2] << 16 | texelBlockDimension[1] << 8 | texelBlockDimension[0];
		f[4] = bytesPlane[3] << 24 | bytesPlane[2] << 16 | bytesPlane[1] << 8 | bytesPlane[0];
		f[5] = bytesPlane[7] << 24 | bytesPlane[6] << 16 | bytesPlane[5] << 8 | bytesPlane[4];
		for(int i=0, j=6 ; i<samplesInfo.size ; i++){
			SampleInfo s = samplesInfo.get(i);
			f[j++] = (s.dataType | s.channelType) << 24 | s.bitLength << 16 | s.bitOffset;
			f[j++] = s.samplePosition[3] << 24 | s.samplePosition[2] << 16 | s.samplePosition[1] << 8 | s.samplePosition[0];
			f[j++] = s.sampleLower;
			f[j++] = s.sampleUpper;
		}
		return f;
	}
	
	public long decode(DataInput in) throws IOException{
		int f0 = in.readInt();
		int f1 = in.readInt() & 0xFFFFFFFF;
		int f2 = in.readInt() & 0xFFFFFFFF;
		int f3 = in.readInt() & 0xFFFFFFFF;
		int f4 = in.readInt() & 0xFFFFFFFF;
		int f5 = in.readInt() & 0xFFFFFFFF;
		long read = 6 * 4;
		
		String.format("%h", f0);
		
		descriptorType = (f0 >> KHR_DF_SHIFT_DESCRIPTORTYPE) & KHR_DF_MASK_DESCRIPTORTYPE;
		vendorId = (f0 >> KHR_DF_SHIFT_VENDORID) & KHR_DF_MASK_VENDORID;
		
		int descriptorBlockSize = (f1 >> KHR_DF_SHIFT_DESCRIPTORBLOCKSIZE) & KHR_DF_MASK_DESCRIPTORBLOCKSIZE;
		versionNumber = (f1 >> KHR_DF_SHIFT_VERSIONNUMBER) & KHR_DF_MASK_VERSIONNUMBER;
		
		flags = (f2 >> KHR_DF_SHIFT_FLAGS) & KHR_DF_MASK_FLAGS;
		transferFunction = (f2 >> KHR_DF_SHIFT_TRANSFER) & KHR_DF_MASK_TRANSFER;
		colorPrimaries = (f2 >> KHR_DF_SHIFT_PRIMARIES) & KHR_DF_MASK_PRIMARIES;
		colorModel = (f2 >> KHR_DF_SHIFT_MODEL) & KHR_DF_MASK_MODEL;
		
		texelBlockDimension[0] = f3 & 0xff;
		texelBlockDimension[1] = (f3 >> 8) & 0xff;
		texelBlockDimension[2] = (f3 >> 16) & 0xff;
		texelBlockDimension[3] = (f3 >> 24) & 0xff;
	
		bytesPlane[0] = (f4 >> 0) & 0xff;
		bytesPlane[1] = (f4 >> 8) & 0xff;
		bytesPlane[2] = (f4 >> 16) & 0xff;
		bytesPlane[3] = (f4 >> 24) & 0xff;

		bytesPlane[4] = (f5 >> 0) & 0xff;
		bytesPlane[5] = (f5 >> 8) & 0xff;
		bytesPlane[6] = (f5 >> 16) & 0xff;
		bytesPlane[7] = (f5 >> 24) & 0xff;
		
		while(read < descriptorBlockSize){
			
			SampleInfo s = new SampleInfo();
			
			int s1 = in.readInt();
			int s2 = in.readInt();
			int s3 = in.readInt();
			int s4 = in.readInt();
			read += 4 * 4;
			
			s.dataType = (s1 >> 24) & 0xf0;
			s.channelType = (s1 >> 24) & 0xf;
			s.bitLength = (s1 >> 16) & 0xff;
			s.bitOffset = (s1 >> 0) & 0xffff;
			
			s.samplePosition[0] = (s2 >> 0) & 0xff;
			s.samplePosition[1] = (s2 >> 8) & 0xff;
			s.samplePosition[2] = (s2 >> 16) & 0xff;
			s.samplePosition[3] = (s2 >> 24) & 0xff;
			
			s.sampleLower = s3;
			s.sampleUpper = s4;
			
			samplesInfo.add(s);
		}
		
		return read;
	}
}
