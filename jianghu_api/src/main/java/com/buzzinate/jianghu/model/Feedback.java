package com.buzzinate.jianghu.model;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

@Entity(value="feedback", noClassnameStored=true)
public class Feedback {
	@Id private ObjectId id = new ObjectId();
	
	@Indexed
	private long userId;
	private long articleId;
	private String feedback;
	private long createAt;
	
	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public long getArticleId() {
		return articleId;
	}
	
	public void setArticleId(long articleId) {
		this.articleId = articleId;
	}
	
	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	public long getCreateAt() {
		return createAt;
	}
	
	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
}
