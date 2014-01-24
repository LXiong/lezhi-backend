package com.buzzinate.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeywordCluster {
	
	public static void main(String[] args) {
		
	}
	
	public static VDoc[] cluster(List<VDoc> docs, int maxword, int maxcluster) {
		HashMap<String, Integer> word2idx = new HashMap<String, Integer>();
		for (VDoc d: docs) {
			for (WordFreq wf: d.wordfreqs) encode(word2idx, wf.word);
		}
		
		int n = word2idx.size();
		String[] words = new String[n];
		for (Map.Entry<String, Integer> e: word2idx.entrySet()) words[e.getValue()] = e.getKey();
		
		double[][] g = new double[n][n];
		int[] freqs = new int[n];
		for (VDoc d: docs) {
			for (int i = 0; i < d.wordfreqs.size(); i++) {
				WordFreq wfi = d.wordfreqs.get(i);
				int wi = encode(word2idx, wfi.word);
				freqs[wi] += wfi.freq;
				for (int j = 0; j < d.wordfreqs.size(); j++) {
					WordFreq wfj = d.wordfreqs.get(j);
					int wj = encode(word2idx, wfj.word);
					double w = Math.log(1 + wfi.freq) + Math.log(1 + wfj.freq);
					g[wi][wj] += w;
					g[wj][wi] += w;
				}
			}
		}
		
		boolean[] used = new boolean[n];
		Arrays.fill(used, false);
		VDoc[] clusters = new VDoc[maxcluster];
		for (int k = 0; k < maxcluster; k++) {
			int maxfreq = 0;
			int maxc = -1;
			for (int i = 0; i < n; i++) {
				if (used[i]) continue;
				if (freqs[i] > maxfreq) {
					maxfreq = freqs[i];
					maxc = i;
				}
			}
			if (maxc != -1) {
				int[] res = mincut(g, used, maxc, maxword);
				for (int r: res) used[r] = true;
				List<WordFreq> wfs = new ArrayList<WordFreq>();
				for (int r: res) {
					wfs.add(new WordFreq(words[r], freqs[r]));
				}
				clusters[k] = new VDoc(wfs);
			}
		}
		
		return clusters;
	}
	
	public static int[] mincut(double[][] g, boolean[] used, int p, int max) {
		int[] res = new int[max];
		int n = g.length;
				
		boolean[] mark = new boolean[n];
		Arrays.fill(mark, false);
		mark[p] = true;
		
		double[] dist = new double[n];
		for (int i = 0; i < dist.length; i++) {
			dist[i] = 0;
			for (int j = 0; j < dist.length; j++) dist[i] += g[i][j];
		}
		
		int k = 0;
		res[k++] = p;
		while (k < max) {
			double min = Double.MAX_VALUE;
			int mini = -1;
			for (int i = 0; i < n; i++) {
				if (mark[i] || used[i]) continue;
				double nc = dist[p] + dist[i] - g[p][i] * 2;
				if (nc < min) {
					min = nc;
					mini = i;
				}
			}
			if (mini != -1) {
				res[k++] = mini;
				// update dist[p]
				dist[p] = min;
				
				// remove mini
				mark[mini] = true;
				
				// update g
				for (int i = 0; i < n; i++) {
					if (mark[i]) continue;
					g[i][p] += g[i][mini];
					g[p][i] = g[i][p];
				}
				
			}
		}
		return res;
	}
	
	private static int encode(Map<String, Integer> word2idx, String word) {
		Integer idx = word2idx.get(word);
		if (idx == null) {
			idx = word2idx.size();
			word2idx.put(word, idx);
		}
		return idx;
	}
	
	public static class WordFreq {
		public String word;
		public int freq;
		
		public WordFreq(String word, int freq) {
			this.word = word;
			this.freq = freq;
		}

		@Override
		public String toString() {
			return word + "(" + freq + ")";
		}
	}
	
	public static class VDoc {
		public List<WordFreq> wordfreqs;
		
		public VDoc(List<WordFreq> wordfreqs) {
			this.wordfreqs = wordfreqs;
		}
	}
}