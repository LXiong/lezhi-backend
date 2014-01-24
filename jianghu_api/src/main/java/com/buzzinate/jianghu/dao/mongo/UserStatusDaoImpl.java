package com.buzzinate.jianghu.dao.mongo;

import java.util.ArrayList;
import java.util.List;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.jianghu.dao.UserStatusDao;
import com.buzzinate.jianghu.model.UserStatus;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Inject;

public class UserStatusDaoImpl extends BaseDaoDefault<UserStatus, Long> implements UserStatusDao {

	@Inject
	public UserStatusDaoImpl(Datastore ds) {
		super(ds);
	}

	@Override
	public UserStatus getOrNew(long id) {
		UserStatus userStatus = get(id);
		if (userStatus == null) {
			userStatus = new UserStatus();
			userStatus.setId(id);
		}
		return userStatus;
	}

	@Override
	public List<UserStatus> findStatus(List<Long> userIds, int count, int page) {
		if (userIds.isEmpty()) return new ArrayList<UserStatus>();
		
		Query<UserStatus> q = createQuery().filter("id in", userIds).field("lastStatusTime").exists().order("-lastStatusTime");
		if (count > -1 && page > -1) {
			q.offset(count * (page - 1));
			q.limit(count);
		}
		return q.asList();
	}
}
