package com.buzzinate.nlp.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.buzzinate.nlp.chinese.Token;

public class PhraseRecognizer implements CorpusProcessor {
	private int minFreq = 2;
	private float minMI = 0.75f;
	

	@Override
	public void process(Corpus corpus) {
		List<List<Token>> tokenss = corpus.getData(Tokenizer.KEY_TOKEN);
		HashMap<String, Integer> wordFreq = corpus.getData(WordFreqBuilder.KEY_WORD_FREQ);
		HashMap<String, String> word2raw = corpus.getData(WordFreqBuilder.KEY_WORD_RAW);
		
		List<Suffix> suffixes = new ArrayList<Suffix>();
		for (List<Token> tokens: tokenss) {
			Token prev = new Token(null);
			List<String> ws = new ArrayList<String>(); 
			for (Token token: tokens) {
				if (wordFreq.containsKey(token.getWord().toLowerCase())) {
					if (token.getPos() == prev.getPos() + 1) {
						ws.add(token.getWord());
					} else {
						if (ws.size() > 1) {
//							System.out.println(ws);
							suffixes.addAll(Suffix.create(ws));
						}
						ws = new ArrayList<String>();
						ws.add(token.getWord());
					}
					prev = token;
				}
			}
			if (ws.size() > 1) {
//				System.out.println(ws);
				suffixes.addAll(Suffix.create(ws));
			}
		}
		
		HashMap<Phrase, Integer> phrases = SuffixArray.extractPhrase(suffixes, minFreq, minMI);
		List<String> phraseTokens = new ArrayList<String>();
		for (Map.Entry<Phrase, Integer> e: phrases.entrySet()) {
			Phrase p = e.getKey();
			String token = p.toToken();
			phraseTokens.add(token);
			
			for (String word: p.getWords()) {
				wordFreq.remove(word.toLowerCase());
			}
			String lc = token.toLowerCase();
			wordFreq.put(lc, e.getValue());
			if (!token.equals(lc)) word2raw.put(lc, token);
		}
		
		for (List<Token> tokens: tokenss) {
			List<String> ws = new ArrayList<String>();
			for (Token t: tokens) ws.add(t.getWord());
			String sentence = StringUtils.join(ws, "#");
			for (String pt: phraseTokens) {
				int cnt = StringUtils.countMatches(sentence, pt);
				for (int i = 0; i < cnt; i++) tokens.add(new Token(pt));
			}
		}
	}

	
}