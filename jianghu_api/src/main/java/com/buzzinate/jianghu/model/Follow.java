package com.buzzinate.jianghu.model;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;

@Entity(value="follow", noClassnameStored=true)
@Indexes(@Index(value="userId, followingId", unique=true, dropDups=true))
public class Follow {
	@Id private ObjectId id = new ObjectId();
	
	private long userId;
	private long followingId;
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
	
	public long getFollowingId() {
		return followingId;
	}
	
	public void setFollowingId(long followingId) {
		this.followingId = followingId;
	}

	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
}
