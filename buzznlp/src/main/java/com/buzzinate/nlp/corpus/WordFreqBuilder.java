package com.buzzinate.nlp.corpus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.buzzinate.nlp.chinese.Token;
import com.buzzinate.nlp.util.Counter;

public class WordFreqBuilder implements CorpusProcessor {
	public static final String KEY_WORD_FREQ = "wordFreq";
	public static final String KEY_WORD_RAW = "wordRaw";
	public static final String KEY_WORD_TAG = "wordTag";
	
	private int minFreq = 2;
	
	public WordFreqBuilder() { }
	
	public WordFreqBuilder(int minFreq) {
		this.minFreq = minFreq;
	}

	@Override
	public void process(Corpus corpus) {
		List<List<Token>> tokenss = corpus.getData(Tokenizer.KEY_TOKEN);
		
		Counter<String> wordCnt = new Counter<String>();
		for (List<Token> tokens: tokenss) {
			for (Token t: tokens) wordCnt.add(t.getWord().toLowerCase());
		}
		
		HashMap<String, Integer> wordFreq = new HashMap<String, Integer>();
		for (Map.Entry<String, Integer> e: wordCnt.toMap().entrySet()) {
			if (e.getValue() >= minFreq) wordFreq.put(e.getKey(), e.getValue());
		}
		
		HashMap<String, String> word2raw = new HashMap<String, String>();
		HashSet<String> wordTag = new HashSet<String>();
		for (List<Token> tokens: tokenss) {
			for (Token t: tokens) {
				String word = t.getWord();
				String lc = word.toLowerCase();
				wordTag.add(lc + "#" + t.getTag());
				if (!word.equals(lc)) word2raw.put(lc, word);
			}
		}
		
		corpus.setData(KEY_WORD_FREQ, wordFreq);
		corpus.setData(KEY_WORD_RAW, word2raw);
		corpus.setData(KEY_WORD_TAG, wordTag);
	}
}