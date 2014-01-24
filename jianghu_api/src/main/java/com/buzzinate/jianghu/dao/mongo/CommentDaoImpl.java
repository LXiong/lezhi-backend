package com.buzzinate.jianghu.dao.mongo;

import java.util.List;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.Page;
import com.buzzinate.jianghu.dao.CommentDao;
import com.buzzinate.jianghu.model.Comment;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Inject;

public class CommentDaoImpl extends BaseDaoDefault<Comment, Long> implements CommentDao {

	@Inject
	public CommentDaoImpl(Datastore ds) {
		super(ds);
	}

	@Override
	public List<Comment> getComments(Long sourceId, Page page) {
		Query<Comment> q = createQuery().filter("sourceId", sourceId);
		filterByPage(q, page);
		return q.asList();
	}

	@Override
	public int countComment(long articleId) {
		return (int) count(createQuery().filter("sourceId", articleId));
	}
}
