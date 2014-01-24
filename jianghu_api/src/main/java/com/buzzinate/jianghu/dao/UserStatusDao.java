package com.buzzinate.jianghu.dao;

import java.util.List;

import com.buzzinate.common.dao.BaseDao;
import com.buzzinate.jianghu.model.UserStatus;

public interface UserStatusDao extends BaseDao<UserStatus, Long> {
	UserStatus getOrNew(long id);
	List<UserStatus> findStatus(List<Long> userIds, int count, int page);
}
