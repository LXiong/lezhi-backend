package com.buzzinate.nlp.corpus;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.buzzinate.nlp.util.TextUtility;

public class Phrase {
	private List<String> words;
	
	public Phrase(List<String> words) {
		this.words = words;
	}
	
	public Phrase left() {
		return new Phrase(words.subList(0, words.size()-1));
	}
	
	public Phrase right() {
		return new Phrase(words.subList(1, words.size()));
	}
	
	public List<String> getWords() {
		return words;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Phrase) {
			return toString().equalsIgnoreCase(obj.toString());
		}
		return false;
	}
	
	public String toToken() {
		return StringUtils.join(words, "#");
	}

	@Override
	public int hashCode() {
		return toString().toLowerCase().hashCode();
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

	public static Phrase create(String[] words, int start, int to) {
		List<String> ws = new ArrayList<String>();
		for (int i = start; i < to; i++) {
			ws.add(words[i]);
		}
		return new Phrase(ws);
	}
}
