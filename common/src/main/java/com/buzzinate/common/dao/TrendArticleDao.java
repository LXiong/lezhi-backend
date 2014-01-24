package com.buzzinate.common.dao;

import java.util.ArrayList;
import java.util.List;

import com.buzzinate.common.model.TrendArticle;
import com.google.code.morphia.Datastore;

public class TrendArticleDao extends BaseDaoDefault<TrendArticle, Long>{
	
	public TrendArticleDao(Datastore ds) {
		super(ds);
	}
	
	public List<Long> findItemsFromTrend(Long trendId, int count, int page){
		List<TrendArticle> trendArticles = createQuery().filter("trendId",trendId).offset(count * (page-1)).limit(count).asList();
		List<Long> pageIds = new ArrayList<Long>();
		for (TrendArticle trendArticle: trendArticles) {
			pageIds.add(trendArticle.getPageId());
		}
		return pageIds;
	}
	
	public Boolean isExistInTrend(Long pageId){
		List<TrendArticle> trendArticles = createQuery().filter("pageId", pageId).asList();
		return trendArticles.size() > 0 ? true : false;
	}
	
	
}
