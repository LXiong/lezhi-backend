package com.buzzinate.jianghu.jredis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ArticleProfileDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.ArticleProfile;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.util.Constants;
import com.buzzinate.jianghu.sr.cache.JCache;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.inject.Inject;

public class PrefCache {
	private static Logger log = Logger.getLogger(PrefCache.class);
	
	private JCache jcache;
	private PreferenceDao prefDao;
	private ArticleDao articleDao;
	private ArticleProfileDao apDao;
	
	@Inject
	public PrefCache(JCache jcache, PreferenceDao prefDao, ArticleDao articleDao, ArticleProfileDao apDao) {
		this.jcache = jcache;
		this.prefDao = prefDao;
		this.articleDao = articleDao;
		this.apDao = apDao;
	}
	
	public List<PrefData> fetchPrefIds(long userId) {
		final long since = System.currentTimeMillis() - Constants.ONE_DAY * 20;
		return jcache.syncs("prefdata-", userId, PrefData.class, Constants.ONE_HOUR * 2, new Function<Long, List<PrefData>>(){

			@Override
			public List<PrefData> apply(Long userId) {
				List<Preference> prefs = prefDao.createQuery().filter("userId", userId).filter("_id >", new ObjectId(new Date(since))).filter("createAt >", since).asList();
				HashMultimap<Long, RichPrefData> user2pds = fetchData(prefs);
				return RichPrefData.toPrefDatas(user2pds.get(userId));
			}
		});
	}
	
	private HashMultimap<Long, RichPrefData> fetchData(List<Preference> prefs) {
		HashMultimap<Long, RichPrefData> result = HashMultimap.create();
		
		HashSet<Long> ids = new HashSet<Long>();
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
				if (a.getStatus() == Status.OK) result.put(pref.getUserId(), new RichPrefData(a.getId(), words, a.getCreateAt(), a.getPrefSize(), pref.getCreateAt()));
				else result.put(pref.getUserId(), new RichPrefData(a.getId(), words, PrefData.MISS, a.getPrefSize(), pref.getCreateAt()));
			}
		}
		
		return result;
	}
	
	public static class RichPrefData {
		private PrefData prefData;
		private long prefTime;
		
		public RichPrefData(long id, List<String> words, long createAt, int size, long prefTime) {
			this.prefData = new PrefData(id, words, createAt, size);
			this.prefTime = prefTime;
		}
		
		public PrefData getPrefData() {
			return prefData;
		}
		
		public long getPrefTime() {
			return prefTime;
		}
		
		public static List<PrefData> toPrefDatas(Collection<RichPrefData> rpds) {
			List<PrefData> pds = new ArrayList<PrefData>();
			for (RichPrefData rpd: rpds) pds.add(rpd.getPrefData());
			return pds;
		}
	}
}