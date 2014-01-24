package com.buzzinate.batch;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.net.ssl.SSLEngineResult.Status;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ArticleProfileDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.Preference;
import com.buzzinate.crawl.core.util.ExtractUtils;
import com.buzzinate.crawl.simhash.SimHash;
import com.buzzinate.keyword.KeywordUtil;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ReextractKeywords {
	private static Logger log = Logger.getLogger(ReextractKeywords.class);
	
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		UserDao userDao = injector.getInstance(UserDao.class);
		PreferenceDao prefDao = new PreferenceDao(ds);
		
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		ArticleProfileDao apDao = new ArticleProfileDao(ds);
		Dictionary dict = injector.getInstance(Dictionary.class);
		
		long minId = 7270387L; //production
//		long minId = 3583328L;
		long maxId = articleDao.findMaxId();
		
		for (long start = minId; start < maxId; start += 500) {
			List<Article> as = articleDao.createQuery().retrievedFields(true, "title", "keywords", "content", "createAt").filter("_id >=", start).filter("_id <", start + 500).filter("status", Status.OK).asList();
			for (Article a: as) {
				try {
					Element body = Jsoup.parse(a.getContent()).body();
					List<Integer> minHashes = SimHash.calcMinHash(body.text());
					apDao.updateMinhash(a.getId(), a.getTitle(), minHashes, a.getCreateAt());
					List<String> sentences = ExtractUtils.splitSentences(body);
					sentences.add(a.getKeywords());
					List<Keyword> keywords = KeywordUtil.extract(dict, a.getTitle(), sentences);
					if (!keywords.isEmpty()) {
						log.info("extract id=" + a.getId() + " ==> " + keywords);
						apDao.updateKeywords(a.getId(), keywords, a.getCreateAt());
					}
				} catch (Exception e) {
					log.error("error: " + a.getId(), e);
				}
			}
		}
		
		List<Long> leziUserIds = userDao.findLeziUserIds();
		HashSet<Long> pageIds = new HashSet<Long>();
		List<Preference> prefs = prefDao.createQuery().filter("userId in", leziUserIds).filter("pageId <", minId).asList();
		for (Preference pref: prefs) pageIds.add(pref.getPageId());
		List<Article> as = articleDao.createQuery().retrievedFields(true, "title", "keywords", "content").filter("_id in", pageIds).asList();
		for (Article a: as) {
			if (a.getContent() == null || a.getTitle() == null) continue;
			try {
				Element body = Jsoup.parse(a.getContent()).body();
				List<String> sentences = ExtractUtils.splitSentences(body);
				sentences.add(a.getKeywords());
				List<Keyword> keywords = KeywordUtil.extract(dict, a.getTitle(), sentences);
				if (!keywords.isEmpty()) {
					log.info("extract id=" + a.getId() + " ==> " + keywords);
					apDao.updateKeywords(a.getId(), keywords, a.getCreateAt());
				}
			} catch (Exception e) {
				log.error("error: " + a.getId(), e);
			}
		}
	}
}