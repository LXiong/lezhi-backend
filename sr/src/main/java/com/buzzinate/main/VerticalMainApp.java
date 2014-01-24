package com.buzzinate.main;

import org.apache.log4j.Logger;

import com.buzzinate.crawl.classify.VerticalCrawlerJob;
import com.buzzinate.util.TimeStats;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class VerticalMainApp {
	private static Logger log = Logger.getLogger(VerticalMainApp.class);

	public static void main(String[] args) {
		System.setProperty("weibo4j.debug", "false");
		try {
			Injector injector = Guice.createInjector(new MyModule(), new QueueModule());
			new Thread(injector.getInstance(VerticalCrawlerJob.class)).start();
			while (!Thread.interrupted()) {
				Thread.sleep(10000L);
				TimeStats.output();
			}
		} catch (Throwable t) {
			log.error("VerticalMain Error", t);
		}
	}
}
