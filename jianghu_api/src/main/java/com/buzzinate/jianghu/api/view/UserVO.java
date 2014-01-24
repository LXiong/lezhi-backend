package com.buzzinate.jianghu.api.view;

import java.util.List;

import com.buzzinate.common.model.User;
import com.buzzinate.jianghu.dao.FollowDao;
import com.buzzinate.jianghu.dao.LikeDao;

public class UserVO {
	public long id;
	public String name;
	public String screenName;
	public boolean following;
	public String profileImageUrl;
	
	public UserVO(User user, boolean following) {
		this.id = user.getId();
		this.name = user.getName();
		this.screenName = user.getScreenName();
		this.following = following;
		this.profileImageUrl = user.getProfileImageUrl();
	}
	
	public static ExtView<UserVO>[] make(FollowDao followDao, LikeDao likeDao, List<User> users, long loginUserId) {
		ExtView<UserVO>[] results = new ExtView[users.size()];
		for (int i = 0; i < users.size(); i++) {
			User f = users.get(i);
			boolean following = followDao.findFriendIds(loginUserId).contains(f.getId());
			int friendsCount = followDao.findFriendIds(f.getId()).size();
			int followerCount = followDao.countFollower(f.getId());
			int likeCount = likeDao.countLikePerUser(f.getId());
			results[i] = ExtView.combine(new UserVO(f, following),
					"likeCount", likeCount, 
					"followersCount", followerCount, 
					"friendsCount", friendsCount);
		}
		return results;
	}
}
