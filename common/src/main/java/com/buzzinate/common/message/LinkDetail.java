package com.buzzinate.common.message;

import com.buzzinate.common.geo.GeoInfo;

public class LinkDetail {
	public long userId;
	public long retweetSize;
	public long commentSize;
	public long tweetId;
	public long tweetTime;
	
	public Long pageId;
	public Boolean isTrendMsg = false;
	public String trendName;
	
	//public GeoInfo geoInfo;
	
	public LinkDetail() {}
	
	public LinkDetail(long userId, long tweetId, long tweetTime, long retweetSize, long commentSize) {
		this.userId = userId;
		this.tweetId = tweetId;
		this.tweetTime = tweetTime;
		this.retweetSize = retweetSize;
		this.commentSize = commentSize;
	}
	
	public LinkDetail(long userId, long tweetId, long tweetTime, long retweetSize, long commentSize, Boolean isTrendMsg, String trendName) {
		this.userId = userId;
		this.tweetId = tweetId;
		this.tweetTime = tweetTime;
		this.retweetSize = retweetSize;
		this.commentSize = commentSize;
		this.isTrendMsg = isTrendMsg;
		this.trendName = trendName;
		//this.geoInfo = geoInfo;
	}
}