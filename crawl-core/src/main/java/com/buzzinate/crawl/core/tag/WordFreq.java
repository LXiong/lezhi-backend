package com.buzzinate.crawl.core.tag;

import java.lang.Comparable;
import java.util.ArrayList;
import java.util.List;

public class WordFreq implements Comparable<WordFreq> {
	private String word;
	private int freq;
	private double mi;
	
	public WordFreq(String word, int freq, double mi) {
		this.word = word;
		this.freq = freq;
		this.mi = mi;
	}

	public String getWord() {
		return word;
	}

	public int getFreq() {
		return freq;
	}

	public double getMi() {
		return mi;
	}

	@Override
	public String toString() {
		return word + "(" + freq + ", " + mi + ")";
	}

	@Override
	public int compareTo(WordFreq o) {
		return -Double.compare(score(), o.score());
	}
	
	public double score() {
		return freq * Math.log(1 + word.length()); 
	}
	
	public static List<String> toWords(List<WordFreq> tfs) {
		List<String> words = new ArrayList<String>();
		for (WordFreq tf: tfs) {
			words.add(tf.getWord());
		}
		return words;
	}
}