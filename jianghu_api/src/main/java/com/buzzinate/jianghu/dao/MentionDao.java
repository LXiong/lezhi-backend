package com.buzzinate.jianghu.dao;

import java.util.List;

import com.buzzinate.common.dao.Page;
import com.buzzinate.jianghu.model.Mention;

public interface MentionDao {
	void addMention(Mention mention);
	List<Mention> findByUser(long userId, Page page);
	long countSince(long userId, long lastId);
}
