package com.buzzinate.jianghu.dao.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.jianghu.dao.FollowDao;
import com.buzzinate.jianghu.model.Follow;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Inject;
import com.mongodb.WriteResult;

public class FollowDaoImpl extends BaseDaoDefault<Follow, ObjectId> implements FollowDao {

	@Inject
	public FollowDaoImpl(Datastore ds) {
		super(ds);
	}

	@Override
	public void addFollowing(long userId, long followingId) {
		Follow follow = new Follow();
		follow.setUserId(userId);
		follow.setFollowingId(followingId);
		follow.setCreateAt(System.currentTimeMillis());
		ds.save(follow);
	}

	@Override
	public void removeFollowing(long userId, long followingId) {
		Query<Follow> q = createQuery().filter("userId", userId).filter("followingId", followingId);
		WriteResult r = deleteByQuery(q);
		if (r.getError() != null) throw new RuntimeException(r.getError());
	}

	@Override
	public int countFollower(long userId) {
		return (int) count(createQuery().filter("followingId", userId));
	}

	@Override
	public List<Long> findFollowerIds(long userId) {
		List<Long> fanIds = new ArrayList<Long>();
		List<Follow> fans = createQuery().filter("followingId", userId).asList();
		for (Follow fan: fans) fanIds.add(fan.getUserId());
		return fanIds;
	}

	@Override
	public List<Long> findFriendIds(long userId) {
		List<Long> friendIds = new ArrayList<Long>();
		List<Follow> friends = createQuery().filter("userId", userId).asList();
		for (Follow f: friends) friendIds.add(f.getFollowingId());
		return friendIds;
	}
}
