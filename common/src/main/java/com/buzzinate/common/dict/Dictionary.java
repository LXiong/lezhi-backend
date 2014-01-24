package com.buzzinate.common.dict;

import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;
import org.apache.mahout.math.map.OpenObjectIntHashMap;
import org.arabidopsis.ahocorasick.WordFreqTree;

import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.Vocabulary;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.common.util.PriorityQueue.Entry;

public class Dictionary {
	private WordFreqTree wft;
	private OpenObjectIntHashMap<String> docFreq;
	private OpenObjectDoubleHashMap<String> possibilities;
	private int numDocs;
	private double threshold = 0;
	
	public Dictionary(List<Vocabulary> vocs) {
		int maxFreq = 0;
		docFreq = new OpenObjectIntHashMap<String>(vocs.size());
		possibilities = new OpenObjectDoubleHashMap<String>(vocs.size());
		wft = new WordFreqTree();
		for (Vocabulary voc: vocs) {
			wft.add(voc.getWord());
			docFreq.put(voc.getWord(), voc.getDocFreq());
			possibilities.put(voc.getWord(), voc.getPossibility());
			if (voc.isNocheck() && voc.getPossibility() < 10) possibilities.put(voc.getWord(), 10);
			if (maxFreq < voc.getDocFreq()) maxFreq = voc.getDocFreq();
		}
		wft.build();
		
		numDocs = maxFreq * 2;
		
		PriorityQueue<Double, String> npq = PriorityQueue.make(vocs.size() / 25, SortUtils.comp(Double.class), SortUtils.comp(String.class));
		for (Vocabulary voc: vocs) {
			if (voc.isNocheck()) continue;
			double idf = Math.log(numDocs / (voc.getDocFreq() + 1));
			npq.add(idf * voc.getPossibility() * Math.log(1 + voc.getWord().length()), voc.getWord());
		}
		
		for (Entry<Double, String> e: npq.entries()) {
			if (threshold < e.getKey()) threshold = e.getKey();
		}
	}
	
	public int docFreq(String word) {
		Integer df = docFreq.get(word);
		if (df == null || df == 0) df = numDocs / 3;
		return df;
	}
	
	public double idf(String word) {
		double df = docFreq(word) + 1;
		return Math.log(numDocs / df);
	}
	
	public List<Keyword> extract(String title, List<String> snippets, int min, int max) {
		List<String> all = new ArrayList<String>();
		all.add(title);
		all.add(title);
		all.addAll(snippets);
		return extract(all, min, max);
	}
	
	public List<Keyword> extract(List<String> snippets, int min, int max) {
		List<Keyword> keywords = new ArrayList<Keyword>();
		OpenObjectIntHashMap<String> wordCnt = new OpenObjectIntHashMap<String>();
		for (String snippet: snippets) {
			for (String word: wft.search(snippet)) {
				wordCnt.adjustOrPutValue(word, 1, 1);
			}
		}
		
		int size = wordCnt.size() * 3 / 5;
		if (size > max) size = max;
		if (size < min) size = min; 
		final PriorityQueue<Double, String> topq = PriorityQueue.make(size, SortUtils.reverse(Double.class), SortUtils.reverse(String.class));
		wordCnt.forEachPair(new ObjectIntProcedure<String>(){
			@Override
			public boolean apply(String word, int freq) {
				double idf = idf(word);
				double poss = possibilities.get(word);
				double t = idf * poss * Math.log(1 + word.length());
				if (t >= threshold) topq.add(freq * t, word);
				return true;
			}
		});
		
		if (topq.size() < min) {
			wordCnt.forEachPair(new ObjectIntProcedure<String>(){
				@Override
				public boolean apply(String word, int freq) {
					double idf = idf(word);
					double poss = possibilities.get(word);
					double t = idf * poss * Math.log(1 + word.length());
					if (t < threshold) topq.add(freq * t / 4, word);
					return true;
				}
			});
		}
		
		for (String word: topq.values()) {
			keywords.add(new Keyword(word, wordCnt.get(word)));
		}
		
		return keywords;
	}
}