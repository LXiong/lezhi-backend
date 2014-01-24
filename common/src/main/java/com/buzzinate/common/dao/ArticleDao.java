package com.buzzinate.common.dao;

import java.util.Collection;
import java.util.List;

import com.buzzinate.common.dao.BaseDao;
import com.buzzinate.common.dao.Page;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;

public interface ArticleDao extends BaseDao<Article, Long> {
	List<Article> findPop(int count, int page);
	List<Article> findPop(Category category, int count, int page);
	List<Article> top(int offset, int length);
	List<Article> top(Category category, int offset, int length);
	Article findByHash(byte[] hash);
	void incPrefSize(long pageId);
	
	List<Article> get(List<Long> articleIds, Page page);
	List<Long> filterByStatus(Collection<Long> ids);
	void updateCategory(long id, Category cat);
	
	public List<Article> findLatest(long sinceId, String... fields);
}