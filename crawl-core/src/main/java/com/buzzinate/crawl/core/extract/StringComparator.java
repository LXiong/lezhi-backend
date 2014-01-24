package com.buzzinate.crawl.core.extract;

import java.util.HashSet;
import java.util.List;

import com.buzzinate.crawl.core.util.TextUtility;

public class StringComparator {
	private HashSet<String> ngrams;
	
	public StringComparator(String base) {
		this.ngrams = createNgram(base, 3);
	}
	
	public float compare(String text) {
		HashSet<String> textNgrams = createNgram(text, 3);
		int common = 0;
		for (String ngram: ngrams) {
			if (textNgrams.contains(ngram)) common++;
		}
		
        return (float) (common/ (ngrams.size() + Math.log(textNgrams.size())));
	}
	
	public static HashSet<String> createNgram(String text, int n) {
		HashSet<String> tokens = new HashSet<String>();
		List<String> words = TextUtility.splitWords(text, TextUtility.keepPuncs);
		int lastPunc = -1;
		for (int i = 0; i < words.size(); i++) {
			if (TextUtility.keepPuncs.contains(words.get(i))) lastPunc = i;
			String[] ws = new String[]{"_", "_", "_"};
			if (i >= 2 && lastPunc < i-2) ws[0] = words.get(i-2);
			if (i >= 1 && lastPunc < i-1) ws[1] = words.get(i-1);
			if (i >= 0 && lastPunc < i) ws[2] = words.get(i);
			tokens.add(ws[0] + ws[1] + ws[2]);
		}
		return tokens;
	}
	
	public static void main(String[] args) {
		StringComparator sc = new StringComparator("InfoQ: Selenium 2(又名Selenium WebDriver)发布");
		System.out.println(sc.compare("Selenium 2(又名Selenium WebDriver)发布 xxxx"));
		System.out.println(sc.compare("Selenium 2(又名Selenium WebDriver)发"));
	}
}
