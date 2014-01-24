package com.buzzinate.crawl.core.test;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.buzzinate.crawl.core.extract.ArticleText;
import com.buzzinate.crawl.core.extract.TextExtractor;
import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;

public class Test {

	public static void main(String[] args) throws IOException {
//		String url = "http://www.socialbeta.cn/articles/the-paradox-of-weibo-operator.html";
		String url = "http://cd.qq.com/news/newshot/hotnewsrss.xml";
		Response resp = PageFetcher.fetch(url);
		Document doc = resp.getDoc();
		System.out.println("status: " + resp.getStatusCode());
		ArticleText text = TextExtractor.extract(doc.body(), doc.title(), url);
		System.out.println("HTML: " + doc.body().html());
	}
}