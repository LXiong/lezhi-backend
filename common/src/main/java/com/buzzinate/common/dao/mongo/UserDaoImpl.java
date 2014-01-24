package com.buzzinate.common.dao.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.model.User;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class UserDaoImpl extends BaseDaoDefault<User, Long> implements UserDao {
	public UserDaoImpl(Datastore datastore) {
		super(datastore);
	}
	
	public List<User> get(List<Long> userIds) {
		return createQuery().field("id").hasAnyOf(userIds).asList();
	}

	@Override
	public List<User> findAll(int offset, int limit) {
		return createQuery().offset(offset).limit(limit).asList();
	}

	@Override
	public User findByAccessToken(String accessToken) {
		return createQuery().filter("accessToken", accessToken).get();
	}

	@Override
	public User findByUid(long uid) {
		return createQuery().filter("uid", uid).get();
	}
	
	@Override
	public User findByCookieId(String cookieId) {
		return createQuery().filter("cookieId", cookieId).get();
	}

	@Override
	public List<User> findByScreenName(List<String> names) {
		if (names.isEmpty()) return new ArrayList<User>();
		return createQuery().filter("screenName in", names).asList();
	}

	@Override
	public List<Long> findLeziUserIds() {
		List<User> users = createQuery().filter("isLeziUser", true).asList();
		List<Long> leziUserIds = new ArrayList<Long>();
		for (User leziUser: users) leziUserIds.add(leziUser.getId());
		return leziUserIds;
	}

	@Override
	public void saveOrUpdate(User user) {
		Query<User> q = createQuery().filter("uid", user.getUid());
		User pUser = q.get();
		if (pUser == null) save(user);
		else {
			UpdateOperations<User> uo = createUpdateOperations();
			uo.set("screenName", user.getScreenName());
			uo.set("name", user.getName());
			uo.set("profileImageUrl", user.getProfileImageUrl());
			uo.set("followersSize", user.getFollowersSize());
			uo.set("tweetsSize", user.getTweetsSize());
			uo.set("detail", user.getDetail());
			update(q, uo);
			user.setId(pUser.getId());
		}
	}
	
	@Override
	public void saveOrUpdate(Collection<User> users) {
		if(users == null || users.size() == 0){
			return;
		}
		HashMap<Long, User> uid2user = new HashMap<Long, User>();
		
		Collection<Long> uids = new ArrayList<Long>();
		for(User user : users){
			uid2user.put(user.getUid(), user);
			uids.add(user.getUid());
		}
		List<User> existsUsers = createQuery().filter("uid in", uids).asList();
		HashSet<Long> existsUids = new HashSet<Long>();
		List<User> newUsers = new ArrayList<User>();
		for(User existsUser : existsUsers){
			existsUids.add(existsUser.getUid());
			User updateUser = uid2user.get(existsUser.getUid());
			existsUser.setScreenName(updateUser.getScreenName());
			existsUser.setName(updateUser.getName());
			existsUser.setProfileImageUrl(updateUser.getProfileImageUrl());
			existsUser.setFollowersSize(updateUser.getFollowersSize());
			existsUser.setTweetsSize(updateUser.getTweetsSize());
			existsUser.setDetail(updateUser.getDetail());
			newUsers.add(existsUser);
			updateUser.setId(existsUser.getId());
		}
		
		for(User user : users){
			if(!existsUids.contains(user.getUid())){
				newUsers.add(user);
			}
		}
		ds.save(newUsers);
	}

	@Override
	public void incPrefSize(long userId) {
		UpdateOperations<User> uo = createUpdateOperations();
		uo.inc("prefSize", 1);
		update(createQuery().filter("id", userId), uo);
	}
}