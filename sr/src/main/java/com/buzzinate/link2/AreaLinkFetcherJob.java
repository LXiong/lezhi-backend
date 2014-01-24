package com.buzzinate.link2;

import static com.buzzinate.common.util.Constants.CRAWL_QUEUE;

import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;


import com.buzzinate.common.geo.GeoInfo;
import com.buzzinate.common.message.LinkMessage;
import com.buzzinate.common.queue.Queue;
import com.buzzinate.common.util.Constants;
import com.buzzinate.resource.RssResource;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class AreaLinkFetcherJob implements Runnable {
	private static Logger log = Logger.getLogger(AreaLinkFetcherJob.class);

	private Queue crawlQueue;


	@Inject
	public AreaLinkFetcherJob(@Named(CRAWL_QUEUE) Queue crawlQueue) {
		this.crawlQueue = crawlQueue;
	}
	
	@Override
	public void run() {
		Thread.currentThread().setName("areaInfo-fetcher" + Thread.currentThread().getId());
		List<Entry<String, GeoInfo>> rssList = RssResource.getGeoRssList();
		try {
			while (true) {
				for (Entry<String, GeoInfo> rssInfo : rssList) {
					String rssUrl = rssInfo.getKey();
					GeoInfo geoInfo = rssInfo.getValue();
					try {
						
						SyndFeedInput input = new SyndFeedInput();
						SyndFeed feed = input.build(new XmlReader(new URL(
								rssUrl)));

						List<SyndEntry> entries = feed.getEntries();
						for (SyndEntry entry : entries) {
							String url = entry.getLink();
							LinkMessage msg = new LinkMessage(url, geoInfo);
							crawlQueue.send(msg);
							log.info("RSS area resource  : send msg =>" + msg.realUrl);
						}
					} catch (Exception e) {
						log.error("ERROR : RSS area resource" + rssUrl + " => " + e.getMessage());
					}
				}
				Thread.sleep(Constants.ONE_HOUR * 6);
			}
		} catch (Throwable t) {
			log.error("Error while fetching areaInfo", t);
		}
	}
	
	public static void main(String[] args){
		List<Entry<String, GeoInfo>> rssList = RssResource.getGeoRssList();
		try {
			while (true) {
				for (Entry<String, GeoInfo> rssInfo : rssList) {
					String rssUrl = rssInfo.getKey();
					GeoInfo geoInfo = rssInfo.getValue();
					try {
						
						SyndFeedInput input = new SyndFeedInput();
						SyndFeed feed = input.build(new XmlReader(new URL(
								rssUrl)));
						System.out.println(geoInfo);
						List<SyndEntry> entries = feed.getEntries();
						for (SyndEntry entry : entries) {
							String url = entry.getLink();
							LinkMessage msg = new LinkMessage(url, geoInfo);
							//Thread.sleep(2000);
							System.out.println("RSS area resource  : send msg =>" + msg.realUrl);
						}
					} catch (Exception e) {
						System.out.println("ERROR : RSS area resource" + rssUrl + " => " + e.getMessage());
					}
				}
				Thread.sleep(Constants.ONE_HOUR * 2);
			}
		} catch (Throwable t) {
			log.error("Error while fetching areaInfo", t);
		}
	}
}
