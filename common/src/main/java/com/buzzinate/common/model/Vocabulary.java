package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * the vocabulary for useful words
 */
@Entity(value="vocabulary", noClassnameStored=true)
public class Vocabulary {
	@Id private String word;
	
	private int docFreq;
	private double possibility; // 越大表示越有可能是关键词
	private boolean nocheck;
	private String category;
	private long createAt;
	
	public Vocabulary() {
		
	}
	
	public Vocabulary(String word, String category, boolean nocheck) {
		this.word = word;
		this.nocheck = nocheck;
		this.category = category;
		this.createAt = System.currentTimeMillis();
	}
	
	public String getWord() {
		return word;
	}
	
	public void setWord(String word) {
		this.word = word;
	}
	
	public int getDocFreq() {
		return docFreq;
	}

	public void setDocFreq(int docFreq) {
		this.docFreq = docFreq;
	}
	
	public double getPossibility() {
		return possibility;
	}

	public void setPossibility(double possibility) {
		this.possibility = possibility;
	}

	public boolean isNocheck() {
		return nocheck;
	}

	public void setNocheck(boolean nocheck) {
		this.nocheck = nocheck;
	}

	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}

	public long getCreateAt() {
		return createAt;
	}
	
	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
}