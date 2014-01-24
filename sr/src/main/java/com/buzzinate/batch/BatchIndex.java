package com.buzzinate.batch;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.buzzinate.lezhi.api.Client;
import org.buzzinate.lezhi.api.Doc;
import org.buzzinate.lezhi.util.LargestTitle;
import org.buzzinate.lezhi.util.SignatureUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Status;
import com.buzzinate.keywords.Keyword;
import com.buzzinate.keywords.MobileKeywordsExtractor;
import com.buzzinate.main.MyModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class BatchIndex {
	public static void main(String[] args) throws ParseException {
		Injector injector = Guice.createInjector(new MyModule());
		Client client = injector.getInstance(Client.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		
		long maxId = articleDao.findMaxId() + 1;
		
		System.out.println("cluster state: " + client.state());
		int total = 0;
		int count = 1;
		while (count > 0) {
			count = 0;
			List<Doc> docs = new ArrayList<Doc>();
			for (Article a: articleDao.createQuery().filter("_id <", maxId).filter("status", Status.OK).order("-_id").limit(100).asList()) {
				try {
					Document doc = Jsoup.parse(a.getContent());
					doc.title(a.getTitle());
					doc.appendElement("meta").attr("name", "keywords").attr("content", a.getKeywords());
					List<Keyword> keywords = MobileKeywordsExtractor.extract(a.getUrl(), doc.outerHtml());
					
					String signature = SignatureUtil.signature(LargestTitle.parseLargest(a.getTitle()));
					List<String> kws = new ArrayList<String>();
					for (Keyword keyword: keywords) {
						if (StringUtils.contains(keyword.word(), "|")) continue;
						kws.add(String.format("%s|%s,%s", StringUtils.replace(keyword.word(), " ", "_"), keyword.freq(), keyword.field()));
					}
					docs.add(new Doc(a.getId(), a.getUrl(), a.getTitle(), signature, a.getThumbnail(), StringUtils.join(kws, " "), a.getCreateAt()));
					System.out.println(a.getTitle() + " ==> ");
					System.out.println(keywords);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (maxId > a.getId()) maxId = a.getId();
				
				count += 1;
			}
			client.bulkAdd(docs);
			total += count;
			System.out.println("total: " + total);
		}
		client.close();
	}
}