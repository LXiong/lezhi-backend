package com.buzzinate.jianghu.dedup;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.buzzinate.crawl.core.util.Counter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class MinHashDeduper {
	private int idx = 0;
	private Multimap<Integer, Integer> mh2idxs = HashMultimap.create();
	
	public void add(List<Integer> mhes) {
		if (mhes == null) return;
		for (Integer mh: mhes) mh2idxs.put(mh, idx);
		idx += 1;
	}
	
	public double checkMaxJaccard(List<Integer> mhes) {
		if (mhes == null) return 0;
		Counter<Integer> idxCnt = new Counter<Integer>();
		for (Integer mh: mhes) {
			for (Integer idx: mh2idxs.get(mh)) {
				idxCnt.add(idx);
			}
		}
		int max = 0;
		for (Map.Entry<Integer, Integer> e: idxCnt.toMap().entrySet()) {
			if (e.getValue() > max) max = e.getValue();
		}
		return max / Math.max(mhes.size(), 1d);
	}

	public static void main(String[] args) {
		MinHashDeduper mhd = new MinHashDeduper();
		mhd.add(Arrays.asList(0, 1, 2));
		mhd.add(Arrays.asList(0, 2, 3));
		System.out.println(mhd.checkMaxJaccard(Arrays.asList(1, 2, 3)));
	}
}