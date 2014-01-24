package com.buzzinate.crawl.core.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buzzinate.crawl.core.text.ExtractNNP;
import com.buzzinate.crawl.core.util.TextUtility;
import com.google.common.collect.Lists;

public class SuffixArray {
	
	public static void main(String[] args) {
		String text = "惠普CEO李艾科（Leo Apotheker）周四宣布了P2P该公司P2P业务重组计划，Why个性化推荐Why作为个性化阅读器的技术。这周作为该计划P2P的组成部分，惠普将于P2P本季度期间终止平板电脑和智能手机业务。尽管惠普去年收购了智能手机厂商Palm，并借此获得Palm的webOS手机操作系统，但却未能在智能手机和平板电脑业务领域有所作为。 ";
		List<WordFreq> keywords = extractKeywords(text, 2, 0.75, 0.5);
		System.out.println(keywords);
	}
	
	public static List<WordFreq> extractKeywords(String text, int minFreq, double minMI, double minEntropy) {
		List<String> sentences = TextUtility.splitSentences(text);
		return extractKeywords(sentences, minFreq, minMI, minEntropy);
	}
	
	public static List<WordFreq> extractKeywords(List<String> texts, int minFreq, double minMI, double minEntropy) {
		List<List<String>> sentences = new ArrayList<List<String>>();
		for (String text: texts) {
			sentences.add(TextUtility.splitWords(ExtractNNP.stemAll(text.toLowerCase())));
		}
		
		HashMap<Phase, Integer> p2cnt = new HashMap<Phase, Integer>();
		for (List<String> sentence: sentences) {
			for (int i = 0; i < sentence.size(); i++) inc(p2cnt, new Phase(sentence.subList(i, i+1)), 1);
		}
		
		HashMap<String, Double> phase2re = new HashMap<String, Double>();
		List<Suffix> suffixes = new ArrayList<Suffix>();
		for (List<String> sentence: sentences) {
			suffixes.addAll(Suffix.create(Lists.reverse(sentence)));
		}
		List<PhasePart> rpps = extractPart(suffixes, minFreq);
		for (PhasePart pp: rpps) {
			p2cnt.put(new Phase(Lists.reverse(pp.words)), pp.freq);
			if (pp.entropy >= minEntropy) {
				String phase = Phase.format(Lists.reverse(pp.words));
				phase2re.put(phase, pp.entropy);
			}
		}
		
		suffixes = new ArrayList<Suffix>();
		for (List<String> sentence: sentences) {
			suffixes.addAll(Suffix.create(sentence));
		}
		List<PhasePart> pps = extractPart(suffixes, minFreq);
		HashMap<String, Double> phase2e = new HashMap<String, Double>();
		for (PhasePart pp: pps) {
			p2cnt.put(new Phase(pp.words), pp.freq);
			if (pp.entropy >= minEntropy) phase2e.put(Phase.format(pp.words), pp.entropy);
		}
		
		List<WordFreq> keywords = new ArrayList<WordFreq>();
		for (PhasePart pp: pps) {
			if (pp.entropy > minEntropy) {
				String phase = Phase.format(pp.words);
				Double re = phase2re.get(phase);
				if (re != null && re >= minEntropy && WordUtil.hasLetter(phase)) {
					if (pp.words.size() == 1) {
						String word = pp.words.get(0);
						if (word.length() > 1 && !WordUtil.isStopword(word)) keywords.add(new WordFreq(word, pp.freq, 1));
					}
					if (pp.words.size() >= 2) {
						int f1 = getFreq(p2cnt, Phase.create(pp.words, 0, pp.words.size()-1), pp.freq);
						int f2 = getFreq(p2cnt, Phase.create(pp.words, pp.words.size()-1, pp.words.size()), pp.freq);
						int f3 = getFreq(p2cnt, Phase.create(pp.words, 0, 1), pp.freq);
						int f4 = getFreq(p2cnt, Phase.create(pp.words, 1, pp.words.size()), pp.freq);
						float mi1 = pp.freq * 1f / (f1 + f2 - pp.freq);
						float mi2 = pp.freq * 1f / (f3 +f4 - pp.freq);
						float mi = pp.freq * 1f / (f1 +f4 - pp.freq);
						Double subre = phase2re.get(Phase.format(pp.words.subList(1, pp.words.size())));
						if (subre == null) subre = 0d;
						Double sube = phase2e.get(Phase.format(pp.words.subList(0, pp.words.size() - 1)));
						if (sube == null) sube = 0d;
						if (mi1 >= 0.1 && mi2 >= 0.1 && subre * 2 <= re && sube * 2 <= pp.entropy && WordUtil.isUseful(phase)) {
							if (pp.words.size() > 2 && mi >= minMI || 
									pp.words.size() == 2 && mi >= 0.9 && pp.freq >= minFreq * 2) {
								keywords.add(new WordFreq(phase, pp.freq, Math.max(mi1, mi2)));
							}
						}
					}
				}
			} 
		}
		
		Collections.sort(keywords);
		return keywords;
	}
	
