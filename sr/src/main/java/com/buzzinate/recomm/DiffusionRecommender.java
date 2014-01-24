package com.buzzinate.recomm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.math.function.LongDoubleProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;
import org.bson.types.ObjectId;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ArticleProfileDao;
import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.diffusion.Bipartite;
import com.buzzinate.common.diffusion.Diffusions;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.ArticleProfile;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.KeywordFeature;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.model.UserFeature;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.PriorityQueue.Entry;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.diffusion.DiffusionProfile;
import com.buzzinate.model.Read;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class DiffusionRecommender {
	private ArticleDao articleDao;
	private ArticleProfileDao apDao;
	private PreferenceDao prefDao;
	private Dictionary dict;
	
	public DiffusionRecommender(ArticleDao articleDao, PreferenceDao prefDao, Dictionary dict) {
		this.articleDao = articleDao;
		this.prefDao = prefDao;
		this.dict = dict;
		this.apDao = new ArticleProfileDao(prefDao.getDatastore());
	}

	public void recommend(long userId, DiffusionProfile dp, final int howManyItem) {
		final long since = System.currentTimeMillis() - Constants.ONE_DAY * 30;
		
		
		List<PrefData> myPrefs = fetchData(userId, since);
		final HashSet<Long> myPrefSet = new HashSet<Long>();
		for (PrefData pd: myPrefs) myPrefSet.add(pd.getId());
		
		HashSet<Long> users = new HashSet<Long>();
		for (UserFeature uf: dp.getUserFeatures()) {
			users.add(uf.getUserId());
		}
		
		Map<Long, List<PrefData>> user2pages = new HashMap<Long, List<PrefData>>();
		for (long user: users) user2pages.put(user, fetchData(user, since));
		
		for (Map.Entry<Long, List<PrefData>> e: user2pages.entrySet()) {
			System.out.print(e.getKey() + " ==> ");
			List<Long> ids = new ArrayList<Long>();
			for (PrefData pd: e.getValue()) ids.add(pd.getId());
			System.out.println(articleDao.mapField(ids, "title"));
		}
		Bipartite<Long> g = new Bipartite<Long>();
		rebuildBipartite(g, user2pages);
		for (UserFeature uf: dp.getUserFeatures()) g.setRowSize(uf.getUserId(), uf.getPrefSize());
		
		HashSet<String> myWords = new HashSet<String>();
		for (KeywordFeature kf: dp.getKeywordFeatures()) myWords.add(kf.getKeyword());
		Bipartite<String> wg = new Bipartite<String>();
		rebuildWordBipartite(wg, myWords, user2pages);
		
		final HashMap<Long, Long> id2creates = new HashMap<Long, Long>();
		for (List<PrefData> pds: user2pages.values()) {
			for (PrefData pd: pds) {
				id2creates.put(pd.getId(), pd.getCreateAt());
			}
		}
		
		for (final Diffusions.Algo algo: Diffusions.algos) {
			final PriorityQueue<Double, Long> pq = PriorityQueue.make(howManyItem, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
			Collection<UserFeature> ufs = Collections2.filter(dp.getUserFeatures(), new Predicate<UserFeature>(){
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
			
			Collection<KeywordFeature> kfs = Collections2.filter(dp.getKeywordFeatures(), new Predicate<KeywordFeature>(){
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
				List<Long> ids = filterOldItem(userId, pq.values(), myPrefSet);
				
				HashSet<Long> itemset = new HashSet<Long>();
				for (Entry<Double, Long> t: pq.entries()) {
					long itemId = t.getValue();
					if (ids.contains(itemId) && !itemset.contains(itemId)) {
						long createAt = id2creates.get(itemId);
						List<String> features = TopItem.asStr(g.getCol(itemId), "user");
						features.addAll(TopItem.asStr(wg.getCol(itemId), "keyword"));
						items.add(new TopItem(itemId, t.getKey(), createAt, algo.name, features));
						itemset.add(itemId);
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
	
	private List<PrefData> fetchData(long userId, long since) {
		List<PrefData> result = new ArrayList<PrefData>();
		
		List<Preference> prefs = prefDao.createQuery().filter("userId", userId).filter("_id >", new ObjectId(new Date(since))).filter("createAt >", since).asList();
		List<Long> ids = new ArrayList<Long>();
		for (Preference pref: prefs) ids.add(pref.getPageId());
		if (ids.isEmpty()) return result;
		
		HashMap<Long, Article> id2a = new HashMap<Long, Article>();
		for (Article a: articleDao.get(new ArrayList<Long>(ids), "status", "prefSize", "createAt")) {
			if (a != null) id2a.put(a.getId(), a);
		}
		Map<Long, ArticleProfile> id2ap = apDao.map2(new ArrayList<Long>(ids), "keywords", "title");
		
		for (Preference pref: prefs) {
			Article a = id2a.get(pref.getPageId());
			ArticleProfile ap = id2ap.get(pref.getPageId());
			List<String> words = new ArrayList<String>();
			if (ap != null && ap.getKeywords() != null) {
				//String title = KeywordUtil.fillContent(ap.getTitle());
				for (Keyword kw: ap.getKeywords()) {
					//boolean intitle = title.contains(KeywordUtil.fill(kw.getWord()));
					//if(intitle) words.add(kw.getWord());
					words.add(kw.getWord());
				}
			}
			if (a != null) {
				if (a.getStatus() == Status.OK) result.add(new PrefData(a.getId(), words, a.getCreateAt(), a.getPrefSize()));
				else result.add(new PrefData(a.getId(), words, PrefData.MISS, a.getPrefSize()));
			}
		}
		
		return result;
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