package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity(value="userWeibo", noClassnameStored=true)
public class UserWeibo {
	@Id
	private String usertweetid;
	
	private long userId;
	private String text;
	private long time;
	
	public UserWeibo() {
		
	}
	
	public UserWeibo(long userId, long tweetId, String text, long time) {
		this.usertweetid = userId + "-" + tweetId;
		this.userId = userId;
		this.text = text;
		this.time = time;
	}

	public String getUsertweetid() {
		return usertweetid;
	}

	public void setUsertweetid(String usertweetid) {
		this.usertweetid = usertweetid;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}