package com.buzzinate.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.model.FetchInfo;
import com.google.code.morphia.Datastore;

public class FetchInfoDao extends BaseDaoDefault<FetchInfo, String> {

	public FetchInfoDao(Datastore ds) {
		super(ds);
	}
	
	public List<FetchInfo> findTop(int topN, long maxFetchTime) {
		return createQuery().filter("lastFetchTime <", maxFetchTime).order("-score, lastFetchTime").limit(topN).asList();
	}
	
	public void batchUpdate(List<FetchInfo> fis) {
		List<String> uids = new ArrayList<String>();
		for (FetchInfo fi: fis) uids.add(fi.uid);
		Map<String, FetchInfo> uid2fs = map2(uids, "lastFetchTime", "lastStatusId");
		for (FetchInfo fi: fis) {
			FetchInfo oldfi = uid2fs.get(fi.uid);
			if (oldfi != null) {
				fi.lastFetchTime = oldfi.lastFetchTime;
				fi.lastStatusId = oldfi.lastStatusId;
			}
		}
		ds.save(fis);
	}
}