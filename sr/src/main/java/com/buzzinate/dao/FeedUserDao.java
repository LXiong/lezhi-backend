package com.buzzinate.dao;

import java.util.List;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.model.FeedUser;
import com.buzzinate.model.FeedUser.Role;
import com.google.code.morphia.Datastore;

public class FeedUserDao extends BaseDaoDefault<FeedUser, Long> {

	public FeedUserDao(Datastore ds) {
		super(ds);
	}

	public List<FeedUser> findByRole(Role role) {
		return createQuery().filter("role", role).asList();
	}
}
