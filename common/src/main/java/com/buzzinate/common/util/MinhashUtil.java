package com.buzzinate.common.util;

import org.apache.mahout.math.MurmurHash3;

public class MinhashUtil {
	public static int hash(long value) {
		byte[] bs = long2bytes(value);
		return MurmurHash3.murmurhash3_x86_32(bs, 0, bs.length, 0x3c074a61) & 0x7FFFFFFF;
	}
	
	public static byte[] long2bytes(long a) {
		byte[] result = new byte[8];

		result[0] = (byte) (a >> 56 & 0xff);
		result[1] = (byte) (a >> 48 & 0xff);
		result[2] = (byte) (a >> 40 & 0xff);
		result[3] = (byte) (a >> 32 & 0xff);
		result[4] = (byte) (a >> 24 & 0xff);
		result[5] = (byte) (a >> 16 & 0xff);
		result[6] = (byte) (a >> 8 & 0xff);
		result[7] = (byte) (a & 0xff);
		return result;
	}
}