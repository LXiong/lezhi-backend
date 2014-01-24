package com.buzzinate.jianghu.sr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.mahout.common.Pair;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ItemMinhashDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.model.ItemMinhash;
import com.buzzinate.common.model.Minhash;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.jianghu.sr.cache.JCache;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;

public class MHItemCFRecommender extends Recommender {
	private JCache jcache = null;
	private PreferenceDao prefDao = null;
	private ArticleDao articleDao = null;
	private ItemMinhashDao imhDao = null;
	
	public MHItemCFRecommender(JCache jcache, PreferenceDao prefDao, ArticleDao articleDao, ItemMinhashDao imhDao, ItemFilter itemFilter, int weight) {
		super(itemFilter, weight);
		this.jcache = jcache;
		this.prefDao = prefDao;
		this.articleDao = articleDao;
		this.imhDao = imhDao;
	}

	@Override
	public void recommend(long userId, int howManyItem, Fuser fuser) {
		List<Long> myprefids = prefDao.findLatestItems(userId, howManyItem);
		Map<Long, Integer> id2prefsize = articleDao.mapField(myprefids, "prefSize");
		int minprefsize = Integer.MAX_VALUE; // use minprefsize as user's prefsize
		PriorityQueue<Integer, Long> sizepq = PriorityQueue.make(howManyItem / 2, SortUtils.comp(Integer.class), SortUtils.reverse(Long.class));
		for (Map.Entry<Long, Integer> e : id2prefsize.entrySet()) {
			sizepq.add(e.getValue(), e.getKey());
			if (minprefsize > e.getValue()) minprefsize = e.getValue(); 
		}
		
		Map<Long, ItemMinhash> mypref2mhes = imhDao.map2(sizepq.values(), "minhashes");
		
		HashMap<Long, Long> id2createAts = new HashMap<Long, Long>(); 
		HashMultimap<Long, Pair<Long, Double>> item2scores = HashMultimap.create();
		for (final Map.Entry<Long, ItemMinhash> e: mypref2mhes.entrySet()) {
			long mypref = e.getKey();
			List<ItemScore> topn = jcache.asyncs("mh-item-topn-", mypref, ItemScore.class, Constants.ONE_HOUR * 10, new Function<Long, List<ItemScore>>(){
				@Override
				public List<ItemScore> apply(Long key) {
					return recommend4item(e.getValue().getMinhashes(), 20);
				}
			});
			
			double maxscore = maxScore(topn);
			double logp = 1 / Math.log(2 + id2prefsize.get(mypref));
			for (ItemScore ti: topn) {
				item2scores.put(ti.id, Pair.of(mypref, ti.score * logp / maxscore));
				id2createAts.put(ti.id, ti.createAt);
			}
		}
		
		final PriorityQueue<Double, Long> pq = PriorityQueue.make(howManyItem, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		double now = System.currentTimeMillis();
		for (Map.Entry<Long, Collection<Pair<Long, Double>>> e: item2scores.asMap().entrySet()) {
			long id = e.getKey();
			double score = 0;
			for (Pair<Long, Double> sub: e.getValue()) score += sub.getSecond();
			double createAt = id2createAts.get(id);
			if(createAt < now){
				// score = originalScore / (T + 2)^Gravity, we can check this equation in http://www.ruanyifeng.com/blog/2012/02/ranking_algorithm_hacker_news.html
				score /= Math.pow(((now - createAt) / Constants.ONE_HOUR + 2.0), 1.8);
				pq.add(score, id);
			}			
		}

		if (pq.size() > 0) {
			List<TopItem> result = new ArrayList<TopItem>();
			List<Long> ids = itemFilter.filterOldItem(userId, pq.values(), new HashSet<Long>(myprefids));
			for (PriorityQueue.Entry<Double, Long> e : pq.entries()) {
				long id = e.getValue();
				if (ids.contains(id)) {
					double score = e.getKey();
					List<Long> refitems = new ArrayList<Long>();
					for (Pair<Long, Double> sub: item2scores.get(id)) refitems.add(sub.getFirst());
					result.add(new TopItem(id, score, -1, id2createAts.get(id), Algorithm.CF, TopItem.asStr(refitems, "refitem")));
				}
			}
			
			fuser.add(result, weight);
		}
	}
	
	private List<ItemScore> recommend4item(List<Minhash> mhes, int topn) {
		final long since = System.currentTimeMillis() - Constants.ONE_DAY * 10;
		List<ItemScore> result = new ArrayList<ItemScore>();
		if (mhes.isEmpty()) return result;
		
		final Map<Integer, Integer> hash2size = mhes2hs(mhes);
		
		Map<Integer, List<ItemUSize>> hash2itemusizes = jcache.batchSyncs("hash-items-", new ArrayList<Integer>(hash2size.keySet()), ItemUSize.class, Constants.ONE_HOUR * 3, new Function<List<Integer>, Map<Integer, List<ItemUSize>>>(){
			@Override
			public Map<Integer, List<ItemUSize>> apply(List<Integer> hashes) {
				Map<Integer, List<ItemUSize>> result = new HashMap<Integer, List<ItemUSize>>();
				if (hashes.isEmpty()) return result;
				
				List<ItemMinhash> jmheslist = imhDao.createQuery().filter("minhashes.hash in", hashes).filter("createAt > ",since).asList();
				if (jmheslist.isEmpty()) return result;
				
				HashSet<Long> ids = new HashSet<Long>();
				for (ItemMinhash jmhes: jmheslist) ids.add(jmhes.getId());
				Map<Long, Status> id2status = articleDao.mapField(new ArrayList<Long>(ids), "status");
				
				for (int hash: hashes) result.put(hash, new ArrayList<ItemUSize>());
				for (ItemMinhash jmhes: jmheslist) {
					Status status = id2status.get(jmhes.getId());
					if (status == null || status != Status.OK) continue;
					
					for (Minhash mh: jmhes.getMinhashes()) {
						List<ItemUSize> ius = result.get(mh.getHash());
						if (ius != null) ius.add(new ItemUSize(jmhes.getId(), mh.getSize(), jmhes.getCreateAt()));
					}
				}
				return result;
			}
		});
		
		final PriorityQueue<Double, Long> pq = PriorityQueue.make(topn, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		HashMap<Long, Long> item2createAt = new HashMap<Long, Long>();
		HashMultimap<Long, Minhash> item2mhes = HashMultimap.create();
		for (Map.Entry<Integer, List<ItemUSize>> e: hash2itemusizes.entrySet()) {
			for (ItemUSize iu: e.getValue()) {
				item2createAt.put(iu.id, iu.createAt);
				item2mhes.put(iu.id, new Minhash(e.getKey(), iu.usize));
			}
		}
		
		for (Map.Entry<Long, Collection<Minhash>> e: item2mhes.asMap().entrySet()) {
			double score = 0;
			for (Minhash mh: e.getValue()) {
				Integer usize = hash2size.get(mh.getHash());
				if (usize != null) {
					double w = 1 / Math.log(1 + mh.getSize() + usize);
					double w2 = w * w;
					score += w2 * w2;
				}
			}
			pq.add(score, e.getKey());
		}
		
		for (PriorityQueue.Entry<Double, Long> e: pq.entries()) {
			ItemScore item = new ItemScore(e.getValue(), e.getKey(), item2createAt.get(e.getValue()));
			result.add(item);
		}
		
		return result;
	}
	
	private Map<Integer, Integer> mhes2hs(List<Minhash> mhes) {
		Map<Integer, Integer> hash2size = new HashMap<Integer, Integer>();
		for (Minhash mh: mhes) hash2size.put(mh.getHash(), mh.getSize());
		return hash2size;
	}
	
	private double maxScore(List<ItemScore> itemDatas) {
		double maxscore = 1;
		for (ItemScore itemData: itemDatas) {
			if (maxscore < itemData.score) maxscore = itemData.score;
		}
		return maxscore;
	}
	
	public static class ItemUSize {
		public long id;
		public int usize;
		public long createAt;
		
		public ItemUSize() {
		}
		
		public ItemUSize(long id, int usize, long createAt) {
			this.id = id;
			this.usize = usize;
			this.createAt = createAt;
		}
	}
}