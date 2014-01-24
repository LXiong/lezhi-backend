package com.buzzinate.nlp.corpus;

import java.util.HashMap;
import java.util.List;

import com.buzzinate.nlp.chinese.Token;

import edu.ucla.sspace.matrix.YaleSparseMatrix;

public class SentenceWordMatrixBuilder implements CorpusProcessor {
	public static final String KEY_WORD_IDX = "wordIdx";
	public static final String KEY_SW_MATRIX = "swMatrix";

	@Override
	public void process(Corpus corpus) {
		List<List<Token>> tokenss = corpus.getData(Tokenizer.KEY_TOKEN);
		HashMap<String, Integer> wordFreq = corpus.getData(WordFreqBuilder.KEY_WORD_FREQ);
		
		HashMap<String, Integer> wordIdx = new HashMap<String, Integer>();
		for (String word: wordFreq.keySet()) {
			int idx = wordIdx.size();
			wordIdx.put(word, idx);
		}
		corpus.setData(KEY_WORD_IDX, wordIdx);
		
		YaleSparseMatrix ysm = new YaleSparseMatrix(tokenss.size(), wordFreq.size());
		for (int r = 0; r < tokenss.size(); r++) {
			for (Token token: tokenss.get(r)) {
				Integer c = wordIdx.get(token.getWord().toLowerCase());
				if (c != null) {
					ysm.set(r, c, ysm.get(r, c) + 1);
				}
			}
		}
		corpus.setData(KEY_SW_MATRIX, ysm);
	}
}