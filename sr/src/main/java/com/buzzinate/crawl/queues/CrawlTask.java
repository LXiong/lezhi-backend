package com.buzzinate.crawl.queues;

public class CrawlTask<T> {
	private static final int MAX_RETRY = 3;
	
	private int qid;
	private String url;
	private T info;
	private int errorTimes = 0;
	
	public CrawlTask(int qid, String url, T info) {
		this.qid = qid;
		this.url = url;
		this.info = info;
	}
	
	public int getQid() {
		return qid;
	}
	
	public String getUrl() {
		return url;
	}
	
	public T getInfo() {
		return info;
	}
	
	public CrawlTask<T> retry() {
		if (errorTimes >= MAX_RETRY) return null;
		CrawlTask<T> task = new CrawlTask<T>(qid, url, info);
		task.errorTimes = errorTimes + 1;
		return task;
	}
}