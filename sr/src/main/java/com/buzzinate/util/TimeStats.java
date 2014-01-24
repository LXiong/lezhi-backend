package com.buzzinate.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

public class TimeStats {
private static Logger log = Logger.getLogger(TimeStats.class);
	
	private static AtomicLong lastLinkFetchTime = new AtomicLong(System.currentTimeMillis());
	private static AtomicLong lastCrawlTime = new AtomicLong(System.currentTimeMillis());
	private static AtomicLong lastUpdateSimTime = new AtomicLong(System.currentTimeMillis());
	private static AtomicInteger activeThreads = new AtomicInteger(0);
	
	private TimeStats() {
		
	}
	
	public static void infoLinkFetched() {
		lastLinkFetchTime.set(System.currentTimeMillis());
	}
	
	public static void infoCrawled() {
		lastCrawlTime.set(System.currentTimeMillis());
	}
	
	public static void infoSimUpdated() {
		lastUpdateSimTime.set(System.currentTimeMillis());
	}
	
	public static void infoThreadStart() {
		activeThreads.incrementAndGet();
	}
	
	public static void infoThreadEnd() {
		activeThreads.decrementAndGet();
		log.warn("Crawl thread terminate");
	}

	public static void output() {
		long now = System.currentTimeMillis();
		log.info("ActiveThreads=" + activeThreads.get());
		log.info("Since Last Fetch Link: " + (now - lastLinkFetchTime.get()) / 1000.0);
		log.info("Since Last Crawl: " + (now - lastCrawlTime.get()) / 1000.0);
		log.info("Since Last Update Sim: " + (now - lastUpdateSimTime.get()) / 1000.0);
	}
}
