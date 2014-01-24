package com.buzzinate.jianghu.dao.mongo;

import java.util.List;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.Page;
import com.buzzinate.jianghu.dao.StatusDao;
import com.buzzinate.jianghu.model.Status;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Inject;

public class StatusDaoImpl extends BaseDaoDefault<Status, Long> implements StatusDao {

	@Inject
	public StatusDaoImpl(Datastore ds) {
		super(ds);
	}
	
	public List<Status> findByUserId(long userId, Page page) {
		Query<Status> q = createQuery().filter("userId", userId);
		filterByPage(q, page);
		return q.asList();
	}

	@Override
	public long countSince(List<Long> userIds, long lastId) {
		if (userIds.isEmpty()) return 0;
		
		Query<Status> q = createQuery().filter("userId in", userIds).filter("id >", lastId);
		return distinct(q, "userId").size();
	}
}
