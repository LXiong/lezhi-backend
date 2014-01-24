package org.knowceans.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Counter<K> {
	private HashMap<K, Integer> cnts = new HashMap<K, Integer>();
	private int maxCnt = 0;
	
	public void add(K k) {
		Integer v = cnts.get(k);
		if (v == null) v = 1;
		else v = v + 1;
		if (v > maxCnt) maxCnt = v;
		cnts.put(k, v);
	}
	
	public void inc(K k, int value) {
		Integer v = cnts.get(k);
		if (v == null) v = value;
		else v = v + value;
		if (v > maxCnt) maxCnt = v;
		cnts.put(k, v);
	}
	
	public void addAll(Collection<K> ks) {
		for (K k: ks) add(k);
	}
	
	public HashMap<K, Integer> toMap() {
		return cnts;
	}
	
	public int getMaxCnt() {
		return maxCnt;
	}
	
	public List<K> unfreq(int maxFreq) {
		List<K> result = new ArrayList<K>();
		for (Map.Entry<K, Integer> e: cnts.entrySet()) {
			if (e.getValue() <= maxFreq) result.add(e.getKey());
		}
		return result;
	}
	
	public List<K> freq(int minFreq) {
		List<K> result = new ArrayList<K>();
		for (Map.Entry<K, Integer> e: cnts.entrySet()) {
			if (e.getValue() >= minFreq) result.add(e.getKey());
		}
		return result;
	}
}
