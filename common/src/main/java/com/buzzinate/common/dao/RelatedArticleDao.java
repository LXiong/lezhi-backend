package com.buzzinate.common.dao;

import com.buzzinate.common.model.RelatedArticle;
import com.google.code.morphia.Datastore;

public class RelatedArticleDao extends BaseDaoDefault<RelatedArticle, Long> implements BaseDao<RelatedArticle, Long> {

	public RelatedArticleDao(Datastore ds) {
		super(ds);
	}
}