package com.buzzinate.jianghu.sr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mahout.math.function.LongDoubleProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenLongObjectHashMap;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;

import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserProfileDao;
import com.buzzinate.common.model.KeywordFeature;
import com.buzzinate.common.model.UserFeature;
import com.buzzinate.common.model.UserProfile;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.PriorityQueue.Entry;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.jianghu.jredis.PrefCache;
import com.buzzinate.jianghu.jredis.PrefData;
import com.google.common.collect.HashMultimap;

public class KNNRecommender extends Recommender {
	private static Logger log = Logger.getLogger(KNNRecommender.class);
	
	private UserProfileDao udpDao;
	private PrefCache prefCache;
	
	public KNNRecommender(PrefCache prefCache, PreferenceDao prefDao, ItemFilter itemFilter, int weight) {
		super(itemFilter, weight);
		this.prefCache = prefCache;
		this.udpDao = new UserProfileDao(prefDao.getDatastore());
	}
	
	@Override
	public void recommend(long userId, int howManyItem, Fuser fuser) {
		final long since = System.currentTimeMillis() - Constants.ONE_DAY * 10;
		UserProfile up = udpDao.get(userId);
		if (up == null) return;
		
		OpenLongDoubleHashMap neighbor2sim = new OpenLongDoubleHashMap();
		for (UserFeature uf: up.getUserFeatures()) {
			neighbor2sim.adjustOrPutValue(uf.getUserId(), uf.getResource(), uf.getResource());
		}
		OpenObjectDoubleHashMap<String> word2sim = new OpenObjectDoubleHashMap<String>();
		for (KeywordFeature kf: up.getKeywordFeatures()) {
			word2sim.adjustOrPutValue(kf.getKeyword(), kf.getResource(), kf.getResource());
		}
		
		List<PrefData> myPrefs = prefCache.fetchPrefIds(userId);
		final HashSet<Long> myPrefSet = new HashSet<Long>();
		for (PrefData pd: myPrefs) myPrefSet.add(pd.getId());
		
		long start = System.currentTimeMillis();
		Map<Long, List<PrefData>> user2pages = new HashMap<Long, List<PrefData>>();
		for (long nid: neighbor2sim.keys().toList()) user2pages.put(nid, prefCache.fetchPrefIds(nid));
		log.info("!!!!! batch cost: " + (System.currentTimeMillis() - start));
		
		OpenLongObjectHashMap<List<Long>> id2neighbors = new OpenLongObjectHashMap<List<Long>>(); 
		OpenLongDoubleHashMap id2score = new OpenLongDoubleHashMap();
		final OpenLongDoubleHashMap id2ws = new OpenLongDoubleHashMap();
		final HashMap<Long, Long> id2creates = new HashMap<Long, Long>();
		HashMultimap<Long, String> id2words = HashMultimap.create();
		for (Map.Entry<Long, List<PrefData>> e: user2pages.entrySet()) {
			double sim = neighbor2sim.get(e.getKey());
			HashSet<Long> pageIds = new HashSet<Long>();
			for (PrefData pd: e.getValue()) {
				if (pageIds.contains(pd.getId())) continue;
				pageIds.add(pd.getId());
				for (String word: pd.getWords()) {
					double ws = word2sim.get(word) / 5;
					if (ws > 0) {
						id2ws.put(pd.getId(), ws);
						id2words.put(pd.getId(), word);
					}
				}
				id2score.adjustOrPutValue(pd.getId(), sim, sim);
				id2creates.put(pd.getId(), pd.getCreateAt());
				List<Long> neighbors = id2neighbors.get(pd.getId());
				if (neighbors == null) {
					neighbors = new ArrayList<Long>();
					id2neighbors.put(pd.getId(), neighbors);
				}
				neighbors.add(e.getKey());
			}
		}
		
		final PriorityQueue<Double, Long> pq = PriorityQueue.make(howManyItem, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		id2score.forEachPair(new LongDoubleProcedure() {
			@Override
			public boolean apply(long item, double score) {
				boolean prefed = myPrefSet.contains(item);
				long createAt = id2creates.get(item);
				if (!prefed && createAt > since) {
					pq.add(score + id2ws.get(item), item);
				}
				return true;
			}
		});
		
		List<TopItem> items = new ArrayList<TopItem>();
		if (pq.size() > 0) {
			List<Long> ids = itemFilter.filterOldItem(userId, pq.values(), myPrefSet);
			
			for (Entry<Double, Long> t: pq.entries()) {
				if (ids.contains(t.getValue())) {
					long createAt = id2creates.get(t.getValue());
					List<String> features = TopItem.asStr(id2neighbors.get(t.getValue()), "user");
					features.addAll(TopItem.asStr(id2words.get(t.getValue()), "keyword"));
					items.add(new TopItem(t.getValue(), t.getKey(), -1, createAt, Algorithm.CF, features));
				}
			}
		}
		fuser.add(items, getWeight());
	}
	
	public void updateCache(long now) {
		// NO OP!
	}
}