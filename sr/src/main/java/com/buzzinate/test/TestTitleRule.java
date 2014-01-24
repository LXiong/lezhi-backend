package com.buzzinate.test;

import java.util.List;

import org.jsoup.Jsoup;

import com.buzzinate.crawl.core.tpl.TitleExtractor;
import com.buzzinate.crawl.core.util.TextSnippet;
import com.buzzinate.crawl.core.util.TextUtility;
import com.buzzinate.dao.RawArticleDao;
import com.buzzinate.main.MyModule;
import com.buzzinate.model.RawArticle;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestTitleRule {

	public static void main(String[] args) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		System.out.println(sdf.format(1336268406000L));
//		
//		if (true) return;
		
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		RawArticleDao raDao = new RawArticleDao(ds);
		
		TitleExtractor te = new TitleExtractor();
		List<String> ids = raDao.getIds(raDao.createQuery().limit(10000));
		for (int start = 0; start < ids.size(); start += 100) {
			List<RawArticle> ras = raDao.get(ids.subList(start, start + 100));
			for (RawArticle ra: ras) {
				String origTitle = ra.title;
				String simpleTitle = te.extract(origTitle);
				
				TextSnippet titleSnippet = new TextSnippet(origTitle);
				String bestTitle = titleSnippet.searchBest(Jsoup.parse(ra.content).text());
				
				if (simpleTitle == null || bestTitle == null) {
//					System.out.println(origTitle + "\nSimple:  " + simpleTitle + " <==> \ncomplex: " + bestTitle);
				} else {
					int diff = TextUtility.diff(simpleTitle, bestTitle);
					if (diff * 3 > simpleTitle.length() && !bestTitle.contains(simpleTitle)) {
						System.out.println(origTitle + "\nSimple:  " + simpleTitle + " <==> \ncomplex: " + bestTitle);
					}
				}
			}
		}
	}
}