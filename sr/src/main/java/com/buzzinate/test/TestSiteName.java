package com.buzzinate.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;
import com.buzzinate.crawl.core.util.URLCanonicalizer;
import com.buzzinate.dao.RawArticleDao;
import com.buzzinate.main.MyModule;
import com.buzzinate.model.RawArticle;
import com.buzzinate.util.DomainNames;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestSiteName {
	private static final List<String> logoNames = Arrays.asList("logo", "header", "brand");

	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		RawArticleDao raDao = new RawArticleDao(ds);
		
		List<String> ids = raDao.getIds(raDao.createQuery().limit(1000));
		for (int start = 0; start < ids.size(); start += 100) {
			List<RawArticle> ras = raDao.get(ids.subList(start, start + 100));
			for (RawArticle ra: ras) {
				Document doc = Jsoup.parse(ra.content, ra.url);
				findSite(ra.url, ra.title, doc);
			}
		}
		
//		String url = "http://blog.motorola.com.cn/?p=1407";
//		String url = "http://www.williamlong.info/archives/3117.html";
//		String url = "http://www.infoq.com/cn/news/2012/06/baidu-salon-26th-summary";
//		Response resp = PageFetcher.fetch(url);
//		findSite(url, resp.getDoc().title(), resp.getDoc());
	}
	
	public static String findSite(String url, String title, Document doc) {
		List<Element> result = findLogos(doc.body(), url);
		PriorityQueue<Integer, String> slpq = PriorityQueue.make(5, SortUtils.comp(Integer.class), SortUtils.reverse(String.class));
		for (Element e: result) {
			String href = URLCanonicalizer.getCanonicalURL(e.attr("href"), url);
			String text = e.text().trim();
			if (text.length() > 0 && title.contains(text)) slpq.add(href.length(), text);
		}
		System.out.println(slpq.values());
		System.out.println("======================");
		return "";
	}
	
	private static List<Element> findLogos(Element root, String url) {
		List<Element> result = new ArrayList<Element>();
		Elements links = root.getElementsByTag("a");
		for (Element link: links) {
			if (link.hasAttr("href")) {
				String href = URLCanonicalizer.getCanonicalURL(link.attr("href"), url);
				if (href.startsWith("http://") && url.startsWith(href)) {
					result.add(link);
				}
			}
		}
		return result;
	}
}