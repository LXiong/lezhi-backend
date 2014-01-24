package com.buzzinate.jianghu.dao;

import java.util.List;

public interface FollowDao {
	void addFollowing(long userId, long followingId);
	void removeFollowing(long userId, long followingId);
	int countFollower(long userId);
	List<Long> findFollowerIds(long id);
	List<Long> findFriendIds(long userId);
}
