package com.buzzinate.common.dao;

import java.util.HashSet;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.model.BlackSite;
import com.google.code.morphia.Datastore;

public class BlackSiteDao extends BaseDaoDefault<BlackSite, String> {

	public BlackSiteDao(Datastore ds) {
		super(ds);
	}
	
	public HashSet<String> getVerifiedBlackSites() {
		return new HashSet<String>(getIds(createQuery().filter("status", BlackSite.Status.Verified)));
	}
}
