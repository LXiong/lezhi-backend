package com.buzzinate.common.model;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;

@Entity(value="userDailyStat", noClassnameStored=true)
@Indexes(@Index(value="date, userId", unique=true, dropDups=true))
public class UserDailyStat {
	@Id private ObjectId id = new ObjectId();
	private long date;
	private long userId;
	private int nLike;
	private int nRead;
	
	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public long getDate() {
		return date;
	}
	
	public void setDate(long date) {
		this.date = date;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public int getnLike() {
		return nLike;
	}
	
	public void setnLike(int nLike) {
		this.nLike = nLike;
	}
	
	public int getnRead() {
		return nRead;
	}
	
	public void setnRead(int nRead) {
		this.nRead = nRead;
	}
}