package org.knowceans.lda.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentBuilder {
	private List<Integer> terms = new ArrayList<Integer>();
	
	public void add(Integer term) {
		terms.add(term);
	}
	
	public void addAll(List<Integer> ts) {
		terms.addAll(ts);
	}
	
	public Document build() {
		HashMap<Integer, Integer> termCnts = new HashMap<Integer, Integer>();
		for (Integer term: terms) {
			Integer cnt = termCnts.get(term);
			if (cnt == null) cnt = 0;
			cnt++;
			termCnts.put(term, cnt);
		}
		int[] words = new int[termCnts.size()];
		int[] counts = new int[termCnts.size()];
		
		int index = 0;
		for (Map.Entry<Integer, Integer> e: termCnts.entrySet()) {
			words[index] = e.getKey();
			counts[index] = e.getValue();
			index++;
		}
		
		return new Document(words, counts, terms.size());
	}
}