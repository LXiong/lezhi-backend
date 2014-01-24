package com.buzzinate.common.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtil {
	private StringUtil() {}
	
	/**
	 * Hash the given string and return a byte array
	 * @param s
	 * @return
	 */
	public static byte[] hash(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(s.getBytes("UTF-8"));
	    } catch (NoSuchAlgorithmException e) {
	    	return new byte[0];	// Won't happen
		} catch (UnsupportedEncodingException e) {
			return new byte[0];	// Won't happen
		}
	}
}
