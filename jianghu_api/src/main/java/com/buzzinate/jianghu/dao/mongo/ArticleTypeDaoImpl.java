package com.buzzinate.jianghu.dao.mongo;

import java.util.List;

import org.bson.types.ObjectId;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.model.ArticleType;
import com.buzzinate.jianghu.dao.ArticleTypeDao;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;
import com.google.code.morphia.query.Query;

public class ArticleTypeDaoImpl extends BaseDaoDefault<ArticleType, ObjectId>
		implements ArticleTypeDao {

	@Inject
	public ArticleTypeDaoImpl(Datastore ds) {
		super(ds);
	}

	public List<ArticleType> findPop(int count, int page) {
		Query<ArticleType> q = createQuery().offset(count * (page - 1)).limit(
				count);
		return q.asList();
	}
}
