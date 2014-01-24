package com.buzzinate.common.model;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;

@Entity(value="userMinhash", noClassnameStored=true)
@Indexes(@Index(value="minhashes.hash, -lastModified", unique=false))
public class UserMinhash {
	private static final List<Minhash> emptyMinhashes = new ArrayList<Minhash>();
	
	@Id private long id;
	
	private List<Minhash> minhashes = emptyMinhashes;
	
	private long lastModified;

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public List<Minhash> getMinhashes() {
		return minhashes;
	}

	public void setMinhashes(List<Minhash> minhashes) {
		this.minhashes = minhashes;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
}