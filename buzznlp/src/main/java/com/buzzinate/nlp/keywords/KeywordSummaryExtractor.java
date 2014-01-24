package com.buzzinate.nlp.keywords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.buzzinate.nlp.corpus.Corpus;
import com.buzzinate.nlp.corpus.CorpusProcessor;
import com.buzzinate.nlp.corpus.PhraseRecognizer;
import com.buzzinate.nlp.corpus.SentenceWordMatrixBuilder;
import com.buzzinate.nlp.corpus.Tokenizer;
import com.buzzinate.nlp.corpus.WordFreqBuilder;
import com.buzzinate.nlp.matrix.scoring.HITSnSVDScorer;
import com.buzzinate.nlp.matrix.scoring.ScoreResult;
import com.buzzinate.nlp.util.PriorityQueue;
import com.buzzinate.nlp.util.PriorityQueue.Entry;
import com.buzzinate.nlp.util.SortUtils;
import com.buzzinate.nlp.util.TextUtility;

import edu.ucla.sspace.matrix.SparseMatrix;

// TODO: 需要重构，暂时因时间关系，保留现在代码的重复
public class KeywordSummaryExtractor {
	private List<CorpusProcessor> corpusProcessors = Arrays.asList(
			new Tokenizer(),
			new WordFreqBuilder(),
			new PhraseRecognizer(),
			new SentenceWordMatrixBuilder()
		);
	private static HITSnSVDScorer scorer = new HITSnSVDScorer();
	
	private String title;
	private List<String> sentences;
	private Corpus corpus;
	private ScoreResult scoreResult;
	
	public KeywordSummaryExtractor(String title, List<String> sentences) {
		if (title == null) title = "";
		title = cleanTitle(title);
		this.title = title;
		this.sentences = sentences;
		if (!StringUtils.isBlank(title)) this.sentences.add(title);
		this.corpus = new Corpus(sentences);
		
		extract();
	}

	private String cleanTitle(String t) {
		t = StringUtils.substringBefore(t, "_");
		t = StringUtils.substringBefore(t, "|");
		return t;
	}

	private void extract() {
		for (CorpusProcessor cp: corpusProcessors) cp.process(corpus);
		
		SparseMatrix m = corpus.getData(SentenceWordMatrixBuilder.KEY_SW_MATRIX);
		if (m.rows() > 0 && m.columns() > 0) scoreResult = scorer.score(m);
	}
	
	public List<Entry<String, Integer>> extractFreqWords(int minFreq) {
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
	
	public List<Entry<Double, String>> extractKeywords() {
		if (scoreResult == null) return new ArrayList<Entry<Double, String>>();
		
		HashMap<String, Integer> wordIdx = corpus.getData(SentenceWordMatrixBuilder.KEY_WORD_IDX);
		HashMap<String, String> word2raw = corpus.getData(WordFreqBuilder.KEY_WORD_RAW);
		String[] terms = new String[wordIdx.size()];
		for (Map.Entry<String, Integer> e: wordIdx.entrySet()) {
			String term = e.getKey();
			String raw = word2raw.get(term);
			if (raw != null) term = raw; 
			terms[e.getValue()] = term;
		}
		
		PriorityQueue<Double, String> tpq = PriorityQueue.make((int)(5 + Math.log(terms.length)), SortUtils.reverse(Double.class), new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return - new Integer(s1.length()).compareTo(s2.length());
			}
		});
		
		HashSet<String> wordTag = corpus.getData(WordFreqBuilder.KEY_WORD_TAG);
		
		List<Entry<Double, String>> es = new ArrayList<Entry<Double, String>>();
		
		double[] colScore = scoreResult.getColScore();
		for (int i = 0; i < colScore.length; i++) {
//			System.out.println(terms[i] + " ==> " + colScore[i]);
			if (Double.isNaN(colScore[i]) || terms[i].length() < 2 || wordTag.contains(terms[i] + "#O")) continue; 
			if (wordTag.contains(terms[i].toLowerCase() + "#NR")) es.add(new Entry<Double, String>(colScore[i] * 5 / 3, terms[i]));
			if (title.contains(terms[i]) || isUseful(terms[i])) tpq.add(colScore[i], terms[i]);
		}
		
		es.addAll(tpq.entries());
		return es;
	}
	
	public static boolean isUseful(String word) {
		if (word.contains("#")) return true;
		if (TextUtility.isFirstAscii(word)) {
			if (TextUtility.isFirstTwoUpperCase(word) && word.length() >= 2) return true;
		}
		else if (word.length() > 2) return true;
		return false;
	}
	
	public String extractSummary(int maxLen) {
		if (scoreResult == null) return "";
		int extLen = maxLen * 3 / 2;
		double[] rowScore = scoreResult.getRowScore();
		for (int k = 0; k < rowScore.length; k++) {
			double score = rowScore[k];
			if (Double.isNaN(score)) return "";
			if (StringUtils.substringBefore(sentences.get(k), "：").length() < 5) score *= 0.75;
			if (StringUtils.substringBefore(sentences.get(k), ":").length() < 5) score *= 0.75;
			if (StringUtils.countMatches(sentences.get(k), "：") >= 2) score *= 0.5;
			if (StringUtils.countMatches(sentences.get(k), ":") >= 2) score *= 0.5;
			if (StringUtils.countMatches(sentences.get(k), " ") >= 2) score *= 0.5;
			if (StringUtils.countMatches(sentences.get(k), "_") >= 2) score *= 0.5;
			rowScore[k] = score;
		}
		
		int[] sumLen = new int[sentences.size() + 1];
		double[] sumScore = new double[sentences.size() + 1];
		double maxScore = 0;
		
		sumLen[0] = 0;
		sumScore[0] = 0;
		for (int i = 0; i < sentences.size() && i < rowScore.length; i++) {
			sumLen[i+1] = sumLen[i] + sentences.get(i).length();
			sumScore[i+1] = sumScore[i] + rowScore[i] * rowScore[i] + 0.4 * rowScore[i];
			if (maxScore < rowScore[i]) maxScore = rowScore[i];
		}
		
		int last = 0;
		double maxAvgScore = 0;
		int from = 0;
		int to = 0;
		for (int i = 1; i <= sentences.size() && i <= rowScore.length; i++) {
			for (int j = last; j < i; j++) {
				double avgScore = (sumScore[i] - sumScore[j]) / (i - j); 
				if (avgScore > maxAvgScore && sumLen[i] - sumLen[j] >= extLen / 2) {
					maxAvgScore = avgScore;
					from = j-1;
					to = i;
				}
			}
			while (sumLen[i] - sumLen[last] > extLen) last++;
			if (rowScore[i-1] * 2 < maxScore) last = i - 1;
		}
		
		StringBuffer sb = new StringBuffer();
		if (from < 0) from = 0;
		for (int k = from; k < to; k++) {
			String sentence = sentences.get(k);
//			sentence = tagReg.matcher(sentence).replaceAll("");
			if (sentence.endsWith("。") || sentence.endsWith(".")) sb.append(sentence);
			else sb.append(sentence + " ");
		}
		
		String summary = sb.toString().trim();
		if (summary.length() > maxLen) summary = summary.substring(0, maxLen);
		return summary;
	}
}