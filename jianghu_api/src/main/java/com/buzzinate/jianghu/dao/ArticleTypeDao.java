package com.buzzinate.jianghu.dao;

import java.util.List;

import com.buzzinate.common.model.ArticleType;

public interface ArticleTypeDao {
	public List<ArticleType> findPop(int count, int page);
	
}
