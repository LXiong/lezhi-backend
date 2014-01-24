package com.buzzinate.common.dao;

import java.util.List;

import com.buzzinate.common.model.UserWeibo;
import com.google.code.morphia.Datastore;

public class UserWeiboDao extends BaseDaoDefault<UserWeibo, String> implements BaseDao<UserWeibo, String> {

	public UserWeiboDao(Datastore ds) {
		super(ds);
	}
	
	public List<UserWeibo> findLatest(long userId, int max) {
		return createQuery().field("_id").startsWith(String.valueOf(userId)).order("-time").limit(max).asList();
	}
}