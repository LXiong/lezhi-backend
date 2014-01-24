package com.buzzinate.keyword;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.model.Article;
import com.buzzinate.crawl.core.util.ExtractUtils;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestKeywords {
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		PreferenceDao prefDao = new PreferenceDao(ds);
		Dictionary dict = injector.getInstance(Dictionary.class);
		
		long userId = 12071L;
		List<Long> prefs = prefDao.findLatestItems(userId, 200);
		for (Article a: articleDao.get(prefs)) {
			if (a == null || a.getContent() == null) continue;
			Element body = Jsoup.parse(a.getContent()).body();
			List<String> texts = ExtractUtils.splitSentences(body);
			texts.add(a.getTitle());
			texts.add(a.getKeywords());
			System.out.println(KeywordUtil.extract(dict, a.getTitle(), texts));
			System.out.println();
		}
	}

	
}