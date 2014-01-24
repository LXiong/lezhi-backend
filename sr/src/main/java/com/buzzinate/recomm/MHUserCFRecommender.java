package com.buzzinate.recomm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.math.function.LongDoubleProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenLongIntHashMap;
import org.bson.types.ObjectId;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.UserMinhashDao;
import com.buzzinate.common.model.Minhash;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.model.UserMinhash;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.PriorityQueue.Entry;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.model.Read;
import com.google.common.collect.HashMultimap;

public class MHUserCFRecommender {
	private static final int K = 80;
	private PreferenceDao prefDao;
	private UserMinhashDao umhDao;
	private UserDao userDao;
	private ArticleDao articleDao;
	
	public MHUserCFRecommender(PreferenceDao prefDao, UserMinhashDao umhDao, UserDao userDao, ArticleDao articleDao) {
		this.prefDao = prefDao;
		this.umhDao = umhDao;
		this.userDao = userDao;
		this.articleDao = articleDao;
	}

	public void recommend(long userId, int howManyItem, double beta, double alpha) {
		final long since = System.currentTimeMillis() - Constants.ONE_DAY * 10;
		
		List<Long> myprefs = prefDao.getItemsFromUserSince(userId, since);
		final HashSet<Long> myprefset = new HashSet<Long>(myprefs);
		
		UserMinhash myumh = umhDao.get(userId);
		HashMap<Integer, Integer> hash2size = new HashMap<Integer, Integer>();
		for (Minhash mh: myumh.getMinhashes()) hash2size.put(mh.getHash(), mh.getSize());
		
		List<UserMinhash> umhes = umhDao.createQuery().filter("minhashes.hash in", hash2size.keySet()).filter("lastModified > ", since).asList();
		
		OpenLongDoubleHashMap usersim = new OpenLongDoubleHashMap();
		for (UserMinhash umh: umhes) {
			if (umh.getId() == userId) continue;
			for (Minhash mh: umh.getMinhashes()) {
				Integer size = hash2size.get(mh.getHash());
				if (size != null) {
					double w = 1 / Math.log(1 + mh.getSize() + size);
					w = 1 + Math.pow(w, beta);
					usersim.adjustOrPutValue(umh.getId(), w, w);
				}
			}
		}
		
		final PriorityQueue<Double, Long> upq = PriorityQueue.make(K, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		usersim.forEachPair(new LongDoubleProcedure(){
			@Override
			public boolean apply(long user, double score) {
				upq.add(score, user);
				return true;
			}
		});
		
		printNeighbors("raw: size + 1 / ni", upq);
		
//		Map<Long, Integer> user2prefsize = userDao.mapField(upq2.values(), "prefSize");
//		final PriorityQueue<Double, Long> upq = PriorityQueue.make(K, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
//		for (Entry<Double, Long> su: upq2.entries()) {
//			double sim = su.getKey();
//			long u = su.getValue();
//			int prefsize = user2prefsize.get(u);
//			upq.add(sim / Math.sqrt(1 + prefsize), u);
//		}
//		
		HashMap<Long, Double> user2sim = new HashMap<Long, Double>();
		for (Entry<Double, Long> e: upq.entries()) user2sim.put(e.getValue(), e.getKey());
//		
//		printNeighbors("(size + 1 / ni) / sqrt(nu)", upq);
		
		List<Preference> prefs = prefDao.createQuery().filter("userId in", user2sim.keySet()).filter("createAt > ", since).asList();
		final HashMultimap<Long, Long> item2users = HashMultimap.create();
		OpenLongDoubleHashMap item2weight = new OpenLongDoubleHashMap();
		for (Preference pref: prefs) {
			item2users.put(pref.getPageId(), pref.getUserId());
			double sim = user2sim.get(pref.getUserId());
			int cnt = (int)Math.floor(sim);
			sim = (sim - cnt) * Math.log(1 + cnt);
			item2weight.adjustOrPutValue(pref.getPageId(), sim, sim);
		}
		
		final PriorityQueue<Double, Long> pq = PriorityQueue.make(howManyItem, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		item2weight.forEachPair(new LongDoubleProcedure(){
			@Override
			public boolean apply(long item, double score) {
				if (!myprefset.contains(item)) {
					double newscore = score * Math.log(1 + item2users.get(item).size());
					pq.add(newscore, item);
				}
				return true;
			}
		});
		
//		HashMap<Long, List<Minhash>> user2mhes = new HashMap<Long, List<Minhash>>();
//		for (UserMinhash umh: umhes) {
//			user2mhes.put(umh.getId(), umh.getMinhashes());
//		}
//		final PriorityQueue<Double, Long> pq = PriorityQueue.make(howManyItem, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
//		for (Entry<Double, Long> t: pq2.entries()) {
//			long itemId = t.getValue();
//			double score = t.getKey();
//			List<Long> users = new ArrayList<Long>(item2users.get(itemId));
//			double sum = 0;
//			for (int i = 0; i < users.size(); i++) {
//				for (int j = i + 1; j < users.size(); j++) {
//					sum += simuv(user2mhes, beta, users.get(i), users.get(j));
//				}
//			}
//			pq.add(score - sum * alpha / Math.pow(users.size(), 1.5), itemId);
//		}
		
		Map<Long, Long> item2creates = articleDao.mapField(pq.values(), "createAt");
		List<TopItem> items = new ArrayList<TopItem>();
		if (pq.size() > 0) {
			List<Long> ids = filterOldItem(userId, pq.values(), myprefset);
			
			for (Entry<Double, Long> t: pq.entries()) {
				if (ids.contains(t.getValue())) {
					long createAt = item2creates.get(t.getValue());
					List<Long> neighbors = new ArrayList<Long>(item2users.get(t.getValue()));
					items.add(new TopItem(t.getValue(), t.getKey(), createAt, "ItemCF", TopItem.asStr(neighbors, "user")));
				}
			}
		}
		
		System.out.println();
		System.out.println("########################################");
		List<Long> ids = new ArrayList<Long>();
		for (TopItem item: items) ids.add(item.getItemId());
		Map<Long, String> item2title = articleDao.mapField(ids, "title");
		for (TopItem item: items) System.out.println(item2title.get(item.getItemId()) + " ==> " + item);
	}

	private void printNeighbors(String prefix, PriorityQueue<Double, Long> upq) {
		Map<Long, String> user2name = userDao.mapField(upq.values(), "name");
		System.out.println(prefix + " neighbors: ");
		for (Entry<Double, Long> e: upq.entries()) {
			System.out.println(e.getValue() + "(" + user2name.get(e.getValue()) + ") => " + e.getKey());
		}
	}

	private double simuv(HashMap<Long, List<Minhash>> user2mhes, double beta, long u, long v) {
		HashMap<Integer, Integer> mh2size4u = new HashMap<Integer, Integer>();
		for (Minhash mh: user2mhes.get(u)) {
			mh2size4u.put(mh.getHash(), mh.getSize());
		}
		double sim = 0;
		for (Minhash mh: user2mhes.get(v)) {
			Integer usize = mh2size4u.get(mh.getHash());
			if (usize != null) {
				double w = 1 / Math.log(1 + mh.getSize() + usize);
				sim  += Math.pow(w, beta);
			}
		}
		return sim;
	}

	private List<Long> filterOldItem(long userId, List<Long> itemIds, Set<Long> prefIds) {
		List<Long> result = new ArrayList<Long>(itemIds);
		
		if (result.size() > 0) {
		    // user doesn't read
			HashSet<Long> readIds = new HashSet<Long>(findReadIds(userId, result));
		    result.removeAll(readIds);
		}
	    result.removeAll(prefIds);
	    
	    return result;
	}
	
	private List<Long> findReadIds(long userId, List<Long> possibleIds) {
		List<Long> articleIds = new ArrayList<Long>();
		if (possibleIds.isEmpty()) return articleIds;
		
		BaseDaoDefault<Read, ObjectId> readDao = new BaseDaoDefault<Read, ObjectId>(Read.class, prefDao.getDatastore());
		List<Read> reads = readDao.createQuery().filter("userId", userId).filter("articleId in", possibleIds).asList();
		for (Read read: reads) articleIds.add(read.getArticleId());
		return articleIds;
	}
}
