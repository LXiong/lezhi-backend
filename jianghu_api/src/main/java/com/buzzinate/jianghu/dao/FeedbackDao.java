package com.buzzinate.jianghu.dao;

import org.bson.types.ObjectId;

import com.buzzinate.common.dao.BaseDao;
import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.jianghu.model.Feedback;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;

public class FeedbackDao extends BaseDaoDefault<Feedback, ObjectId> implements BaseDao<Feedback, ObjectId> {

	@Inject
	public FeedbackDao(Datastore ds) {
		super(ds);
	}
}