package com.buzzinate.common.dao;

import java.util.Collection;
import java.util.List;

import com.buzzinate.common.dao.BaseDao;
import com.buzzinate.common.model.User;

public interface UserDao extends BaseDao<User, Long> {
	List<User> findAll(int offset, int limit);
	User findByAccessToken(String accessToken);
	User findByUid(long uid);
	User findByCookieId(String cookieId);
	List<User> findByScreenName(List<String> names);
	
	List<Long> findLeziUserIds();
	void saveOrUpdate(User user);
	void saveOrUpdate(Collection<User> users);
	void incPrefSize(long userId);
}
