package com.buzzinate.common.dao;

import com.buzzinate.common.model.UserDailyStat;
import com.google.code.morphia.Datastore;

public class UserDailyStatDao extends BaseDaoDefault<UserDailyStat, Long> implements BaseDao<UserDailyStat, Long> {

	public UserDailyStatDao(Datastore ds) {
		super(ds);
	}
}