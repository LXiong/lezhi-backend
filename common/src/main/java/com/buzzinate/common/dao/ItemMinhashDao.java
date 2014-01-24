package com.buzzinate.common.dao;

import com.buzzinate.common.model.ItemMinhash;
import com.google.code.morphia.Datastore;

public class ItemMinhashDao extends BaseDaoDefault<ItemMinhash, Long> implements BaseDao<ItemMinhash, Long> {

	public ItemMinhashDao(Datastore ds) {
		super(ds);
	}
}