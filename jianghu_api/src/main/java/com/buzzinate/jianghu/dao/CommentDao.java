package com.buzzinate.jianghu.dao;

import java.util.List;

import com.buzzinate.common.dao.BaseDao;
import com.buzzinate.common.dao.Page;
import com.buzzinate.jianghu.model.Comment;

public interface CommentDao extends BaseDao<Comment, Long> {
	public List<Comment> getComments(Long sourceId, Page page);
	int countComment(long articleId);
}
