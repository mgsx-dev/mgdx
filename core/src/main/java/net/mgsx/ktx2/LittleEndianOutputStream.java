package net.mgsx.ktx2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LittleEndianOutputStream extends FilterOutputStream implements DataOutput
{
	OutputStream os;
	
	public LittleEndianOutputStream(OutputStream out) {
		super(out);
		this.os = out;
	}

	@Override
	public void write (int b) throws IOException {
		os.write(b);
	}

	public void writeBoolean (boolean v) throws IOException {
		os.write(v ? 1 : 0);
	}

	public void writeByte (int v) throws IOException {
		os.write(v);
	}

	public void writeBytes (String s) throws IOException {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			os.write(s.charAt(i) & 0xff);
		}
	}

	public void writeChar (int v) throws IOException {
		os.write(v >> 8);
		os.write(v);
	}

	public void writeChars (String s) throws IOException {
		throw new RuntimeException("writeChars NYI");
	}

	public void writeDouble (double v) throws IOException {
		writeLong(Double.doubleToLongBits(v));
	}

	public void writeFloat (float v) throws IOException {
		writeInt(Float.floatToIntBits(v));
	}

	public void writeInt (int v) throws IOException {
		os.write(v);
		os.write(v >> 8);
		os.write(v >> 16);
		os.write(v >> 24);
	}

	public void writeLong (long v) throws IOException {
		writeInt((int)v);
		writeInt((int)(v >> 32L));
	}

	public void writeShort (int v) throws IOException {
		os.write(v);
		os.write(v >> 8);
	}

	public void writeUTF (String s) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c > 0 && c < 80) {
				baos.write(c);
			} else if (c < '\u0800') {
				baos.write(0xc0 | (0x1f & (c >> 6)));
				baos.write(0x80 | (0x3f & c));
			} else {
				baos.write(0xe0 | (0x0f & (c >> 12)));
				baos.write(0x80 | (0x3f & (c >> 6)));
				baos.write(0x80 | (0x3f & c));
			}
		}
		writeShort(baos.size());
		os.write(baos.toByteArray(), 0, baos.size());
	}
}
