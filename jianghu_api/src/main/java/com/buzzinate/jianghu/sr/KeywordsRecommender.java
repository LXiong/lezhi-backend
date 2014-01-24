package com.buzzinate.jianghu.sr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mahout.common.Pair;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ArticleProfileDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserProfileDao;
import com.buzzinate.common.dao.UserWeiboDao;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.model.ArticleProfile;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.model.UserProfile;
import com.buzzinate.common.model.UserWeibo;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.MentionUtil;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.crawl.core.util.TextUtility;
import com.buzzinate.jianghu.dao.ReadDao;
import com.buzzinate.jianghu.sr.cache.JCache;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;

public class KeywordsRecommender extends Recommender {
	private static Logger log = Logger.getLogger(KeywordsRecommender.class);
	private static int NTOP_SUM = 4;
	private ArticleDao articleDao;
	private UserProfileDao upDao;
	private ReadDao readDao;
	private PreferenceDao prefDao;
	private ArticleProfileDao apDao;
	private UserWeiboDao uwDao;
	private Dictionary dict;
	private JCache jcache;

	public KeywordsRecommender(JCache jcache, ArticleDao articleDao, ReadDao readDao, UserProfileDao upDao, ArticleProfileDao apDao, Dictionary dict, ItemFilter itemFilter, int weight) {
		super(itemFilter, weight);
		this.jcache = jcache;
		this.articleDao = articleDao;
		this.readDao = readDao;
		this.upDao = upDao;
		this.prefDao = new PreferenceDao(articleDao.getDatastore());
		this.apDao = apDao;
		this.dict = dict;
		this.uwDao = new UserWeiboDao(articleDao.getDatastore());
	}

