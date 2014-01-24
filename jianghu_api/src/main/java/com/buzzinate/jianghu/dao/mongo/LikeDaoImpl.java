package com.buzzinate.jianghu.dao.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.Page;
import com.buzzinate.jianghu.dao.LikeDao;
import com.buzzinate.jianghu.model.Like;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Inject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class LikeDaoImpl extends BaseDaoDefault<Like, ObjectId> implements LikeDao {

	@Inject
	public LikeDaoImpl(Datastore ds) {
		super(ds);
	}

	@Override
	public void addLike(long userId, long articleId) {
		Like like = new Like();
		like.setUserId(userId);
		like.setArticleId(articleId);
		like.setCreateAt(System.currentTimeMillis());
		ds.save(like, WriteConcern.NORMAL);
	}

	@Override
	public void removeLike(long userId, long articleId) {
		Query<Like> q = createQuery().filter("userId", userId).filter("articleId", articleId);
		WriteResult r = deleteByQuery(q);
		if (r.getError() != null) throw new RuntimeException(r.getError());
	}

	@Override
	public List<Long> findLikeUserIds(long articleId) {
		List<Long> userIds = new ArrayList<Long>();
		List<Like> likes = createQuery().filter("articleId", articleId).asList();
		for (Like like: likes) userIds.add(like.getUserId());
		return userIds;
	}

	@Override
	public int countLikePerArticle(long articleId) {
		return (int) count(createQuery().filter("articleId", articleId));
	}
	
	@Override
	public int countLikePerUser(long userId) {
		return (int) count(createQuery().filter("userId", userId));
	}

	@Override
	public List<Long> findLikeIds(long userId, Page page) {
		List<Long> articleIds = new ArrayList<Long>();
		Query<Like> q = createQuery().filter("userId", userId);
		filterByPage(q, page);
		List<Like> likes = q.asList();
		for (Like like: likes) articleIds.add(like.getArticleId());
		return articleIds;
	}

	@Override
	public List<Long> findLikeIds(long userId, List<Long> possibleIds) {
		List<Long> articleIds = new ArrayList<Long>();
		if (possibleIds.isEmpty()) return articleIds;
		
		List<Like> likes = createQuery().filter("userId", userId).filter("articleId in", possibleIds).asList();
		for (Like like: likes) articleIds.add(like.getArticleId());
		return articleIds;
	}
}