	private static void inc(Map<Phase, Integer> f2cnt, Phase p, int incValue) {
		Integer f = f2cnt.get(p);
		if (f == null) f = 0;
		f2cnt.put(p, f + incValue);
	}
	
	private static int getFreq(Map<Phase, Integer> f2cnt, Phase p, int def) {
		Integer f = f2cnt.get(p);
		if (f == null) f = def;
		return f;
	}
	
	private static List<PhasePart> extractPart(List<Suffix> suffixes, int minFreq) {
		Collections.sort(suffixes, new Suffix.SuffixCmp(6));
		
		List<PhaseIdx> pis = new ArrayList<PhaseIdx>();
		int[] freq = new int[] { 0, 0, 0, 0, 0};
		String[] ft = new String[] {"", "", "", "", ""};
		for (int k = 0; k < suffixes.size(); k++) {
			Suffix suffix = suffixes.get(k);
			List<String> terms = suffix.toWords();
			int i = 0;
			for (; i < freq.length && i < terms.size(); i++) {
				if (terms.get(i).equals(ft[i])) freq[i]++;
				else break;
			}
			for (;i < freq.length && i < terms.size(); i++) {
				if (freq[i] >= minFreq) {
					if (i > 0 || (ft[i].length() > 1 && !ExtractNNP.isKnownWord(ft[i]))) pis.add(new PhaseIdx(i+1, freq[i], k-1));
				}
				freq[i] = 1;
			}
			for (;i < freq.length; i++) {
				if (freq[i] >= minFreq) {
					if (i > 0 || (ft[i].length() > 1 && !ExtractNNP.isKnownWord(ft[i]))) pis.add(new PhaseIdx(i+1, freq[i], k-1));
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
				if (i > 0 || (ft[i].length() > 1 && !ExtractNNP.isKnownWord(ft[i]))) pis.add(new PhaseIdx(i+1, freq[i], suffixes.size()-1));
			}
			freq[i] = 0;
		}
		
		List<PhasePart> phases = new ArrayList<PhasePart>();
		
		for (PhaseIdx pi: pis) {
			int start = pi.idx - pi.freq + 1;
			List<Integer> nextFreqs = new ArrayList<Integer>();
			String prev = "";
			int nextFreq = 0;
			for (int k = start;  k <= pi.idx; k++) {
				List<String> terms = suffixes.get(k).toWords();
				String next = "$";
				if (pi.len < terms.size()) next = terms.get(pi.len);
				if (prev.equals(next)) nextFreq++;
				else {
					if (nextFreq > 0) nextFreqs.add(nextFreq);
					nextFreq = 1;
				}
				prev = next;
			}
			if (nextFreq > 0) nextFreqs.add(nextFreq);
			double entropy = 0;
			double f = pi.freq;
			for (int nf: nextFreqs) entropy += - nf * Math.log(nf / f);
			entropy = entropy / f;
			List<String> words = suffixes.get(pi.idx).toWords().subList(0, pi.len);
			phases.add(new PhasePart(words, pi.freq, entropy));
		}
		
		return phases;
	}
}

class PhaseIdx {
	int len;
	int freq;
	int idx;
	
	public PhaseIdx(int len, int freq, int idx) {
		this.len = len;
		this.freq = freq;
		this.idx = idx;
	}
}

class PhasePart {
	List<String> words;
	int freq;
	double entropy;
	
	public PhasePart(List<String> words, int freq, double entropy) {
		this.words = words;
		this.freq = freq;
		this.entropy = entropy;
	}

	@Override
	public String toString() {
		return Phase.format(words) + "(" + freq + ", " + entropy + ")";
	}
}
