package com.buzzinate.common.model;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

/**
 * 记录用户喜欢（发表）文章的偏好
 * 
 * @author Brad Luo
 *
 */
@Entity(value="preference", noClassnameStored=true)
public class Preference {
	@Id private ObjectId id = new ObjectId();
	
	@Indexed(background=true)
	private long userId;
	@Indexed(background=true)
	private long pageId;
	private long createAt;
	
	public Preference() {
	}

	public Preference(long userId, long pageId, long createAt) {
		super();
		this.userId = userId;
		this.pageId = pageId;
		this.createAt = createAt;
	}
	
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
	public long getPageId() {
		return pageId;
	}
	public void setPageId(long pageId) {
		this.pageId = pageId;
	}
	public long getCreateAt() {
		return createAt;
	}
	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
}
