package com.buzzinate.jianghu.dao.mongo;

import java.util.List;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.Page;
import com.buzzinate.jianghu.dao.MentionDao;
import com.buzzinate.jianghu.model.Mention;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Inject;

public class MentionDaoImpl extends BaseDaoDefault<Mention, Long> implements MentionDao {

	@Inject
	public MentionDaoImpl(Datastore ds) {
		super(ds);
	}

	@Override
	public void addMention(Mention mention) {
		this.save(mention);
	}

	@Override
	public List<Mention> findByUser(long userId, Page page) {
		Query<Mention> q = createQuery().filter("userId", userId);
		filterByPage(q, page);
		return q.asList();
	}

	@Override
	public long countSince(long userId, long lastId) {
		Query<Mention> q = createQuery().filter("userId", userId).filter("id >", lastId);
		return count(q);
	}
}
