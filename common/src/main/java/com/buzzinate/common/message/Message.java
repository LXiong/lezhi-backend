package com.buzzinate.common.message;

public class Message {
	
	public enum MessageType {
		Fetched, Crawled, Classified, Recommended, Liked, NewUser
	}
	
	public long userId;
	public Long pageId;
	public Boolean isLike;
	public MessageType type;
	
	public Boolean isTrendMsg = false;
	public String trendName;
	
	public Message() { }
	
	public Message(long userId, Long pageId, MessageType type) {
		this.userId = userId;
		this.pageId = pageId;
		this.type = type;
	}
	
	public Message(long userId, Long pageId, Boolean isLike, MessageType type) {
		this.userId = userId;
		this.pageId = pageId;
		this.isLike = isLike;
		this.type = type;
	}

	public Message(long userId, MessageType type) {
		this.userId = userId;
		this.type = type;
	}
}
