package com.buzzinate.common.dao;

/**
 * 分页信息，用来修改MongoDB query
 * 
 * @author Brad Luo
 *
 */
public class Page {
	private long sinceId;
	private long maxId;
	private int count;
	private int page;
	
	public Page(long sinceId, long maxId, int count, int page) {
		this.sinceId = sinceId;
		this.maxId = maxId;
		this.count = count;
		this.page = page;
	}

	public long getSinceId() {
		return sinceId;
	}

	public long getMaxId() {
		return maxId;
	}

	public int getCount() {
		return count;
	}

	public int getPage() {
		return page;
	}
}
