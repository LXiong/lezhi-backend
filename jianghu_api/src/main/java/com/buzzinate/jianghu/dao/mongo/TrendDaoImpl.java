package com.buzzinate.jianghu.dao.mongo;

import java.util.List;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.model.Trend;
import com.buzzinate.jianghu.dao.TrendDao;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Inject;

public class TrendDaoImpl extends BaseDaoDefault<Trend, Long> implements TrendDao{
	
	@Inject
	public TrendDaoImpl(Datastore ds) {
		super(ds);
	}
	
	public List<Trend> findPop(int count, int page) {
		Query<Trend> q = createQuery().filter("count >", 0).order("-createAt").offset(count * (page-1)).limit(count);
		return q.asList();
	}
	
	public Trend findByHash(byte[] hash) {
		return createQuery().filter("hash", hash).get();
	}

}
