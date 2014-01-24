package com.buzzinate.jianghu.sr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mahout.common.Pair;
import org.buzzinate.lezhi.api.Client;
import org.buzzinate.lezhi.api.Doc;
import org.buzzinate.lezhi.api.HitDoc;
import org.buzzinate.lezhi.api.UrlTime;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserProfileDao;
import com.buzzinate.common.dao.UserWeiboDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.UserProfile;
import com.buzzinate.common.model.UserWeibo;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.MentionUtil;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.jianghu.dao.ReadDao;
import com.buzzinate.jianghu.sr.cache.JCache;
import com.buzzinate.keywords.MobileKeywordsExtractor;
import com.buzzinate.nlp.util.DictUtil;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;

public class SearchRecommender extends Recommender {
	private static Logger log = Logger.getLogger(SearchRecommender.class);
	private static int NTOP_SUM = 4;
	private ArticleDao articleDao;
	private UserProfileDao upDao;
	private ReadDao readDao;
	private PreferenceDao prefDao;
	private UserWeiboDao uwDao;
	private JCache jcache;
	private Client client;

	public SearchRecommender(Client client, JCache jcache, ArticleDao articleDao, ReadDao readDao, UserProfileDao upDao, ItemFilter itemFilter, int weight) {
		super(itemFilter, weight);
		this.jcache = jcache;
		this.articleDao = articleDao;
		this.readDao = readDao;
		this.upDao = upDao;
		this.prefDao = new PreferenceDao(articleDao.getDatastore());
		this.client = client;
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

					List<Keyword> keywords = new ArrayList<Keyword>();
					for (com.buzzinate.keywords.Keyword keyword: MobileKeywordsExtractor.extract(texts, 30)) {
						keywords.add(new Keyword(keyword.word(), keyword.freq()));
					}
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
			
		HashMap<Long, Long> id2createAts = new HashMap<Long, Long>();
		HashMultimap<Long, Pair<Long, Double>> item2scores = HashMultimap.create();
		
		// recommend for weibo keywords
		List<ItemScore> usertopn = jcache.syncs("content-user-topn-", userId, ItemScore.class, Constants.ONE_HOUR * 5, new Function<Long, List<ItemScore>>(){
			@Override
			public List<ItemScore> apply(Long key) {
				List<String> kws = new ArrayList<String>();
				for (Keyword keyword: keywords) {
					if (keyword.getWord() == null) continue;
					kws.add(String.format("%s|%s,%s,%s", StringUtils.replace(keyword.getWord(), " ", "_"), keyword.getFreq(), 0, idf(keyword.getWord())));
				}
				return recommend4item(null, StringUtils.join(kws, " "), 20, 100);
			}
		});
		log.debug("user topn for " + userId + ": " + usertopn);
		
		double maxscore = maxScore(usertopn);
		double logp = 1 / Math.log(2 + minprefsize);
		for (ItemScore ti: usertopn) {
			item2scores.put(ti.id, Pair.of(0L, ti.score * 3 * logp / maxscore));
			id2createAts.put(ti.id, ti.createAt);
		}
		
		// recommend for preference items
		List<UrlTime> uts = new ArrayList<UrlTime>();
		if (pref2weight.size() > 0) {
			for (Article a: articleDao.createQuery().retrievedFields(true, "url", "createAt").filter("_id in", pref2weight.keySet()).asList()) {
				uts.add(new UrlTime(a.getUrl(), a.getCreateAt()));
			}
		}
		
		for (final Doc doc: client.get(uts).values()) {
			long mypref = doc.id;
			int prefsize = minprefsize;
			Integer ps = id2prefsize.get(mypref);
			if (ps != null) prefsize = ps;
			int weight = pref2weight.get(mypref);
			
			List<ItemScore> topn = jcache.asyncs("content-item-topn-", mypref, ItemScore.class, Constants.ONE_HOUR * 10, new Function<Long, List<ItemScore>>(){
				@Override
				public List<ItemScore> apply(Long key) {
					List<String> kws = new ArrayList<String>();
					for (String part: StringUtils.split(doc.keyword, " ")) {
						String word = StringUtils.substringBefore(part, "|");
						kws.add(part + "," + idf(StringUtils.replace(word, "_", " ")));
					}
					return recommend4item(doc.signature, StringUtils.join(kws, " "), 10, 50);
				}
			});
			
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
	
	private double idf(String word) {
		try {
			return DictUtil.splitIdf(word);
		} catch (IOException e) {
			return 1;
		}
	}
	
	private double maxScore(List<ItemScore> itemDatas) {
		double maxscore = 1;
		for (ItemScore itemData: itemDatas) {
			if (maxscore < itemData.score) maxscore = itemData.score;
		}
		return maxscore;
	}

	private List<ItemScore> recommend4item(String signature, String keywords, int maxword, int topn) {
		final long since = System.currentTimeMillis() - Constants.ONE_DAY * 20;
		List<ItemScore> result = new ArrayList<ItemScore>();
		if (StringUtils.isBlank(keywords)) return result;
		
		List<HitDoc> hdocs = client.query(signature, keywords, since, maxword, topn);
		for (HitDoc doc: hdocs) {
			ItemScore is = new ItemScore(doc.id, doc.score, doc.lastModified);
			result.add(is);
		}
		
		return result;
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