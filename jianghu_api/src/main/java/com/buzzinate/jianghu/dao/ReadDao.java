package com.buzzinate.jianghu.dao;

import java.util.List;

public interface ReadDao {
	void addRead(long userId, long articleId);
	void removeRead(long userId, long articleId);
	List<Long> findReadIds(long userId, long minArticleId);
	List<Long> findReadIds(long userId, List<Long> possibleIds);
	List<Long> findLatestRead(long userId, int max);
}