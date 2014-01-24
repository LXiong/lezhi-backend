package com.buzzinate.batch;

import java.io.IOException;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.TextNode;

import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;

public class CrawlRssFeeds {

	public static void main(String[] args) throws IOException {
		crawlBlogread();
	}

	private static void crawlBlogread() throws IOException {
		for (int p = 1; p <= 15; p++) {
			String url = "http://blogread.cn/it/site.php?page=" + p;
			Response resp = PageFetcher.fetch(url);
			Elements feeds = resp.getDoc().select(":containsOwn(查看RSS)");
			for (Element fe: feeds) {
				TextNode next = (TextNode) fe.nextSibling();
				if (next.text().contains("2012-")) {
					System.out.println(fe.absUrl("href"));
				}
			}
		}
	}

}
