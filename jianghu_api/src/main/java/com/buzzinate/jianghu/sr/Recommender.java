package com.buzzinate.jianghu.sr;

import java.util.List;

public abstract class Recommender {
	protected ItemFilter itemFilter;
	protected int weight;
	
	protected Recommender(ItemFilter itemFilter, int weight) {
		this.itemFilter = itemFilter;
		this.weight = weight;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public abstract void recommend(long userId, int howManyItem, Fuser fuser);
	
	public List<Long> filterOldItem(long userId, List<Long> itemIds) {
		return itemFilter.filterOldItem(userId, itemIds);
	}
}