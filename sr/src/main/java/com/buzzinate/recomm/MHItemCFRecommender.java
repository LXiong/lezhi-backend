package com.buzzinate.recomm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.math.function.IntDoubleProcedure;
import org.apache.mahout.math.function.LongDoubleProcedure;
import org.apache.mahout.math.map.OpenIntDoubleHashMap;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.bson.types.ObjectId;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.ItemMinhashDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.model.ItemMinhash;
import com.buzzinate.common.model.Minhash;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.MinhashUtil;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.common.util.PriorityQueue.Entry;
import com.buzzinate.model.Read;

public class MHItemCFRecommender {
	private PreferenceDao prefDao;
	private ItemMinhashDao imhDao;
	private ArticleDao articleDao;
	
	public MHItemCFRecommender(PreferenceDao prefDao, ItemMinhashDao imhDao, ArticleDao articleDao) {
		this.prefDao = prefDao;
		this.imhDao = imhDao;
		this.articleDao = articleDao;
	}

	public void recommend(long userId, int howManyItem, double beta) {
		final long since = System.currentTimeMillis() - Constants.ONE_DAY * 10;
		
		List<Long> myprefs = prefDao.findLatestItems(userId, 200);
		final HashSet<Long> myprefset = new HashSet<Long>(myprefs);
		
		List<ItemMinhash> itemmhes = imhDao.get(myprefs);
		OpenIntDoubleHashMap mh2weight = new OpenIntDoubleHashMap();
		int cnt = 0;
		for (ItemMinhash imh: itemmhes) {
			if (imh == null) continue;
			cnt += 1;
			for (Minhash mh: imh.getMinhashes()) {
				double w = 1 / Math.log(1 + mh.getSize());
				double w4 = Math.pow(w, beta);
				mh2weight.adjustOrPutValue(mh.getHash(), w4, w4);
			}
		}
		System.out.println(myprefs.size() + " ==> " + cnt);
		
		final int selfhash = MinhashUtil.hash(userId);
		final PriorityQueue<Double, Integer> mpq = PriorityQueue.make(160, SortUtils.reverse(Double.class), SortUtils.comp(Integer.class));
		mh2weight.forEachPair(new IntDoubleProcedure(){
			@Override
			public boolean apply(int hash, double w) {
				if (hash != selfhash) mpq.add(w, hash);
				return true;
			}});
		
		HashMap<Integer, Double> topmh2weight = new HashMap<Integer, Double>();
		for (Entry<Double, Integer> e: mpq.entries()) topmh2weight.put(e.getValue(), e.getKey());
		
		List<ItemMinhash> jmhes = imhDao.createQuery().filter("minhashes.hash in", topmh2weight.keySet()).filter("createAt > ", since).asList();
		
		final HashMap<Long, Long> item2creates = new HashMap<Long, Long>();
		OpenLongDoubleHashMap item2score = new OpenLongDoubleHashMap();
		for (ItemMinhash jmh: jmhes) {
			long j = jmh.getId();
			item2creates.put(j, jmh.getCreateAt());
			for (Minhash mh: jmh.getMinhashes()) {
				Double subweight = topmh2weight.get(mh.getHash());
				if (subweight != null) item2score.adjustOrPutValue(j, subweight, subweight);
			}
		}
		
		final PriorityQueue<Double, Long> pq = PriorityQueue.make(howManyItem, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		item2score.forEachPair(new LongDoubleProcedure() {
			@Override
			public boolean apply(long item, double score) {
				if (!myprefset.contains(item)) {
					pq.add(score, item);
				}
				return true;
			}
		});
		
		List<TopItem> items = new ArrayList<TopItem>();
		if (pq.size() > 0) {
			List<Long> ids = filterOldItem(userId, pq.values(), myprefset);
			
			for (Entry<Double, Long> t: pq.entries()) {
				if (ids.contains(t.getValue())) {
					long createAt = item2creates.get(t.getValue());
					List<String> features = Arrays.asList("");
					items.add(new TopItem(t.getValue(), t.getKey(), createAt, "ItemCF", features));
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
