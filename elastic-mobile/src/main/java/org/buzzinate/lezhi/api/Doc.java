package org.buzzinate.lezhi.api;


public class Doc {
	public long id;
	public String url;
	public String title;
	public String signature;
	public String thumbnail;
	public String keyword;
	public long lastModified;
	
	public Doc() {}
	
	public Doc(long id, String url, String title, String signature, String thumbnail, String keyword, long lastModified) {
		this.id = id;
		this.url = url;
		this.title = title;
		this.signature = signature;
		this.thumbnail = thumbnail;
		this.keyword = keyword;
		this.lastModified = lastModified;
	}
}