	@Override
	public void recommend(long userId, int howManyItem, Fuser fuser) {
		final List<Keyword> keywords = jcache.syncs("keywords-", userId, Keyword.class, Constants.ONE_DAY, new Function<Long, List<Keyword>>(){
			@Override
			public List<Keyword> apply(Long userId) {
				UserProfile up = upDao.get(userId);
				if (up == null || up.getKeywords().isEmpty() || up.getCreateAt() < System.currentTimeMillis() - Constants.ONE_DAY) {
					List<UserWeibo> weibos = uwDao.findLatest(userId, 200);
					List<String> texts = new ArrayList<String>();
					for (UserWeibo weibo : weibos) {
						String text = MentionUtil.cleanStatusText(weibo.getText());
						texts.add(text);
					}

					List<Keyword> keywords = dict.extract(texts, 5, 30);
					if (keywords.size() > 0) upDao.update(userId, keywords);
					up = new UserProfile();
					up.setUserId(userId);
					up.setKeywords(keywords);
					up.setCreateAt(System.currentTimeMillis());
					return keywords;
				} else return up.getKeywords();
			}
		});

		HashMap<Long, Integer> pref2weight = new HashMap<Long, Integer>();
		List<Long> myprefids = prefDao.findLatestItems(userId, howManyItem);
		List<Long> myreadids = readDao.findLatestRead(userId, howManyItem / 4);
		for (long rid: myreadids) pref2weight.put(rid, 1);
		for (long pid: myprefids) pref2weight.put(pid, 3);
		
		Map<Long, Integer> id2prefsize = articleDao.mapField(new ArrayList<Long>(pref2weight.keySet()), "prefSize");
		int minprefsize = Integer.MAX_VALUE;
		for (Map.Entry<Long, Integer> e : id2prefsize.entrySet()) {
			if (minprefsize > e.getValue()) minprefsize = e.getValue(); 
		}
		
		final Map<Long, ArticleProfile> mypref2ap = apDao.map2(new ArrayList<Long>(pref2weight.keySet()), "keywords");		
		HashMap<Long, Long> id2createAts = new HashMap<Long, Long>();
		
		HashMultimap<Long, Pair<Long, Double>> item2scores = HashMultimap.create();
		
		List<ItemScore> usertopn = jcache.syncs("content-user-topn-", userId, ItemScore.class, Constants.ONE_HOUR * 5, new Function<Long, List<ItemScore>>(){
			@Override
			public List<ItemScore> apply(Long key) {
				return recommend4item(keywords, 100);
			}
		});
		log.debug("user topn for " + userId + ": " + usertopn);
		
		double maxscore = maxScore(usertopn);
		double logp = 1 / Math.log(2 + minprefsize);
		for (ItemScore ti: usertopn) {
			item2scores.put(ti.id, Pair.of(0L, ti.score * 3 * logp / maxscore));
			id2createAts.put(ti.id, ti.createAt);
		}
		
		for (final Map.Entry<Long, Integer> e: id2prefsize.entrySet()) {
			long mypref = e.getKey();
			int prefsize = e.getValue();
			int weight = pref2weight.get(mypref);
			
			final ArticleProfile ap = mypref2ap.get(mypref);
			if (ap == null) continue;
			
			List<ItemScore> topn = null;
			if (prefsize <= minprefsize * 5) {
				topn = jcache.syncs("content-item-topn-", mypref, ItemScore.class, Constants.ONE_HOUR * 10, new Function<Long, List<ItemScore>>(){
					@Override
					public List<ItemScore> apply(Long key) {
						return recommend4item(ap.getKeywords(), 100);
					}
				});
			} else {
				topn = jcache.asyncs("content-item-topn-", mypref, ItemScore.class, Constants.ONE_HOUR * 10, new Function<Long, List<ItemScore>>(){
					@Override
					public List<ItemScore> apply(Long key) {
						return recommend4item(ap.getKeywords(), 100);
					}
				});
			}
			
			log.debug("topn item for " + userId + " with item=" + mypref + ": " + topn);
			
			maxscore = maxScore(topn);
			logp = 1 / Math.log(2 + prefsize);
			for (ItemScore ti: topn) {
				item2scores.put(ti.id, Pair.of(mypref, ti.score * weight * logp / maxscore));
				id2createAts.put(ti.id, ti.createAt);
			}
		}
		
		final PriorityQueue<Double, Long> pq = PriorityQueue.make(howManyItem, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		double now = System.currentTimeMillis();
		for (Map.Entry<Long, Collection<Pair<Long, Double>>> e: item2scores.asMap().entrySet()) {
			long id = e.getKey();
			// sum topn sub score
			PriorityQueue<Double, Double> spq = PriorityQueue.make(NTOP_SUM, SortUtils.reverse(Double.class), SortUtils.reverse(Double.class));
			for (Pair<Long, Double> sub: e.getValue()) spq.add(sub.getSecond(), sub.getSecond());
			double score = 0;
			for (double sub: spq.values()) score += sub;
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
					result.add(new TopItem(id, score, -1, id2createAts.get(id), Algorithm.Content, TopItem.asStr(refitems, "refitem")));
				}
			}
			
			fuser.add(result, weight);
		}
	}
	
	private double maxScore(List<ItemScore> itemDatas) {
		double maxscore = 1;
		for (ItemScore itemData: itemDatas) {
			if (maxscore < itemData.score) maxscore = itemData.score;
		}
		return maxscore;
	}

	private List<ItemScore> recommend4item(List<Keyword> keywords, int topn) {
		final long since = System.currentTimeMillis() - Constants.ONE_DAY * 10;
		List<ItemScore> result = new ArrayList<ItemScore>();
		if (keywords.isEmpty()) return result;
		
		final Map<String, Integer> wfs =  keywords2wordfreq(keywords);
		final PriorityQueue<Double, Long> pq = PriorityQueue.make(topn, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		HashMap<Long, Long> item2createAt = new HashMap<Long, Long>();
		
		Map<String, List<ItemFreq>> word2itemfreqs = jcache.batchSyncs("keyword-items-", new ArrayList<String>(wfs.keySet()), ItemFreq.class, Constants.ONE_HOUR * 3, new Function<List<String>, Map<String, List<ItemFreq>>>(){
			@Override
			public Map<String, List<ItemFreq>> apply(List<String> words) {
				Map<String, List<ItemFreq>> result = new HashMap<String, List<ItemFreq>>();
				if (words.isEmpty()) return result;
				
				for (String word: words) result.put(word, new ArrayList<ItemFreq>());
				List<ArticleProfile> aps = apDao.createQuery().filter("keywords.word in", words).filter("createAt >", since).asList();
				if (aps.isEmpty()) return result;
				
				HashSet<Long> ids = new HashSet<Long>();
				for (ArticleProfile ap: aps) ids.add(ap.getId());
				Map<Long, Status> id2status = articleDao.mapField(new ArrayList<Long>(ids), "status");
				
				for (ArticleProfile ap: aps) {
					Status status = id2status.get(ap.getId());
					if (status == null || status != Status.OK) continue;
					
					for (Keyword kw: ap.getKeywords()) {
						List<ItemFreq> ifs = result.get(kw.getWord());
						if (ifs != null) ifs.add(new ItemFreq(ap.getId(), kw.getFreq(), ap.getCreateAt()));
					}
				}
				return result;
			}
		});
		
		HashMultimap<Long, Keyword> item2kws = HashMultimap.create();
		for (Map.Entry<String, List<ItemFreq>> e: word2itemfreqs.entrySet()) {
			for (ItemFreq itemFreq: e.getValue()) {
				item2createAt.put(itemFreq.id, itemFreq.createAt);
				item2kws.put(itemFreq.id, new Keyword(e.getKey(), itemFreq.freq));
			}
		}
		
		for (Map.Entry<Long, Collection<Keyword>> e: item2kws.asMap().entrySet()) {
			double score = scoreByKeyword(wfs, e.getValue());
			pq.add(score, e.getKey());
		}
		
		for (PriorityQueue.Entry<Double, Long> e: pq.entries()) {
			ItemScore item = new ItemScore(e.getValue(), e.getKey(), item2createAt.get(e.getValue()));
			result.add(item);
		}
		
		return result;
	}
	
	private double scoreByKeyword(Map<String, Integer> word2freq, Collection<Keyword> kws) {
		List<Double> subScores = new ArrayList<Double>();
		for (Keyword kw : kws) {
			Integer freq = word2freq.get(kw.getWord());
			if (freq != null) {
				Double idf = dict.idf(kw.getWord());
				double subScore = TextUtility.isUsefulKeyword(kw.getWord()) ? 5 : 3;
				subScore *= idf * Math.sqrt(freq * kw.getFreq());
				subScores.add(subScore);
			}
		}
		return scoreTotal(subScores, 10);
	}
	
	private static double scoreTotal(List<Double> subScores, int maxLen) {
		if (subScores.isEmpty())
			return 0;

		PriorityQueue<Double, Double> tspq = PriorityQueue.make(maxLen, SortUtils.reverse(Double.class), SortUtils.reverse(Double.class));
		for (Double s : subScores)
			tspq.add(s, s);
		double sum = 0;
		for (double s : tspq.values())
			sum += s;

		return sum;
	}
	
	private static Map<String, Integer> keywords2wordfreq(List<Keyword> keywords) {
		Map<String, Integer> wfs = new HashMap<String, Integer>();
		for (Keyword kw : keywords)
			wfs.put(kw.getWord(), kw.getFreq());
		return wfs;
	}

	public static class ItemFreq {
		public long id;
		public int freq;
		public long createAt;
		
		public ItemFreq() {
		}
		
		public ItemFreq(long id, int freq, long createAt) {
			this.id = id;
			this.freq = freq;
			this.createAt = createAt;
		}
	}
}