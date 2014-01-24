package com.buzzinate.crawl.queues;

import java.util.LinkedList;
import java.util.List;

/**
 * 抓取队列，一般一个抓取队列对应一个host，并且保证一个队列不会有多个线程同时抓取，比较容易限速，比较不会被封
 * 
 * @author Brad Luo
 *
 */
public class WorkQueue<T> implements Comparable<WorkQueue<T>> {	
	private LinkedList<CrawlTask<T>> q = new LinkedList<CrawlTask<T>>();
	private int qid;
	private long lastCrawlTime = 0;
	private boolean inUsed = false;
	
	public WorkQueue(int qid) {
		this.qid = qid;
	}

	public CrawlTask<T> poll() {
		inUsed = false;
		lastCrawlTime = System.currentTimeMillis();
		return q.poll();
	}
	
	public CrawlTask<T> peek() {
		CrawlTask<T> task = q.peek();
		if (task != null) inUsed = true;
		return task;
	}

	public boolean offer(CrawlTask<T> e) {
		return q.offer(e);
	}
	
	public long getLastCrawlTime() {
		return lastCrawlTime;
	}
	
	public void setLastCrawlTime(long lastCrawlTime) {
		this.lastCrawlTime = lastCrawlTime;
	}
	
	public int getQid() {
		return qid;
	}
	
	public boolean isUsed() {
		return inUsed;
	}
	
	public int size() {
		return q.size();
	}
	
	public List<CrawlTask<T>> getTasks() {
		return q;
	}

	public boolean isEmpty() {
		return q.isEmpty();
	}

	@Override
	public int compareTo(WorkQueue<T> o) {
		if (getLastCrawlTime() < o.getLastCrawlTime()) return -1;
		else if (getLastCrawlTime() == o.getLastCrawlTime()) return 0;
		else return 1;
	}
}