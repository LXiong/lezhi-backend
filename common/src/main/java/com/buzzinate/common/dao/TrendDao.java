package com.buzzinate.common.dao;

import java.util.List;

import com.buzzinate.common.model.Trend;
import com.buzzinate.common.util.Constants;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;

public class TrendDao extends BaseDaoDefault<Trend, Long>{

	
	public TrendDao(Datastore ds) {
		super(ds);
	}
	
	public List<Trend> findPop(int count, int page) {
		Query<Trend> q = createQuery().filter("count >=", 0).order("-createAt").offset(count * (page-1)).limit(count);
		return q.asList();
	}
	
	public Trend findByHash(byte[] hash) {
		return createQuery().filter("hash", hash).get();
	}
	
	
}
