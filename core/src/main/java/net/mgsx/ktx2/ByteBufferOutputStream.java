package net.mgsx.ktx2;

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ByteBufferOutputStream extends FilterOutputStream implements DataOutput {

	private static final int SIZE = 4096;
	private byte[] tmp = new byte[SIZE];
	private ByteBuffer buffer = BufferUtils.newByteBuffer(SIZE);
	
	public ByteBufferOutputStream(OutputStream out) {
		super(out);
	}
	
	@Override
	public void flush() throws IOException {
		flushCurrentBuffer();
		super.flush();
	}
	
	private void ensure(int n){
		if(buffer.position() + n > SIZE){
			flushCurrentBuffer();
		}
	}
	
	private void flushCurrentBuffer() {
		buffer.flip();
		buffer.get(tmp, 0, buffer.remaining());
		try {
			out.write(tmp);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
		buffer.rewind();
	}
	private void writeSafe(byte[] bytes){
		writeSafe(bytes, 0, bytes.length);
	}
	private void writeSafe(byte[] bytes, int offset, int length){
		int remaining = buffer.capacity() - buffer.position();
		if(length > remaining){
			buffer.put(bytes, offset, remaining);
			flushCurrentBuffer();
			writeSafe(bytes, offset + remaining, length - remaining);
		}else{
			buffer.put(bytes, offset, length);
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		writeSafe(b);
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		writeSafe(b, off, len);
	}
	
	@Override
	public void writeBoolean(boolean v) throws IOException {
		ensure(4);
		buffer.putInt(v ? 1 : 0);
	}

	@Override
	public void writeByte(int v) throws IOException {
		ensure(1);
		buffer.put((byte)v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		writeSafe(s.getBytes());
	}

	@Override
	public void writeChar(int v) throws IOException {
		ensure(1);
		buffer.putChar((char)v);
	}

	@Override
	public void writeChars(String s) throws IOException {
		writeSafe(s.getBytes());
	}

	@Override
	public void writeDouble(double v) throws IOException {
		ensure(8);
		buffer.putDouble(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		ensure(4);
		buffer.putFloat(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		ensure(4);
		buffer.putInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		ensure(8);
		buffer.putLong(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		ensure(2);
		buffer.putShort((short)v);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		writeSafe(s.getBytes("UTF8"));
	}

	
}
