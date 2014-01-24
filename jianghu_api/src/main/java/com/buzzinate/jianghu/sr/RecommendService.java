package com.buzzinate.jianghu.sr;

import static com.buzzinate.common.util.Constants.KEY_ITEM_NUM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.buzzinate.lezhi.api.Client;

import redis.clients.jedis.ShardedJedisPool;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ArticleProfileDao;
import com.buzzinate.common.dao.ItemMinhashDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.RelatedArticleDao;
import com.buzzinate.common.dao.UserProfileDao;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.RelatedArticle;
import com.buzzinate.common.model.RelatedArticle.RelatedItem;
import com.buzzinate.jianghu.dao.ReadDao;
import com.buzzinate.jianghu.sr.cache.CacheUtils;
import com.buzzinate.jianghu.sr.cache.JCache;
import com.buzzinate.jianghu.sr.cache.LocalCacher;
import com.google.code.morphia.Datastore;
import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class RecommendService {	
	private static Logger log = Logger.getLogger(RecommendService.class);
	
	private List<Recommender> recommenders = new ArrayList<Recommender>();
	private ArticleDao articleDao;
	private ArticleProfileDao apDao;
	private RelatedArticleDao raDao;
	private Dictionary dict;
	
	private ShardedJedisPool pool;
	
	@Inject(optional = true) @Named(KEY_ITEM_NUM)
	private int howManyItem = 200;
	
	private LocalCacher<Long, List<TopItem>> recommendCacher = new LocalCacher<Long, List<TopItem>>(1000L * 1200);
	
	@Inject
	public RecommendService(Datastore ds, Client client, ArticleDao articleDao, ReadDao readDao, ShardedJedisPool pool, JCache jcache, Dictionary dict) throws IOException {
		this.articleDao = articleDao;
		this.apDao = new ArticleProfileDao(ds);
		this.raDao = new RelatedArticleDao(ds);
		this.pool = pool;
		this.dict = dict;
		PreferenceDao prefDao = new PreferenceDao(ds);
		ItemMinhashDao imhDao = new ItemMinhashDao(ds);
		
		ItemFilter itemFilter = new ItemFilter(pool, prefDao, readDao);
		recommenders.add(new MHItemCFRecommender(jcache, prefDao, articleDao, imhDao, itemFilter, 1));
		recommenders.add(new SearchRecommender(client, jcache, articleDao, readDao, new UserProfileDao(ds), itemFilter, 5));
//		recommenders.add(new KeywordsRecommender(jcache, articleDao, readDao, new UserProfileDao(ds), new ArticleProfileDao(ds), dict, itemFilter, 5));
		
		log.info("Starting...");
	}
	
	public List<TopItem> recommend(String algo, final long userId) {
		Algorithm algorithm = Algorithm.valueOf(Algorithm.class, algo);
		long start = System.currentTimeMillis();
		Fuser fuser = new Fuser(apDao);
		for (Recommender r: recommenders) {
			r.recommend(userId, howManyItem, fuser);
		}
		List<TopItem> items = fuser.fuse(algorithm, howManyItem);
		log.info("recommend for user " + userId + " cost(ms): " + (System.currentTimeMillis() - start));
		return items;
	}
	
	public List<TopItem> recommend(final long userId, long lastRecommendArticleTime, int offset, int len) {
		List<TopItem> items = cacheRecommend(userId);
		
		long maxRecTime = lastRecommendArticleTime;
		for (TopItem item: items) {
			long createAt = item.getCreateAt();
			if (maxRecTime < createAt) maxRecTime = createAt;
		}
		
		CacheUtils.getset(pool, "maxrec." + userId, String.valueOf(maxRecTime));
		
		// TODO: tricky way to avoid miss articles
		int to = Math.min(offset + len + 5, items.size());
		log.info("total=" + items.size() + ", actual=[" + offset + ", " + to + ")");
		return items.subList(offset, to);
	}

	private List<TopItem> cacheRecommend(final long userId) {
		return recommendCacher.getOrElse(userId, new Function<Long, List<TopItem>>(){
			@Override
			public List<TopItem> apply(Long userid) {
				long start = System.currentTimeMillis();
				Fuser fuser = new Fuser(apDao);
				for (Recommender r: recommenders) {
					r.recommend(userId, howManyItem, fuser);
				}
				List<TopItem> items = fuser.fuse(howManyItem * 5 / 2);
				log.info("recommend for user " + userId + " cost(ms): " + (System.currentTimeMillis() - start));
				return items;
			}
		});
	}
	
	public long getMaxRecTime(long userId) {
		String t = CacheUtils.get(pool, "maxrec." + userId);
		if (t == null) return -1;
		else return Long.parseLong(t);
	}

	public boolean checkNew(long userId, long lastRecommendArticleTime) {
		long maxRecTime = getMaxRecTime(userId);
		if (maxRecTime > lastRecommendArticleTime) return true;
		else if (maxRecTime == -1) {
			try {
				List<TopItem> items = cacheRecommend(userId);
				
				for (TopItem item: items) {
					long createAt = item.getCreateAt();
					if (createAt > lastRecommendArticleTime) return true;
				}
				return false;
			} catch (Exception e) {
				log.warn("Could not check recommend for user: " + userId, e);
				return false;
			}
		} else return false;
	}
	
	/**
	 * 推荐相关文章
	 */
	public List<RelatedItem> recommendRelated(long id, int max) {
		List<RelatedItem> items = new ArrayList<RelatedItem>();
		RelatedArticle ra = raDao.get(id);
		if (ra == null) return items;
		
		List<Long> ids = new ArrayList<Long>();
		for (RelatedItem item: ra.getItems()) {
			ids.add(item.articleId);
		}
		ids.add(id);
		List<Article> as = articleDao.get(ids, "title");
		HashMap<Long, String> id2title = new HashMap<Long, String>();
		for (Article a: as) {
			if (a != null) id2title.put(a.getId(), a.getTitle());
		}
		
		HashSet<String> nrs = extractNR(id2title.get(id));
		for (RelatedItem item: ra.getItems()) {
			if (item.score > 0.2) items.add(item);
			else {
				HashSet<String> rnrs = extractNR(id2title.get(item.articleId));
				rnrs.retainAll(nrs);
				if (rnrs.size() > 0) items.add(item);
			}
		}
		return items;
	}

	private HashSet<String> extractNR(String title) {
		HashSet<String> nrs = new HashSet<String>();
		if (title == null) return nrs;
		
		List<Keyword> keywords = dict.extract(Arrays.asList(title), 2, 5);
		for (Keyword kw: keywords) nrs.add(kw.getWord());
		return nrs;
	}
	
	public static <K, V> V get(HashMap<K, V> map, K key, V def) {
		V v = map.get(key);
		if (v == null) v = def;
		return v;
	}
}