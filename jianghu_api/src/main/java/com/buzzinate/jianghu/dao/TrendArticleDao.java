package com.buzzinate.jianghu.dao;

import java.util.List;

public interface TrendArticleDao {
	public List<Long> findItemsFromTrend(Long trendId, int count, int page);
}
