package com.buzzinate.model;

import java.util.HashMap;
import java.util.Map;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * 包含链接的原始微博内容及一些统计信息
 * 
 * @author Brad Luo
 *
 */
@Entity(value="tweet", noClassnameStored=true)
public class Tweet {
	//微博ID
	@Id private long id;
	
	private long uid;
	private long reUid;
	private String text;
	private long tweetTime;
	
	private Map<String, String> urls = new HashMap<String, String>();
	
	//微博转发数及评论数
	private long retweetSize;
	private long commentSize;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getUid() {
		return uid;
	}
	
	public void setUid(long uid) {
		this.uid = uid;
	}
	
	public long getReUid() {
		return reUid;
	}

	public void setReUid(long reUid) {
		this.reUid = reUid;
	}

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public long getTweetTime() {
		return tweetTime;
	}

	public void setTweetTime(long tweetTime) {
		this.tweetTime = tweetTime;
	}

	public long getRetweetSize() {
		return retweetSize;
	}
	
	public void setRetweetSize(long retweetSize) {
		this.retweetSize = retweetSize;
	}
	
	public long getCommentSize() {
		return commentSize;
	}
	
	public void setCommentSize(long commentSize) {
		this.commentSize = commentSize;
	}

	public Map<String, String> getUrls() {
		return urls;
	}

	public void setUrls(Map<String, String> urls) {
		this.urls = urls;
	}
}
