package com.buzzinate.jianghu.jredis;

import java.util.List;

public class PrefData {
	public static final int MISS = -1;
	
	private long id;
	private long createAt;
	private List<String> words;
	private int size;
	
	public PrefData() {
		
	}
	
	public PrefData(long id, List<String> words, long createAt, int size) {
		this.id = id;
		this.words = words;
		this.createAt = createAt;
		this.size = size;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}

	public List<String> getWords() {
		return words;
	}

	public void setWords(List<String> words) {
		this.words = words;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}