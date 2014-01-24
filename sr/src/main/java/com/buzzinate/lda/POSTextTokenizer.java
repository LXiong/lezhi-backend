package com.buzzinate.lda;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.buzzinate.nlp.chinese.Token;
import com.buzzinate.nlp.chinese.WordSegmenter;
import com.buzzinate.nlp.corpus.Phrase;
import com.buzzinate.nlp.corpus.Suffix;
import com.buzzinate.nlp.corpus.SuffixArray;

public class POSTextTokenizer {

	public static void main(String[] args) {
		String text = "惠普CEO李艾科（Leo Apotheker）周四宣布了该公司业务重组计划。这周作为该计划的组成部分，惠普将于本季度期间终止平板电脑和智能手机业务。尽管惠普去年收购了智能手机厂商Palm，并借此获得Palm的webOS手机操作系统，但却未能在智能手机和平板电脑业务领域有所作为。 ";
		System.out.println(new POSTextTokenizer().tokenize(text));
	}

	public List<String> tokenize(String text) {
		List<String> result = new ArrayList<String>();
		List<Token> tokens = WordSegmenter.segmentSentence(text);
		
		List<Suffix> suffixes = new ArrayList<Suffix>();
		Token prev = new Token(null);
		List<String> ws = new ArrayList<String>(); 
		for (Token token : tokens) {

			if (token.getPos() == prev.getPos() + 1) {
				ws.add(token.getWord());
			} else {
				if (ws.size() > 1) {
					// System.out.println(ws);
					suffixes.addAll(Suffix.create(ws));
				}
				ws = new ArrayList<String>();
				ws.add(token.getWord());
			}
			prev = token;
		}
		if (ws.size() > 1) {
//			System.out.println(ws);
			suffixes.addAll(Suffix.create(ws));
		}
		
		HashSet<String> phrases = new HashSet<String>();
		for (Map.Entry<Phrase, Integer> e: SuffixArray.extractPhrase(suffixes, 3, 0.75f).entrySet()) {
			phrases.add(e.getKey().toToken().toLowerCase());
		}
		
		prev = new Token(null);
		for (Token token: tokens) {
			result.add(token.getWord());
			if (token.getPos() == prev.getPos() + 1) {
				String phrase = prev.getWord() + "#" + token.getWord();
				if (phrases.contains(phrase)) result.add(phrase);
			}
			prev = token;
		}
		
		return result;
	}
}