package org.buzzinate.lezhi.api;

public class UrlTime {
	public String url;
	public long lastModified;
	
	public UrlTime() {}
	
	public UrlTime(String url, long lastModified) {
		this.url = url;
		this.lastModified = lastModified;
	}
}