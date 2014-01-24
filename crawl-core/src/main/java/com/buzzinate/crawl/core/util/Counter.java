package com.buzzinate.crawl.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Counter<K> {
	private HashMap<K, Integer> cnts = new HashMap<K, Integer>();
	
	public void add(K k) {
		Integer v = cnts.get(k);
		if (v == null) v = 1;
		else v = v + 1;
		cnts.put(k, v);
	}
	
	public void inc(K k, int value) {
		Integer v = cnts.get(k);
		if (v == null) v = value;
		else v = v + value;
		cnts.put(k, v);
	}
	
	public void addAll(Collection<K> ks) {
		for (K k: ks) add(k);
	}
	
	public HashMap<K, Integer> toMap() {
		return cnts;
	}
	
	public List<Map.Entry<K, Integer>> sortDescending() {
		List<Map.Entry<K, Integer>> result = new ArrayList<Map.Entry<K, Integer>>();
		for (Map.Entry<K, Integer> e: cnts.entrySet()) result.add(e);
		Collections.sort(result, new Comparator<Map.Entry<K, Integer>>() {
			@Override
			public int compare(Entry<K, Integer> e1, Entry<K, Integer> e2) {
				return - e1.getValue().compareTo(e2.getValue());
			}
		});
		return result;
	}
	
	public List<K> top(int max) {
		List<K> result = new ArrayList<K>();
		List<Integer> vs = new ArrayList<Integer>(cnts.values());
		if (vs.size() < max) return new ArrayList<K>(cnts.keySet());
		Collections.sort(vs);
		int threshold = vs.get(vs.size() - max);
		for (Map.Entry<K, Integer> e: cnts.entrySet()) {
			if (e.getValue() >= threshold) result.add(e.getKey());
		}
		return result;
	}
	
	public List<K> freqItems(float ratio) {
		return freqItems(ratio, 1);
	}
	
	public List<K> freqItems(int min) {
		List<K> result = new ArrayList<K>();
		for (Map.Entry<K, Integer> e: cnts.entrySet()) {
			if (e.getValue() >= min) result.add(e.getKey());
		}
		return result;
	}
	
	public List<K> freqItems(float ratio, int min) {
		List<K> result = new ArrayList<K>();
		if (cnts.isEmpty()) return result;
		
		int max = Collections.max(cnts.values());
		for (Map.Entry<K, Integer> e: cnts.entrySet()) {
			if (e.getValue() >= max * ratio && e.getValue() >= min) result.add(e.getKey());
		}
		return result;
	}
}
