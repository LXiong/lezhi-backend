package com.buzzinate.jianghu.dao.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.jianghu.dao.ReadDao;
import com.buzzinate.jianghu.model.Read;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Inject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class ReadDaoImpl extends BaseDaoDefault<Read, ObjectId> implements ReadDao {

	@Inject
	public ReadDaoImpl(Datastore ds) {
		super(ds);
	}

	@Override
	public void addRead(long userId, long articleId) {
		Read read = new Read();
		read.setUserId(userId);
		read.setArticleId(articleId);
		read.setCreateAt(System.currentTimeMillis());
		ds.save(read, WriteConcern.NORMAL);
	}

	@Override
	public void removeRead(long userId, long articleId) {
		Query<Read> q = createQuery().filter("userId", userId).filter("articleId", articleId);
		WriteResult r = deleteByQuery(q);
		if (r.getError() != null) throw new RuntimeException(r.getError());
	}

	@Override
	public List<Long> findReadIds(long userId, long minArticleId) {
		List<Long> articleIds = new ArrayList<Long>();
		List<Read> reads = createQuery().filter("userId", userId).filter("articleId >", minArticleId-1).asList();
		for (Read read: reads) articleIds.add(read.getArticleId());
		return articleIds;
	}

	@Override
	public List<Long> findReadIds(long userId, List<Long> possibleIds) {
		List<Long> articleIds = new ArrayList<Long>();
		if (possibleIds.isEmpty()) return articleIds;
		
		List<Read> reads = createQuery().filter("userId", userId).filter("articleId in", possibleIds).asList();
		for (Read read: reads) articleIds.add(read.getArticleId());
		return articleIds;
	}

	@Override
	public List<Long> findLatestRead(long userId, int max) {
		List<Long> articleIds = new ArrayList<Long>();
		List<Read> reads = createQuery().filter("userId", userId).order("-articleId").limit(max).asList();
		for (Read read: reads) articleIds.add(read.getArticleId());
		return articleIds;
	}
}