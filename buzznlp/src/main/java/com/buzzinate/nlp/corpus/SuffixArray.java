package com.buzzinate.nlp.corpus;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuffixArray {
	private static int maxLen = 3;
	
	public static HashMap<Phrase, Integer> extractPhrase(List<Suffix> suffixes, int minFreq, float minMI) {
		Collections.sort(suffixes, new Suffix.SuffixCmp(maxLen));
		
		HashMap<Phrase, Integer> result = new HashMap<Phrase, Integer>();
		
		@SuppressWarnings("unchecked")
		HashMap<Phrase, Integer>[] phrases = new HashMap[maxLen];
		for (int i = 0; i < phrases.length; i++) phrases[i] = new HashMap<Phrase, Integer>();
		int[] freq = new int[maxLen];
		Arrays.fill(freq, 0);
		String[] ft = new String[maxLen];
		Arrays.fill(ft, "");
		for (Suffix suffix: suffixes) {
			List<String> terms = suffix.toWords();
			int i = 0;
			for (; i < freq.length && i < terms.size(); i++) {
				if (terms.get(i).equalsIgnoreCase(ft[i])) freq[i]++;
				else break;
			}
			for (;i < freq.length && i < terms.size(); i++) {
				if (freq[i] >= minFreq) {
					Phrase phrase = Phrase.create(ft, 0, i+1);
					phrases[i].put(phrase, freq[i]); 
				}
				freq[i] = 1;
			}
			for (;i < freq.length; i++) {
				if (freq[i] >= minFreq) {
					Phrase phrase = Phrase.create(ft, 0, i+1);
					phrases[i].put(phrase, freq[i]);
				}
				freq[i] = 0;
			}
			
			i = 0;
			for (; i < freq.length && i < terms.size(); i++) {
				ft[i] = terms.get(i);
			}
			for (; i < freq.length; i++) {
				ft[i] = "";
			}
		}
		
		for (int i = 0;i < freq.length; i++) {
			if (freq[i] >= minFreq) {
				Phrase phrase = Phrase.create(ft, 0, i+1);
				phrases[i].put(phrase, freq[i]);
			}
			freq[i] = 0;
		}
		
		HashMap<String, Integer> all = new HashMap<String, Integer>();
		for(Map.Entry<Phrase, Integer> e: phrases[phrases.length - 1].entrySet()) {
			Phrase p = e.getKey();
			putMax(all, p.left().toString(), e.getValue());
			putMax(all, p.right().toString(), e.getValue());
		}
		for (int i = phrases.length - 2; i >= 1; i--) {
			for(Map.Entry<Phrase, Integer> e: phrases[i].entrySet()) {
				Phrase p = e.getKey();
				String pt = p.toString();
				if (isComplete(all, minFreq, i+1, pt, e.getValue())) {
					Phrase left = p.left();
					Phrase right = p.right();
					float mi = e.getValue() * 1f / (phrases[i-1].get(left) + phrases[i-1].get(right) - e.getValue());
					int mf = minFreq;
					if (i == 1) mf = mf * 2;
					if (mi >= minMI && i < phrases.length - 1) {
						result.put(p, e.getValue());
					}
//					System.out.println(pt + " => " + e.getValue() + ", " + mi);
				}
				putMax(all, p.left().toString(), e.getValue());
				putMax(all, p.right().toString(), e.getValue());
			}
		}
		
		return result;
	}
	
	private static boolean isComplete(HashMap<String, Integer> wordFreq, int minFreq, int wordNum, String word, int freq) {
		Integer lf = wordFreq.get(word.toLowerCase());
		if (lf == null) lf = 0;
		int freqThreshold = lf + 1;
		if (wordNum <= 2 && word.length() <= 2) freqThreshold = Math.max(minFreq, lf) * 5 / 4;
		return freqThreshold < freq;
	}
	
	private static void putMax(HashMap<String, Integer> map, String k, Integer v) {
		k = k.toLowerCase();
		Integer ov = map.get(k);
		if (ov != null) v = Math.max(ov, v);
		map.put(k, v);
	}
}