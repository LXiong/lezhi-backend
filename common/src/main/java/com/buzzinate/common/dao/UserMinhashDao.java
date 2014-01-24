package com.buzzinate.common.dao;

import com.buzzinate.common.model.UserMinhash;
import com.google.code.morphia.Datastore;

public class UserMinhashDao extends BaseDaoDefault<UserMinhash, Long> implements BaseDao<UserMinhash, Long> {

	public UserMinhashDao(Datastore ds) {
		super(ds);
	}
}