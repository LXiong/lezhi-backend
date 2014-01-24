package com.buzzinate.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DCounter<K> {
	private HashMap<K, Double> cnts = new HashMap<K, Double>();
	
	public void inc(K k, double delta) {
		Double v = cnts.get(k);
		if (v == null) v = delta;
		else v = v + delta;
		cnts.put(k, v);
	}
	
	public double get(K k, double def) {
		Double v = cnts.get(k);
		if (v == null) return def;
		else return v;
	}
	
	public List<K> top(int max) {
		List<K> result = new ArrayList<K>();
		List<Double> vs = new ArrayList<Double>(cnts.values());
		if (vs.size() < max) return new ArrayList<K>(cnts.keySet());
		Collections.sort(vs);
		double threshold = vs.get(vs.size() - max);
		for (Map.Entry<K, Double> e: cnts.entrySet()) {
			if (e.getValue() >= threshold) result.add(e.getKey());
		}
		return result;
	}
	
	public HashMap<K, Double> toMap() {
		return cnts;
	}
}
