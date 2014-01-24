package com.buzzinate.jianghu.model;

import java.util.ArrayList;
import java.util.List;

import com.buzzinate.jianghu.sr.TopItem;

public class Recommendation {
	public long userId; // user Id
	public List<TopItem> topItems;
	public long maxRecommendArticleTime;
	
	public Recommendation(long userId, List<TopItem> topItems, long maxRecommendArticleTime) {
		this.userId = userId;
		this.topItems = topItems;
		this.maxRecommendArticleTime = maxRecommendArticleTime;
	}
	
	public Recommendation(long userId) {
		this.userId = userId;
		this.topItems = new ArrayList<TopItem>();
		this.maxRecommendArticleTime = 0;
	}
}
