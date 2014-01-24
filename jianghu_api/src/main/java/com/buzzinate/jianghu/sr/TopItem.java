package com.buzzinate.jianghu.sr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class TopItem {
	protected long itemId;
	protected double score;
	protected int clusterId;
	protected long createAt;
	protected Algorithm algorithme;
	protected List<String> features = new ArrayList<String>();
	
	public TopItem(long itemId, double score, int clusterId, long createAt, Algorithm algo, List<String> features) {
		this.itemId = itemId;
		this.score = score;
		this.clusterId = clusterId;
		this.createAt = createAt;
		this.algorithme = algo;
		this.features = features;
	}
	
	public long getItemId() {
		return itemId;
	}

	public double getScore() {
		return score;
	}
	
	public int getClusterId() {
		return clusterId;
	}

	public long getCreateAt() {
		return createAt;
	}

	public Algorithm getAlgorithme() {
		return algorithme;
	}

	public List<String> getFeatures() {
		return features;
	}

	@Override
	public String toString() {
		return itemId + " #" + algorithme + ",score=" + score + ", cluster=" + clusterId + ",createAt=" + createAt + ", features=" + features;
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