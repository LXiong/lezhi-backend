package com.gftech.util;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 和网络相关的操作
 * 
 * @author sinboy
 * @version 1.0 2005-9-27
 * 
 */
final public class GFNet {
	
	public static final short readUInt8(InputStream in) throws IOException {
		int ch = in.read();
		if (ch < 0) {
			throw new EOFException();
		}
		return (short) (ch & 0xff);
	}

	public static final int readUInt16(InputStream in) throws IOException {
		return (readUInt8(in) + (readUInt8(in) << 8)) & 0xffff;
	}

	public static final int readInt32(InputStream in) throws IOException {
		return readInt32(in, false);
	}

	public static final int readInt32(InputStream in, boolean isHighFirst) throws IOException {
		if (isHighFirst)
			return (readUInt8(in) << 24) + (readUInt8(in) << 16) + (readUInt8(in) << 8) + readUInt8(in);
		else
			return readUInt8(in) + (readUInt8(in) << 8) + (readUInt8(in) << 16) + (readUInt8(in) << 24);
	}
	
	public static final long readLong(InputStream in) throws IOException {
		long r = 0;
		for (int i = 0; i < 64; i += 8) {
			long t = readUInt8(in);
			r += t << i;
		}
		return r;
	}

	public static byte[] readBytes(DataInputStream in, int len) throws IOException {
		if (in != null && len > 0) {
			byte[] b = new byte[len];

			for (int i = 0; i < len; i++)
				b[i] = in.readByte();

			return b;
		}

		return null;
	}
	public static final void writeInt8(OutputStream out, int value) throws IOException { 
		  out.write(value);
	}
	
	
	public static final boolean writeInt32(OutputStream out, int value) throws IOException {

		return writeInt32(out, value, false);
	}

	public static final boolean writeInt32(OutputStream out, int value, boolean isHighFirst) throws IOException {
		boolean result = false;

		if (out != null) {
			byte[] b = GFCommon.int2bytes(value, isHighFirst);
			out.write(b);
		}
		return result;
	}

	public static int readInt(DataInputStream in, boolean isBig) throws IOException {
		if (in != null) {
			return in.readInt();
		} else
			throw new IOException();

	}

}
