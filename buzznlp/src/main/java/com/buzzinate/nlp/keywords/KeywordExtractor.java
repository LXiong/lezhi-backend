package com.buzzinate.nlp.keywords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import org.apache.commons.lang.StringUtils;
import org.ictclas4j.bean.DictLib;
import org.ictclas4j.bean.POSTag;
import org.ictclas4j.bean.Pos;
import org.ictclas4j.bean.SegAtom;
import org.ictclas4j.segment.Segment;

import com.buzzinate.nlp.chinese.WordSegmenter;
import com.buzzinate.nlp.corpus.Corpus;
import com.buzzinate.nlp.corpus.CorpusProcessor;
import com.buzzinate.nlp.corpus.PhraseRecognizer;
import com.buzzinate.nlp.corpus.Tokenizer;
import com.buzzinate.nlp.corpus.WordFreqBuilder;
import com.buzzinate.nlp.util.PriorityQueue.Entry;
import com.buzzinate.nlp.util.TextUtility;

public class KeywordExtractor {
	
	private static List<CorpusProcessor> corpusProcessors = Arrays.asList(
			new Tokenizer(),
			new WordFreqBuilder(),
			new PhraseRecognizer()
		);

	private static String cleanTitle(String t) {
		t = StringUtils.substringBefore(t, "_");
		t = StringUtils.substringBefore(t, "|");
		return t;
	}
	
	public static List<Entry<String, Integer>> extractKeywordFreqs(String title, List<String> sentences, int minFreq) {
		if (title == null) title = "";
		title = cleanTitle(title);
		if (!StringUtils.isBlank(title)) sentences.add(title);
		Corpus corpus = new Corpus(sentences);
		for (CorpusProcessor cp: corpusProcessors) cp.process(corpus);
		
		HashMap<String, String> word2raw = corpus.getData(WordFreqBuilder.KEY_WORD_RAW);
		HashMap<String, Integer> word2freq = corpus.getData(WordFreqBuilder.KEY_WORD_FREQ);
		HashSet<String> wordTag = corpus.getData(WordFreqBuilder.KEY_WORD_TAG);
		
		List<Entry<String, Integer>> es = new ArrayList<Entry<String, Integer>>();
		for (Map.Entry<String, Integer> e: word2freq.entrySet()) {
			String term = e.getKey();
			int freq = e.getValue();
			if (freq >= minFreq) {
				if (term.length() < 2 || wordTag.contains(term + "#O")) continue;
				
				String raw = word2raw.get(term);
				if (raw != null) term = raw; 
				if (wordTag.contains(term.toLowerCase() + "#NR") || title.contains(term) || isUseful(term)) es.add(new Entry<String, Integer>(term, freq));
			}
		}
		
		return es;
	}
	
	/**
	 * 根据话题名称返回相应分词结果的bigram,如果有分词碎片，则把相邻项结合
	 * @param trend
	 * @param minLength 如果trend的长度小于minLength,则不切分，直接返回原始trend名称
	 * @return
	 */
	public static List<String> extractTrendGram(String trend, Integer minLength) {
		List<String> trendGrams = new ArrayList<String>();
		if(trend.length() < minLength){
			trendGrams.add(trend);
			return trendGrams;
		}
		List<SegAtom> atoms = WordSegmenter.seg.split(trend).getAtoms();
		HashMap<Integer, String> singleHanzi = new HashMap<Integer, String>();
		for(int i = 0; i < atoms.size(); i++){
			SegAtom atom = atoms.get(i);
			Boolean isPunc = false;
			for(Pos pos : atom.getPosList()){
				if(pos.getTag() == POSTag.PUNC){
					isPunc = true;
					break;
				}
			}
			if(!isPunc){
				String term = atom.getWord();
				if(term.length() > 1){
					trendGrams.add(term);
				} else if(term.length() == 1){
					if(!WordSegmenter.stopwords.contains(term)){
						singleHanzi.put(i, term);
					}
					
				}
			}
		}
		for(int i = 0; i < atoms.size() - 1; i++){
			if(singleHanzi.containsKey(i) && singleHanzi.containsKey(i + 1)){
				trendGrams.add(singleHanzi.get(i) + singleHanzi.get(i + 1));
			}
		}
		return trendGrams;
	}
	
	public static void main(String[] args){
		System.out.println(extractTrendGram("孟非怒斥女嘉宾太现实", 5));
	}
	
	
//	private static Boolean isSingleHanzi(SegAtom atom) {
//		Boolean isPunc = false;
//		String term = atom.getWord();
//		for(Pos pos : atom.getPosList()){
//			if(pos.getTag() == POSTag.PUNC){
//				isPunc = true;
//				break;
//			}
//		}
//		if(!isPunc && term.length() == 1){
//			return true;
//		}
//	}
//	
//	
	public static boolean isUseful(String word) {
		if (word.contains("#")) return true;
		if (TextUtility.isFirstAscii(word)) {
			if (TextUtility.isFirstTwoUpperCase(word) && word.length() >= 2) return true;
		}
		else if (word.length() > 2) return true;
		return false;
	}
}