package com.buzzinate.crawl.core.tag;

import java.util.ArrayList;
import java.util.List;

import com.buzzinate.crawl.core.util.TextUtility;

public class Phase {
	private List<String> words;
	
	public Phase(List<String> words) {
		this.words = words;
	}
	
	public boolean isUseful() {
		if (words.size() >= 2) return true;
		if (words.size() == 1 && words.get(0).length() >= 2) return true;
		return false;
	}
	
	public Phase left() {
		return new Phase(words.subList(0, words.size()-1));
	}
	
	public Phase right() {
		return new Phase(words.subList(1, words.size()));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Phase) {
			return toString().equals(obj.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return format(words);
	}
	
	public static String format(List<String> words) {
		StringBuffer sb = new StringBuffer();
		boolean prevEn = false;
		for (String word: words) {
			if (TextUtility.isFirstAscii(word)) {
				if (prevEn) sb.append(" ");
				prevEn = true;
			} else {
				prevEn = false;
			}
			sb.append(word);
		}
		return sb.toString();
	}

	public static Phase create(String[] words, int start, int to) {
		List<String> ws = new ArrayList<String>();
		for (int i = start; i < to; i++) {
			ws.add(words[i]);
		}
		return new Phase(ws);
	}
	
	public static Phase create(List<String> words, int start, int to) {
		return new Phase(words.subList(start, to));
	}
}