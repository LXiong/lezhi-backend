package com.buzzinate.jianghu.dao;

import java.util.List;

import com.buzzinate.common.dao.Page;

public interface LikeDao {
	void addLike(long userId, long articleId);
	void removeLike(long userId, long articleId);
	List<Long> findLikeUserIds(long articleId);
	List<Long> findLikeIds(long userId, Page page);
	int countLikePerArticle(long articleId);
	int countLikePerUser(long userId);
	List<Long> findLikeIds(long userId, List<Long> possibleIds);
}
