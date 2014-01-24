package com.buzzinate.main;

import org.apache.log4j.Logger;

import com.buzzinate.classify.ClassifyJob;
import com.buzzinate.crawl.PageCrawlerJob;
import com.buzzinate.link2.AreaLinkFetcherJob;
import com.buzzinate.link2.RssFetcherJob;
import com.buzzinate.link2.FastLinkFetcherJob;
import com.buzzinate.link2.TrendLinkFetcherJob;
import com.buzzinate.util.TimeStats;
import com.google.inject.Guice;
import com.google.inject.Injector;


public class MainApp {
	private static Logger log = Logger.getLogger(MainApp.class);
	
	public static void main(String[] args) {
		System.setProperty("weibo4j.debug", "false");
		try {
			Injector injector = Guice.createInjector(new MyModule(), new QueueModule());
			
			new Thread(injector.getInstance(FastLinkFetcherJob.class)).start();
			new Thread(injector.getInstance(PageCrawlerJob.class)).start();
			new Thread(injector.getInstance(AreaLinkFetcherJob.class)).start();
			new Thread(injector.getInstance(ClassifyJob.class)).start();
			new Thread(injector.getInstance(TrendLinkFetcherJob.class)).start();
			new Thread(injector.getInstance(RssFetcherJob.class)).start();
			
			while (!Thread.interrupted()) {
				Thread.sleep(10000L);
				TimeStats.output();
			} 
		} catch (Throwable t) {
			log.error("Main Error", t);
		}
    }
}
