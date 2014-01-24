package com.buzzinate.jianghu.api.view;


import com.buzzinate.common.model.User;
import com.buzzinate.jianghu.model.Comment;
import com.buzzinate.jianghu.model.Mention;

public class MentionVO {
	public long createdAt;
	public long id;
	public String text;
	public long sourceId;
	public int type;
	public UserVO user;
	
	public MentionVO(Mention mention, Comment comment, User user, boolean following) {
		this.createdAt = mention.getCreateAt();
		this.id = mention.getId();
		this.text = comment.getText();
		this.sourceId = comment.getSourceId();
		this.type = mention.getType();
		this.user = new UserVO(user, following);
	}
}
