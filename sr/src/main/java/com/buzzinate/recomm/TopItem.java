package com.buzzinate.recomm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class TopItem {
	protected long itemId;
	protected double score;
	protected long createAt;
	protected String algo;
	protected List<String> features = new ArrayList<String>();
	
	public TopItem(long itemId, double score, long createAt, String algo, List<String> features) {
		this.itemId = itemId;
		this.score = score;
		this.createAt = createAt;
		this.algo = algo;
		this.features = features;
	}
	
	public long getItemId() {
		return itemId;
	}

	public double getScore() {
		return score;
	}

	public long getCreateAt() {
		return createAt;
	}

	public String getAlgorithme() {
		return algo;
	}

	public List<String> getFeatures() {
		return features;
	}

	@Override
	public String toString() {
		return itemId + " #" + algo + ",score=" + score + ",createAt=" + createAt + ", features=" + features;
	}

	public static class Comp implements Comparator<TopItem> {
		@Override
		public int compare(TopItem item1, TopItem item2) {
			return - Double.compare(item1.score, item2.score);
		}
	}
	
	public static <T> List<String> asStr(Collection<T> features, String prefix) {
		List<String> list = new ArrayList<String>();
		for (T f: features) list.add(prefix + "-" + f);
		return list;
	}
}