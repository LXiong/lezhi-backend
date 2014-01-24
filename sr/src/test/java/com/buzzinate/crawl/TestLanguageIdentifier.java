package com.buzzinate.crawl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;
import com.buzzinate.util.LanguageDetector;
import com.cybozu.labs.langdetect.LangDetectException;


public class TestLanguageIdentifier {
	public static void main(String[] args) throws LangDetectException, IOException{
		
		List<String> urls = Arrays.asList(
				"http://mvnrepository.com/artifact/junit/junit/4.10",
				"http://news.nate.com/view/20120919n09216",
				"http://news.sina.com.cn/c/2012-09-19/105125206792.shtml",
				"http://host.cc.ntu.edu.tw/sec/schinfo/schinfo_asp/ShowContent.asp?num=1106&sn=11109",
				"http://www.u-tokyo.ac.jp/gen03/kouhou_j.html"
		);
		
		for(String url : urls){
			Response resp = PageFetcher.fetch(url);
			String title = resp.getDoc().title();
			Boolean isTargetLanguage = LanguageDetector.isTargetLanguage(url, title);
			System.out.println("title => " + title);
			System.out.println("isTargetLanguage => " + isTargetLanguage);
		}
		
	}
}
