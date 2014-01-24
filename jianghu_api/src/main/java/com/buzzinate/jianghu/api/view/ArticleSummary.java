package com.buzzinate.jianghu.api.view;

public class ArticleSummary {
	public long id;
	public String title;
	public String summary;
	public String url;
	public String feedback;
	
	public ArticleSummary() {
		
	}
	
	public ArticleSummary(long id, String title, String summary, String url, String feedback) {
		this.id = id;
		this.title = title;
		this.summary = summary;
		this.url = url;
		this.feedback = feedback;
	}
}