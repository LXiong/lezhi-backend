package com.buzzinate.keyword;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.crawl.core.tag.SuffixArray;
import com.buzzinate.crawl.core.tag.WordFreq;

public class KeywordUtil {
	public static List<Keyword> extract(Dictionary dict, String title, List<String> texts) {
		List<String> snippets = new ArrayList<String>();
		snippets.add(title);
		snippets.addAll(texts);
		return merge(SuffixArray.extractKeywords(snippets, 3, 0.5, 0.5), dict.extract(title, texts, 5, 20));
	}
	
	private static List<Keyword> merge(List<WordFreq> words, List<Keyword> keywords) {
		HashSet<String> wordset = new HashSet<String>();
		List<Keyword> result = new ArrayList<Keyword>();
		if (words.size() > 10) words = words.subList(0, 10);
		for (WordFreq wf: words) {
			wordset.add(wf.getWord());
			result.add(new Keyword(wf.getWord(), wf.getFreq()));
		}
		for (Keyword kw: keywords) {
			if (!wordset.contains(kw.getWord())) result.add(kw); 
		}
		return result;
	}
}