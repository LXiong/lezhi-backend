package com.buzzinate.jianghu.api.view;

import com.buzzinate.common.model.User;
import com.buzzinate.jianghu.model.Comment;

public class CommentVO {
	public long id;
	public long sourceId;
	public String text;
	public long createdAt;
	public UserVO user;
	
	public CommentVO(Comment comment, User user, boolean following) {
		this.id = comment.getId();
		this.sourceId = comment.getSourceId();
		this.text = comment.getText();
		this.createdAt = comment.getCreateAt();
		this.user = new UserVO(user, following);
	}
}
