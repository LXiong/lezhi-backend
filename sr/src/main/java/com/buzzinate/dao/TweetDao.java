package com.buzzinate.dao;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.model.Tweet;
import com.google.code.morphia.Datastore;

public class TweetDao extends BaseDaoDefault<Tweet, Long> {

	public TweetDao(Datastore ds) {
		super(ds);
	}
	
	public void save(Iterable<Tweet> entities) {
		ds.save(entities);
	}
}
