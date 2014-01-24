package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Embedded;

@Embedded
public class Keyword {
	private String word;
	private int freq;
	
	public Keyword() {
	}
	
	public Keyword(String word, int freq) {
		this.word = word;
		this.freq = freq;
	}

	public String getWord() {
		return word;
	}

	public int getFreq() {
		return freq;
	}

	@Override
	public String toString() {
		return word + "(" + freq + ")";
	}
}