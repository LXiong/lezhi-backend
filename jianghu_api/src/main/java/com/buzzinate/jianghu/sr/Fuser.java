package com.buzzinate.jianghu.sr;

import static com.buzzinate.common.util.Constants.BASE_RANK_TIME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.mahout.math.set.OpenLongHashSet;

import com.buzzinate.common.dao.ArticleProfileDao;
import com.buzzinate.jianghu.dedup.MinHashDeduper;

public class Fuser {
	private ArticleProfileDao apDao;
	private List<Group> groups = new ArrayList<Group>();
	
	public Fuser(ArticleProfileDao apDao) {
		this.apDao = apDao;
	}

	public void add(List<TopItem> items, int weight) {
		groups.add(new Group(fuseTime(items), weight));
	}
	
	public List<TopItem> fuse(int howMany) {
		return fuse(howMany, groups);
	}
	
	public List<TopItem> fuse(int howMany, List<Group> groups) {	
		List<TopItem> topItems = new ArrayList<TopItem>();
		
		int topn = howMany * 5 / 4;
		int[] gi = new int[groups.size()];
		OpenLongHashSet ids = new OpenLongHashSet(topn);
		while (ids.size() < topn) {
			int ni = -1;
			double min = Double.MAX_VALUE;
			for (int i = 0; i < groups.size(); i++) {
				Group g = groups.get(i);
				if (gi[i] >= g.items.size()) continue;
				double n = gi[i] * 1d / g.weight;
				if (min > n) {
					min = n;
					ni = i;
				}
			}
			if (ni >= 0) {
				TopItem item = groups.get(ni).items.get(gi[ni]);
				if (!ids.contains(item.getItemId())) {
					topItems.add(item);
				}
				ids.add(item.getItemId());
				gi[ni]++;
			} else break;
		}
		
		topItems = dedup(topItems);
		
		HashSet<Long> allids = new HashSet<Long>();
		List<TopItem> result = new ArrayList<TopItem>();
		for (TopItem item: topItems) {
			if (!allids.contains(item.getItemId())) result.add(item);
			allids.add(item.getItemId());
		}
		
		return result;
	}
	
	private List<TopItem> dedup(List<TopItem> topItems) {
		List<Long> ids = new ArrayList<Long>();
		for (TopItem item: topItems) ids.add(item.getItemId());
		Map<Long, List<Integer>> id2mhes = apDao.mapField(ids, "minhashes");
		MinHashDeduper mhd = new MinHashDeduper();
		
		List<TopItem> result = new ArrayList<TopItem>();
		List<TopItem> tail = new ArrayList<TopItem>();
		for (TopItem item: topItems) {
			List<Integer> mhes = id2mhes.get(item.getItemId());
			double sim = mhd.checkMaxJaccard(mhes);
			mhd.add(mhes);
			if (sim < 0.5) result.add(item);
			else tail.add(item);
		}
		result.addAll(tail);
		
		return result;
	}

	private static List<TopItem> fuseTime(List<TopItem> items) {
		List<TopItem> result = new ArrayList<TopItem>();
		for (TopItem item: items) {
			item.score = item.score * (1 + (item.createAt - BASE_RANK_TIME) / 45000000f);
			result.add(item);
		}
		Collections.sort(result, new TopItem.Comp());
		return result;
	}
	
	public List<TopItem> fuse(Algorithm algorithm, int howManyItem) {
		List<TopItem> topItems = new ArrayList<TopItem>();
		for (Group g: groups) {
			for (TopItem item: g.items) {
				if (item.getAlgorithme() == algorithm) topItems.add(item);
			}
		}
		return topItems;
	}
	
	public static Group group(List<TopItem> items, int weight) {
		return new Group(items, weight);
	}
	
	public static class Group {
		private List<TopItem> items;
		private int weight;
		
		public Group(List<TopItem> items, int weight) {
			this.items = items;
			this.weight = weight;
		}

		public List<TopItem> getItems() {
			return items;
		}

		public int getWeight() {
			return weight;
		}
	}
}