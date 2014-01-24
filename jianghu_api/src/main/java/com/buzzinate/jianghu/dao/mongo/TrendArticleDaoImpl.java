package com.buzzinate.jianghu.dao.mongo;


import java.util.ArrayList;
import java.util.List;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.model.TrendArticle;
import com.buzzinate.jianghu.dao.TrendArticleDao;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;

public class TrendArticleDaoImpl extends BaseDaoDefault<TrendArticle, Long> implements TrendArticleDao{
	
	@Inject
	public TrendArticleDaoImpl(Datastore ds) {
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

}
