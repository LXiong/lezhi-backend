package com.buzzinate.common.model;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;

@Entity(value="itemMinhash", noClassnameStored=true)
@Indexes(@Index(value="minhashes.hash, -createAt", unique=false))
public class ItemMinhash {
	private static final List<Minhash> emptyMinhashes = new ArrayList<Minhash>();
	
	@Id private long id;
	
	private List<Minhash> minhashes = emptyMinhashes;
	
	private long createAt;

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

	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
}