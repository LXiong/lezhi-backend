package com.buzzinate.jianghu.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

@Entity(value="comment", noClassnameStored=true)
public class Comment {
	@Id private long id;
	
	private long userId;
	@Indexed
	private long sourceId;
	private long cid;
	
	private String text;
	private long createAt;

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getCid() {
		return cid;
	}
	
	public void setCid(long cid) {
		this.cid = cid;
	}
	
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public long getSourceId() {
		return sourceId;
	}
	
	public void setSourceId(long sourceId) {
		this.sourceId = sourceId;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public long getCreateAt() {
		return createAt;
	}
	
	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
}