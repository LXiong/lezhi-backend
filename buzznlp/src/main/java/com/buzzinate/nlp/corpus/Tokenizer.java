package com.buzzinate.nlp.corpus;

import java.util.ArrayList;
import java.util.List;

import com.buzzinate.nlp.chinese.Token;
import com.buzzinate.nlp.chinese.WordSegmenter;

public class Tokenizer implements CorpusProcessor {
	public static final String KEY_TOKEN = "token";
	
	@Override
	public void process(Corpus corpus) {
		List<String> texts = corpus.getTexts();
		List<List<Token>> tokenss = new ArrayList<List<Token>>();
		for (String s: texts) {
			tokenss.add(WordSegmenter.segmentSentence(s));
		}
		corpus.setData(KEY_TOKEN, tokenss);
	}
}