package com.buzzinate.crawl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;
import com.buzzinate.crawl.core.util.URLCanonicalizer;

public class TestURLClassify {

	private ArrayList<String> urls = new ArrayList<String>();
	private HashMap<String, String> resultMap = new HashMap<String, String>();

	class Token {
		Integer lv = -1;
		String str = "";

		public Token(Integer lv, String str) {
			this.lv = lv;
			this.str = str;
		}
		
		//public int hashCode = str.hashCode();
		
		public int hashCode(){
			return (str+lv).hashCode();
		}
	}

	public TestURLClassify(ArrayList<String> urls) {
		this.urls = urls;
	}

	public void produceResult() throws IOException {
		for (String url : urls) {
			Response resp = PageFetcher.fetch(url);
			if (resp.getStatusCode() >= 400)
				throw new IOException("status=" + resp.getStatusCode());

			Document doc = resp.getDoc();

			Element body = Jsoup.parse(resp.getDoc().html()).body();

			Elements es = body.getElementsByTag("a");

			for (int i = 0; i < es.size(); i++) {
				Element e = es.get(i);
				String href = URLCanonicalizer.getCanonicalURL(e.attr("href"),
						url);
				
				//href.split("/")
				// if (href.replaceAll("http://", "").startsWith(
				// OLYMPIC_SITESUFFIX)) {
				// if (!mayDuplicate(href) && !crawledUrlSets.contains(href)) {
				// if (queue.getSize() <= maxQueueSize * 0.8) {
				// queue.submit(href, href);
				// synchronized (this) {
				// crawledUrlSets.add(href);
				// }
				// } else {
				// log.info("remove cururl => " + url);
				// synchronized (this) {
				// //
				// 这个url的子链接没有被完全抓取到，故从crawledUrlSets中删除掉，将来碰到这个url，会重新抓取他的子链接
				// crawledUrlSets.remove(url);
				// }
				// break;
				// }
				// }
				// }
			}
		}
	}

	public void printResult() {
		for (Entry<String, String> e : resultMap.entrySet()) {
			System.out.println(e.getKey() + " => " + e.getValue());
		}
	}

	public ArrayList<Token> getTokens(String siteprefix, String url) {
		ArrayList<Token> res = new ArrayList<Token>();
		String sufUrl = url.replaceAll(siteprefix, "");

		String[] splits = sufUrl.split("/");

		for (int i = 0; i < splits.length; i++) {
			res.add(new Token(i, splits[i]));
		}
		return res;
	}

	public static void main(String[] args) {
		String sufUrl = "http://www.guokr.com/YouthCon2012/?index=event";
		String[] splits = sufUrl.split("/");
		System.out.println(splits.length);
	}
}
