package com.buzzinate.batch;

import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.main.MyModule;
import com.buzzinate.political.PoliticalFilter;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class CleanPoliticalArticles {
	private static Logger log = Logger.getLogger(CleanPoliticalArticles.class);
	
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new MyModule());
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		
		long maxid = articleDao.findMaxId();
		for (long start = 0; start <= maxid; start += 1000) {
			List<Article> as = articleDao.createQuery().filter("_id >=", start).filter("_id <", start + 1000).asList();
			for (Article a: as) {
				String text = a.getTitle() + a.getSummary();
				if (a.getContent() != null) text = text + Jsoup.parse(a.getContent()).text();
				if (PoliticalFilter.hasPoliticalKeywords(text)) {
					System.out.println(a.getId() + ", " + a.getUrl() + " ===> " + a.getTitle());
					log.info(a.getId() + ", " + a.getUrl() + " ===> " + a.getTitle());
					articleDao.deleteById(a.getId());
				}
			}
		}
	}
}