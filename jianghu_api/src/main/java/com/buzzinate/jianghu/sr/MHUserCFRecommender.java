package com.buzzinate.jianghu.sr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mahout.math.function.LongDoubleProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;

import com.buzzinate.common.dao.UserMinhashDao;
import com.buzzinate.common.model.Minhash;
import com.buzzinate.common.model.UserMinhash;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.PriorityQueue.Entry;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.jianghu.jredis.PrefCache;
import com.buzzinate.jianghu.jredis.PrefData;
import com.google.common.collect.HashMultimap;

public class MHUserCFRecommender extends Recommender {
	private static Logger log = Logger.getLogger(MHUserCFRecommender.class);
	
	private UserMinhashDao umhDao;
	private PrefCache prefCache;
	
	public MHUserCFRecommender(UserMinhashDao umhDao, PrefCache prefCache, ItemFilter itemFilter, int weight) {
		super(itemFilter, weight);
		this.umhDao = umhDao;
		this.prefCache = prefCache;
	}

	@Override
	public void recommend(long userId, int howManyItem, Fuser fuser) {
		final long since = System.currentTimeMillis() - Constants.ONE_DAY * 10;
		
		UserMinhash myumh = umhDao.get(userId);
		if (myumh == null) return;
		
		HashMap<Integer, Integer> hash2size = new HashMap<Integer, Integer>();
		for (Minhash mh: myumh.getMinhashes()) hash2size.put(mh.getHash(), mh.getSize());
		
		List<PrefData> myPrefs = prefCache.fetchPrefIds(userId);
		final HashSet<Long> myprefset = new HashSet<Long>();
		for (PrefData pd: myPrefs) myprefset.add(pd.getId());
		
		List<UserMinhash> umhes = umhDao.createQuery().filter("minhashes.hash in", hash2size.keySet()).filter("lastModified >", since).asList();
		
		OpenLongDoubleHashMap userscore = new OpenLongDoubleHashMap();
		for (UserMinhash umh: umhes) {
			if (umh.getId() == userId) continue;
			for (Minhash mh: umh.getMinhashes()) {
				Integer size = hash2size.get(mh.getHash());
				if (size != null) {
					double w = 1 / Math.log(1 + mh.getSize() + size);
					// 1 + w^^4
					w = w * w;
					w = 1 + w * w;
					userscore.adjustOrPutValue(umh.getId(), w, w);
				}
			}
		}
		
		final PriorityQueue<Double, Long> upq = PriorityQueue.make(80, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		userscore.forEachPair(new LongDoubleProcedure(){
			@Override
			public boolean apply(long user, double score) {
				upq.add(score, user);
				return true;
			}
		});
		
		HashMap<Long, Double> user2sim = new HashMap<Long, Double>();
		for (Entry<Double, Long> e: upq.entries()) {
			double score = e.getKey();
			int cnt = (int)Math.floor(score);
			double sim = (score - cnt) * Math.log(1 + cnt);
			user2sim.put(e.getValue(), sim);
		}
		
		long start = System.currentTimeMillis();
		Map<Long, List<PrefData>> user2pages = new HashMap<Long, List<PrefData>>();
		for (long nid: user2sim.keySet()) user2pages.put(nid, prefCache.fetchPrefIds(nid));
		log.info("!!!!! batch cost: " + (System.currentTimeMillis() - start));
		
		final HashMultimap<Long, Long> item2users = HashMultimap.create();
		OpenLongDoubleHashMap item2score = new OpenLongDoubleHashMap();
		final Map<Long, Long> item2creates = new HashMap<Long, Long>();
		for (Map.Entry<Long, List<PrefData>> e: user2pages.entrySet()) {
			long user = e.getKey();
			double sim = user2sim.get(user);
			for (PrefData pd: e.getValue()) {
				item2users.put(pd.getId(), user);
				item2score.adjustOrPutValue(pd.getId(), sim, sim);
				item2creates.put(pd.getId(), pd.getCreateAt());
			}
		}
		
		final PriorityQueue<Double, Long> pq = PriorityQueue.make(howManyItem, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		item2score.forEachPair(new LongDoubleProcedure(){
			@Override
			public boolean apply(long item, double score) {
				long createAt = item2creates.get(item);
				if (!myprefset.contains(item) && createAt > since) {
					double newscore = score * Math.log(1 + item2users.get(item).size());
					pq.add(newscore, item);
				}
				return true;
			}
		});
		
		List<TopItem> items = new ArrayList<TopItem>();
		if (pq.size() > 0) {
			List<Long> ids = itemFilter.filterOldItem(userId, pq.values(), myprefset);
			
			for (Entry<Double, Long> t: pq.entries()) {
				if (ids.contains(t.getValue())) {
					long createAt = item2creates.get(t.getValue());
					List<Long> neighbors = new ArrayList<Long>(item2users.get(t.getValue()));
					items.add(new TopItem(t.getValue(), t.getKey(), -1, createAt, Algorithm.UserCF, TopItem.asStr(neighbors, "user")));
				}
			}
		}
		
		fuser.add(items, getWeight());
	}
}