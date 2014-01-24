package com.buzzinate.link2;

import static com.buzzinate.common.util.Constants.CRAWL_QUEUE;

import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

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

/**
 * 将Rss内容源加入乐知文章数据库 
 * 
 * @author feeling
 * 
 */
public class RssFetcherJob implements Runnable {
	private static Logger log = Logger.getLogger(RssFetcherJob.class);

	private Queue crawlQueue;

	@Inject
	public RssFetcherJob(@Named(CRAWL_QUEUE) Queue crawlQueue) {
		this.crawlQueue = crawlQueue;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("rss-fetcher" + Thread.currentThread().getId());
		List<String> rssList = RssResource.getRssList();
		try {
			while (true) {
				for (String rssUrl : rssList) {
					try {
						SyndFeedInput input = new SyndFeedInput();
						SyndFeed feed = input.build(new XmlReader(new URL(rssUrl)));

						List<SyndEntry> entries = feed.getEntries();
						for (SyndEntry entry : entries) {
							long pubTime = System.currentTimeMillis();
							if(entry.getPublishedDate() != null){
								pubTime = entry.getPublishedDate().getTime();
							}
							String url = entry.getLink();
							LinkMessage msg = new LinkMessage(url, pubTime);
							crawlQueue.send(msg);
							Thread.sleep(2000);
							log.info("From rss : send msg =>" + msg.realUrl);
						}
					} catch (Exception e) {
						log.error("RSS " + rssUrl + " => " + e.getMessage());
					}
				}
				Thread.sleep(Constants.ONE_HOUR * 6);
			}
		} catch (Throwable t) {
			log.error("Error while fetching ebrun rss article", t);
		}
	}
}