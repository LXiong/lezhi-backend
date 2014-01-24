package com.buzzinate.dao;

import java.util.List;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.model.RawArticle;
import com.google.code.morphia.Datastore;

public class RawArticleDao extends BaseDaoDefault<RawArticle, String> {

	public RawArticleDao(Datastore ds) {
		super(RawArticle.class, ds);
	}

	public List<String> findBySite(String site) {
		String reverseSite = RawArticle.reverse(site);
		return getIds(createQuery().field("_id").startsWith(reverseSite));
	}
}