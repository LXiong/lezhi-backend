package org.ictclas4j.run;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

import com.gftech.util.GFNet;

public class LoadDict {
	public static final int SIZE_INT = 4;
	public static final int SIZE_DOUBLE = 8;

	public static void main(String[] args) throws Exception {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream("D:/tmp/pangu.dct")));
		byte[] v = GFNet.readBytes(in, 32);
		System.out.println(new String(v));
		while (in.available() > 0) {
			int len = GFNet.readInt32(in);
			byte[] buff = GFNet.readBytes(in, len - SIZE_INT - SIZE_DOUBLE);
			String word = new String(buff);
			int pos = GFNet.readInt32(in);
			long lbs = GFNet.readLong(in);
//			buff = GFNet.readBytes(in, SIZE_DOUBLE);
			double freq = Double.longBitsToDouble(lbs);
			System.out.println(word + " => " + pos + ", " + freq);
		}
		in.close();
	}
}