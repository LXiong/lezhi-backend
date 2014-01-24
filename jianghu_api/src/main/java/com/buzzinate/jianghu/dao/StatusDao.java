package com.buzzinate.jianghu.dao;

import java.util.List;

import com.buzzinate.common.dao.BaseDao;
import com.buzzinate.common.dao.Page;
import com.buzzinate.jianghu.model.Status;

public interface StatusDao extends BaseDao<Status, Long> {
	List<Status> findByUserId(long userId, Page page);
	long countSince(List<Long> userIds, long lastId);
}
