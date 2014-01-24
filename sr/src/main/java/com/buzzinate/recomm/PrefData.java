package com.buzzinate.recomm;

import java.util.List;

public class PrefData {
	public static final int MISS = -1;
	
	private long id;
	private long createAt;
	private List<String> words;
	private int size;
	
	public PrefData(long id, List<String> words, long createAt, int size) {
		this.id = id;
		this.words = words;
		this.createAt = createAt;
		this.size = size;
	}
	
	public long getId() {
		return id;
	}
	
	public List<String> getWords() {
		return words;
	}
	
	public long getCreateAt() {
		return createAt;
	}

	public int getSize() {
		return size;
	}
}