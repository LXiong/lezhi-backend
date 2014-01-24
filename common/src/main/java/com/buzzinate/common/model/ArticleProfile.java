package com.buzzinate.common.model;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Indexes;

// TODO: 把文章关键词，topic分布，高频词等等信息都合并到这个类，同理，需要一个UserProfile类

@Entity(value="articleProfile", noClassnameStored=true)
@Indexes(@Index(value="keywords.word, -createAt", unique=false))
public class ArticleProfile {
	private static final List<Keyword> emptyKeywords = new ArrayList<Keyword>();
	private static final List<Integer> emptyMinhashes = new ArrayList<Integer>();
	
	@Id private long id;
	
	private String title;
	// 采用minHash来去重，先找minHash一样的文章，然后再根据minHashes来判断文章重复度，或者采用similarity?
	@Indexed
	private int minhash;
	private List<Integer> minhashes = emptyMinhashes;
	private List<Keyword> keywords = emptyKeywords;
	
	private long createAt;

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getMinhash() {
		return minhash;
	}

	public void setMinhash(int minhash) {
		this.minhash = minhash;
	}

	public List<Integer> getMinhashes() {
		return minhashes;
	}

	public void setMinhashes(List<Integer> minhashes) {
		this.minhashes = minhashes;
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}

	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
}