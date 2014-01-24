package com.buzzinate.common.model;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Indexes;

/**
 * 保存计算出来的用户跟用户的相似度
 * 
 * @author Brad Luo
 *
 */
@Entity(value="similarity", noClassnameStored=true)
@Indexes(@Index(value="user1Id, user2Id", unique=true, dropDups=true))
public class Similarity {
	@Id private ObjectId id = new ObjectId();
	
	private double similarity;
	@Indexed
	private boolean hot;
	
	private int intersectSize;
	private long user1Id;
	private long user2Id;
	private long createAt;
	
	public Similarity() {
	}
	
	public Similarity(long user1Id, long user2Id) {
		super();
		this.user1Id = user1Id;
		this.user2Id = user2Id;
	}
	
	public Similarity(long user1Id, long user2Id, int intersectSize) {
		super();
		this.intersectSize = intersectSize;
		this.user1Id = user1Id;
		this.user2Id = user2Id;
	}
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public boolean isHot() {
		return hot;
	}

	public void setHot(boolean hot) {
		this.hot = hot;
	}

	public int getIntersectSize() {
		return intersectSize;
	}

	public void setIntersectSize(int intersectSize) {
		this.intersectSize = intersectSize;
	}

	public Long getUser1Id() {
		return user1Id;
	}

	public void setUser1Id(Long user1Id) {
		this.user1Id = user1Id;
	}

	public Long getUser2Id() {
		return user2Id;
	}

	public void setUser2Id(Long user2Id) {
		this.user2Id = user2Id;
	}
	
	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}

	public void setUser1Id(long user1Id) {
		this.user1Id = user1Id;
	}

	public void setUser2Id(long user2Id) {
		this.user2Id = user2Id;
	}
}