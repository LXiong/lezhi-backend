package com.buzzinate.common.dao;

import com.buzzinate.common.model.ArticleType;
import com.google.code.morphia.Datastore;

public class ArticleTypeDao extends BaseDaoDefault<ArticleType, Integer> {
	
	public ArticleTypeDao(Datastore ds) {
		super(ds);
	}
	
	public ArticleType findByID(Integer id) {
		return createPrimaryQuery().filter("id", id).get();
	}
}
