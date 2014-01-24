package com.buzzinate.crawl.core.fetch;

import org.jsoup.nodes.Document;

public class Response {
	private int statusCode;
	private String realUrl;
	private String contentType;
	private long lastMod;
	private String charset;
	private Document doc;
	
	public Response(int statusCode, String realUrl, String contentType, long lastMod, String charset, Document doc) {
		this.statusCode = statusCode;
		this.realUrl = realUrl;
		this.contentType = contentType;
		this.lastMod = lastMod;
		this.charset = charset;
		this.doc = doc;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getRealUrl() {
		return realUrl;
	}

	public String getContentType() {
		return contentType;
	}

	public long getLastMod() {
		return lastMod;
	}

	public String getCharset() {
		return charset;
	}

	public Document getDoc() {
		return doc;
	}
}
