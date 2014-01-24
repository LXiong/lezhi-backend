package com.buzzinate.crawl.classify;

import static com.buzzinate.common.util.Constants.KEY_VERTICAL_QUEUE_SIZE;
import static com.buzzinate.common.util.Constants.KEY_VERTICAL_THREAD_NUM;
import static com.buzzinate.common.util.Constants.KEY_VERTICAL_START_URL;
import static com.buzzinate.common.util.Constants.KEY_VERTICAL_CATEGORY;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.mongo.ArticleDaoImpl;
import com.buzzinate.common.model.Category;
import com.buzzinate.crawl.SiteTemplateService;
import com.buzzinate.crawl.queues.WorkQueues;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class VerticalCrawlerJob implements Runnable {
	private Logger log = Logger.getLogger(VerticalCrawlerJob.class);
	
	
	@Inject(optional = true)
	@Named(KEY_VERTICAL_QUEUE_SIZE)
	private int maxQueueSize = 1000;

	@Inject(optional = true)
	@Named(KEY_VERTICAL_THREAD_NUM)
	private int threadNum = 1;
	
	@Inject(optional = false)
	@Named(KEY_VERTICAL_START_URL)
	private String startUrl;
	
	@Inject(optional = false)
	@Named(KEY_VERTICAL_CATEGORY)
	private int category;
	
	

	private ArticleDao articleDao;
	private SiteTemplateService siteTpl;

	@Inject
	public VerticalCrawlerJob(Datastore ds) {
		this.articleDao = new ArticleDaoImpl(ds);
		this.siteTpl = new SiteTemplateService(ds);
	}

	@Override
	public void run() {
		
		log.info("start vertical crawler job");
		final WorkQueues<String> workQueues = new WorkQueues<String>(
				maxQueueSize);
		final VerticalCrawlThread[] crawlers = new VerticalCrawlThread[threadNum];
		try {
			workQueues.submit(startUrl,
					startUrl);
		} catch (InterruptedException e) {

		}
		Set<String> crawledUrlSets = Collections
				.synchronizedSet(new HashSet<String>());

		

		crawledUrlSets.add(startUrl);
		// ConcurrentHashMap crawledUrlSet = new ConcurrentHashMap();
		for (int i = 0; i < crawlers.length; i++) {

			crawlers[i] = new VerticalCrawlThread(workQueues, articleDao,
					siteTpl, crawledUrlSets,  maxQueueSize, startUrl, Category.getCategory(category));
			crawlers[i].start();
		}
		
		log.info("start to crawl page...");

//		while (!Thread.interrupted()) {
//			try {
//				if (workQueues.getSize() == 0) {
//					crawledUrlSets.clear();
//					log.info("crawled done this round ! , start next round crawled");
//					workQueues.submit("http://2012.sina.com.cn/",
//							"http://2012.sina.com.cn/");
//					crawledUrlSets.add("http://2012.sina.com.cn/");
//				}
//				Thread.sleep(60 * 1000);
//			} catch (InterruptedException e) {
//
//			}
//		}

		

	}

}
