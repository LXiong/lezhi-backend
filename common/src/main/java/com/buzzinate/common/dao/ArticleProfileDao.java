package com.buzzinate.common.dao;

import java.util.List;

import com.buzzinate.common.model.ArticleProfile;
import com.buzzinate.common.model.Keyword;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class ArticleProfileDao extends BaseDaoDefault<ArticleProfile, Long> implements BaseDao<ArticleProfile, Long> {

	public ArticleProfileDao(Datastore ds) {
		super(ds);
	}
	
	public List<ArticleProfile> findByMinHash(int minhash) {
		return createQuery().filter("minhash", minhash).asList();
	}
	
	public void updateMinhash(long id, String title, List<Integer> minhashes, long createAt) {
		if (minhashes.size() > 0) {
			int minhash = minhashes.get(0);
			Query<ArticleProfile> q = createQuery().filter("id", id);
			UpdateOperations<ArticleProfile> uop = createUpdateOperations().set("title", title).set("minhash", minhash).set("minhashes", minhashes).set("createAt", createAt);
			ds.updateFirst(q, uop, true);
		}
	}

	public void updateKeywords(long id, List<Keyword> keywords, long createAt) {
		Query<ArticleProfile> q = createQuery().filter("id", id);
		UpdateOperations<ArticleProfile> uop = createUpdateOperations().set("keywords", keywords).set("createAt", createAt);
		ds.updateFirst(q, uop, true);
	}

	public List<ArticleProfile> findByKeywords(List<String> missKeywords, long since) {
		return createQuery().filter("keywords.word in", missKeywords).filter("createAt >", since).asList();
	}

	public List<ArticleProfile> findSince(long since) {
		return createQuery().filter("createAt >", since).asList();
	}
}