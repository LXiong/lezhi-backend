package com.buzzinate.crawl.core.test;

import java.io.IOException;

import org.jsoup.nodes.Document;

import com.buzzinate.crawl.core.extract.ArticleText;
import com.buzzinate.crawl.core.extract.TextExtractor;
import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;

public class TestSVD {
	public static void main(String[] args) throws IOException {
//		String url = "http://www.51sushi.net/news/13606768-1.html";
//		String url = "http://www.20ju.com/content/V183509.htm?bsh_bid=32540792";
//		String url = "http://blog.sina.com.cn/s/blog_87572b250100t43p.html";
//		String url = "http://shanghai.kankanews.com/vods/ff80808129b7f7280129b8af59770334/ff808081335e143201335f4d071a0301/#";
//		String url = "http://news.ifeng.com/society/2/detail_2011_11/02/10339606_0.shtml";
//		String url = "http://www.secon.cn/Article/xwzx/2011/11/02/95854.html";
//		String url = "http://news.ifeng.com/society/1/detail_2011_11/02/10342590_0.shtml";
//		String url = "http://coolshell.cn/articles/5701.html?jtss=tsina";
//		String url = "http://cn.wsj.com/gb/20111031/bch073550.asp?source=whatnews2";
//		String url = "http://movie.douban.com/review/5143430/";
//		String url = "http://net.chinabyte.com/148/12194148.shtml";
//		String url = "http://www.dospy.com/news/trade/nokia/2011-11-04/10150.html";
//		String url = "http://www.gn00.com/forum.php?mod=viewthread&tid=34814&extra=";
//		String url = "http://www.bioon.com/industry/enterprisenews/509875.shtml";
		String url = "http://gamerboom.com/archives/32659";
//		String url = "http://www.tianya.cn/publicforum/content/feeling/1/1900795.shtml";
		Response resp = PageFetcher.fetch(url);
		Document doc = resp.getDoc();
		System.out.println("status: " + resp.getStatusCode());
		System.out.println(doc.select("meta[name=description]").attr("content"));
		ArticleText text = TextExtractor.extract(doc.body(), doc.title(), url);
		
		System.out.println("---------------");
		System.out.println(text.getContent());
	}
}