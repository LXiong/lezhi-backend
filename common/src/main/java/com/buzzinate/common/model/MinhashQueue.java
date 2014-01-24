package com.buzzinate.common.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MinhashQueue {
	private int max;
	private TreeMap<Integer, Integer> hash2size = new TreeMap<Integer, Integer>();
	
	public MinhashQueue(int max) {
		this.max = max;
	}
	
	public void add(Minhash mh) {
		Integer oldSize = hash2size.get(mh.getHash());
		if (oldSize == null || oldSize < mh.getSize()) hash2size.put(mh.getHash(), mh.getSize());
		if (hash2size.size() > max) hash2size.pollLastEntry();
	}
	
	public List<Minhash> values() {
		List<Minhash> result = new ArrayList<Minhash>();
		for (Map.Entry<Integer, Integer> e: hash2size.entrySet()) {
			result.add(new Minhash(e.getKey(), e.getValue()));
		}
		return result;
	}

	public static void main(String[] args) {
		MinhashQueue mq = new MinhashQueue(3);
		List<Minhash> mhes1 = Arrays.asList(new Minhash(1, 2), new Minhash(2, 5), new Minhash(4, 8));
		List<Minhash> mhes2 = Arrays.asList(new Minhash(2, 4), new Minhash(3, 6));
		for (Minhash mh: mhes1) mq.add(mh);
		for (Minhash mh: mhes2) mq.add(mh);
		for (Minhash mh: mhes1) mq.add(mh);
		System.out.println(mq.values());
	}

}