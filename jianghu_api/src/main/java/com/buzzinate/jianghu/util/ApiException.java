package com.buzzinate.jianghu.util;

public class ApiException extends Exception {
	private static final long serialVersionUID = -3871133584299666718L;
	
	public static final int UNKNOWN = -1;
	public static final int DUPLICATE = 1;
	public static final int RECOMMEND_ERROR = 2;
	
	private int code;
	private String detail;
	
	public ApiException() {
		super();
		code = UNKNOWN;
	}
	
	public ApiException(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public ApiException(int code, Throwable t) {
		super(t);
		this.code = code;
	}
	
	public ApiException(int code, String message, Throwable t) {
		super(message, t);
		this.code = code;
		StackTraceElement[] sts = t.getStackTrace();
		if (sts.length > 0) detail = sts[0].toString();
	}
	
	public int getCode() {
		return code;
	}
	
	public String getDetail() {
		return detail;
	}

	// TODO: use json generator
	public String toString() {
		return "{ 'code':" + code + ", 'error':'" + getMessage() + ", 'detail':" + detail + "'}";  
	}
	
	public static void main(String[] args) {
		String r = new ApiException(3, "error", new RuntimeException()).toString();
		System.out.println(r);
	}
}
