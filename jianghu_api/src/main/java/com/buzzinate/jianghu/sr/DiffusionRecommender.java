package com.buzzinate.jianghu.sr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mahout.math.function.LongDoubleProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;

import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserProfileDao;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.diffusion.Bipartite;
import com.buzzinate.common.diffusion.Diffusions;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.KeywordFeature;
import com.buzzinate.common.model.UserFeature;
import com.buzzinate.common.model.UserProfile;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.PriorityQueue.Entry;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.jianghu.jredis.PrefCache;
import com.buzzinate.jianghu.jredis.PrefData;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class DiffusionRecommender extends Recommender {
	private static Logger log = Logger.getLogger(DiffusionRecommender.class);
	
	private UserProfileDao upDao;
	private PrefCache prefCache;
	private Dictionary dict;
	private Map<String, Algorithm> algos = new HashMap<String, Algorithm>();
	
	public DiffusionRecommender(PrefCache prefCache, PreferenceDao prefDao, ItemFilter itemFilter, Dictionary dict, int weight) {
		super(itemFilter, weight);
		this.prefCache = prefCache;
		this.dict = dict;
		this.upDao = new UserProfileDao(prefDao.getDatastore());
		
		algos.put("heat", Algorithm.HeatDiffusion);
		algos.put("energy", Algorithm.EnergyDiffusion);
	}

	@Override
	public void recommend(final long userId, final int howManyItem, Fuser fuser) {
		final long since = System.currentTimeMillis() - Constants.ONE_DAY * 10;
		UserProfile up = upDao.get(userId);
		if (up == null) return;
		
		List<PrefData> myPrefs = prefCache.fetchPrefIds(userId);
		final HashSet<Long> myPrefSet = new HashSet<Long>();
		for (PrefData pd: myPrefs) myPrefSet.add(pd.getId());
		
		HashSet<Long> users = new HashSet<Long>();
		for (UserFeature uf: up.getUserFeatures()) {
			users.add(uf.getUserId());
		}
		
		long start = System.currentTimeMillis();
		Map<Long, List<PrefData>> user2pages = new HashMap<Long, List<PrefData>>();
		for (long nid: users) user2pages.put(nid, prefCache.fetchPrefIds(nid));
		log.info("!!!!! batch cost: " + (System.currentTimeMillis() - start));
//		ArticleDao articleDao = new ArticleDaoImpl(upDao.getDatastore());
//		for (Map.Entry<Long, List<PrefData>> e: user2pages.entrySet()) {
//			System.out.print(e.getKey() + " ==> ");
//			List<Long> ids = new ArrayList<Long>();
//			for (PrefData pd: e.getValue()) ids.add(pd.getId());
//			System.out.println(articleDao.mapField(ids, "title"));
//		}
		Bipartite<Long> g = new Bipartite<Long>();
		rebuildBipartite(g, user2pages);
		for (UserFeature uf: up.getUserFeatures()) g.setRowSize(uf.getUserId(), uf.getPrefSize());
		
		HashSet<String> myWords = new HashSet<String>();
		for (KeywordFeature kf: up.getKeywordFeatures()) myWords.add(kf.getKeyword());
		for (Keyword kw: up.getKeywords()) myWords.add(kw.getWord());
		Bipartite<String> wg = new Bipartite<String>();
		rebuildWordBipartite(wg, myWords, user2pages);
		
		final HashMap<Long, Long> id2creates = new HashMap<Long, Long>();
		for (List<PrefData> pds: user2pages.values()) {
			for (PrefData pd: pds) {
				id2creates.put(pd.getId(), pd.getCreateAt());
			}
		}
		
		for (final Diffusions.Algo algo: Diffusions.algos) {
			Collection<UserFeature> ufs = Collections2.filter(up.getUserFeatures(), new Predicate<UserFeature>(){
				@Override
				public boolean apply(UserFeature uf) {
					return uf.getAlgo().equals(algo.name);
				}
			});
			Multimap<Integer, UserFeature> c2ufs = Multimaps.index(ufs, new Function<UserFeature, Integer>(){
				@Override
				public Integer apply(UserFeature uf) {
					return uf.getClusterId();
				}});
			
			Collection<KeywordFeature> kfs = Collections2.filter(up.getKeywordFeatures(), new Predicate<KeywordFeature>(){
				@Override
				public boolean apply(KeywordFeature kf) {
					return kf.getAlgo().equals(algo.name);
				}
			});
			Multimap<Integer, KeywordFeature> c2kfs = Multimaps.index(kfs, new Function<KeywordFeature, Integer>(){
				@Override
				public Integer apply(KeywordFeature kf) {
					return kf.getClusterId();
				}
			});
			
			final PriorityQueue<Double, Long> pq = PriorityQueue.make(howManyItem, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
			
			Set<Integer> cs = new HashSet<Integer>();
			cs.addAll(c2ufs.keySet());
			cs.addAll(c2kfs.keySet());
			for (final int c: cs) {
				OpenObjectDoubleHashMap<Long> userScore = new OpenObjectDoubleHashMap<Long>();
				for (UserFeature uf: c2ufs.get(c)) userScore.put(uf.getUserId(), uf.getResource());
				OpenLongDoubleHashMap colScore = algo.diffusion.row2col(g, userScore);
				userScore = algo.diffusion.col2row(g, colScore);
				colScore = combine(colScore, 10, algo.diffusion.row2col(g, userScore), -1);
				
				OpenObjectDoubleHashMap<String> wordScore = new OpenObjectDoubleHashMap<String>();
				for (KeywordFeature kf: c2kfs.get(c)) wordScore.put(kf.getKeyword(), kf.getResource());
				OpenLongDoubleHashMap wcolScore = algo.diffusion.row2col(wg, wordScore);
				wordScore = algo.diffusion.col2row(wg, wcolScore);
				wcolScore = combine(wcolScore, 10, algo.diffusion.row2col(wg, wordScore), -1);
				
				combine(colScore, 3, wcolScore, 1).forEachPair(new LongDoubleProcedure(){
					@Override
					public boolean apply(long item, double score) {
						boolean prefed = myPrefSet.contains(item);
						long createAt = id2creates.get(item);
						if (!prefed && createAt > since) {
							pq.add(score, item);
						}
						return true;
					}
				});
			}
			
			List<TopItem> items = new ArrayList<TopItem>();
			if (pq.size() > 0) {
				List<Long> ids = itemFilter.filterOldItem(userId, pq.values(), myPrefSet);
				HashSet<Long> itemset = new HashSet<Long>();
				for (Entry<Double, Long> t: pq.entries()) {
					long itemId = t.getValue();
					if (ids.contains(itemId) && !itemset.contains(itemId)) {
						long createAt = id2creates.get(itemId);
						List<String> features = TopItem.asStr(g.getCol(itemId), "user");
						features.addAll(TopItem.asStr(wg.getCol(itemId), "keyword"));
						items.add(new TopItem(itemId, t.getKey(), -1, createAt, algos.get(algo.name), features));
					}
				}
			}
			fuser.add(items, getWeight());
		}
	}

	private void rebuildBipartite(Bipartite<Long> g, Map<Long, List<PrefData>> user2pages) {
		g.clear();
		for (Map.Entry<Long, List<PrefData>> e: user2pages.entrySet()) {
			long user = e.getKey();
			for (PrefData pd: e.getValue()) {
				if (pd.getSize() > 0) {
					g.add(user, pd.getId());
					g.setColSize(pd.getId(), pd.getSize());
				}
			}
		}
	}
	
	private void rebuildWordBipartite(Bipartite<String> g, Set<String> myWords, Map<Long, List<PrefData>> user2pages) {
		g.clear();
		for (Map.Entry<Long, List<PrefData>> e: user2pages.entrySet()) {
			for (PrefData pd: e.getValue()) {
				List<String> words = pd.getWords();
				words.retainAll(myWords);
				for (String word: words) {
					g.add(word, pd.getId());
					g.setRowSize(word, dict.docFreq(word));
				}
			}
		}
	}
	
	private static OpenLongDoubleHashMap combine(OpenLongDoubleHashMap scores1, final int w1, OpenLongDoubleHashMap scores2, final int w2) {
		final OpenLongDoubleHashMap result = new OpenLongDoubleHashMap();
		scores1.forEachPair(new LongDoubleProcedure(){
			@Override
			public boolean apply(long item, double score) {
				result.adjustOrPutValue(item, score * w1, score * w1);
				return true;
			}
		});
		scores2.forEachPair(new LongDoubleProcedure(){
			@Override
			public boolean apply(long item, double score) {
				result.adjustOrPutValue(item, score * w2, score * w2);
				return true;
			}
		});
		return result;
	}
}