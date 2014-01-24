package com.buzzinate.crawl;

import static com.buzzinate.common.util.Constants.KEY_QUEUE_SIZE;
import static com.buzzinate.common.util.Constants.KEY_THREAD_NUM;
import static com.buzzinate.common.util.Constants.CRAWL_QUEUE;
import static com.buzzinate.common.util.Constants.CLASSIFY_QUEUE;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.buzzinate.lezhi.api.Client;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.mongo.ArticleDaoImpl;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.message.LinkMessage;
import com.buzzinate.common.queue.Handler;
import com.buzzinate.common.queue.MessageWrapper;
import com.buzzinate.common.queue.Queue;
import com.buzzinate.crawl.queues.WorkQueues;
import com.buzzinate.link.BlackSiteDetector;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * 抓取文章，监听link queue，启动多线程抓取，进行网页正文抽取
 * 
 * @author Yeming
 *
 */
public class PageCrawlerJob implements Runnable {
	private Logger log = Logger.getLogger(PageCrawlerJob.class);
	
	@Inject(optional = true) @Named(KEY_QUEUE_SIZE)
	private int maxQueueSize = 1000;
	
	@Inject(optional = true) @Named(KEY_THREAD_NUM)
	private int threadNum = 20;
	
	private Queue crawlQueue;
	private Queue classifyQueue;
	
	private PreferenceDao prefDao;
	private ArticleDao articleDao;
	private BlackSiteDetector blackSiteDetector;
	private SiteTemplateService siteTpl;
	private Client client;
	private Dictionary dict;

	@Inject
	public PageCrawlerJob(@Named(CRAWL_QUEUE) Queue crawlQueue, 
			@Named(CLASSIFY_QUEUE) Queue classifyQueue,
			BlackSiteDetector blackSiteDetector,
			Datastore ds,
			Client client,
			Dictionary dict) {
		this.prefDao = new PreferenceDao(ds);
		this.articleDao = new ArticleDaoImpl(ds);
		this.crawlQueue = crawlQueue;
		this.classifyQueue = classifyQueue;
		this.blackSiteDetector = blackSiteDetector;
		this.siteTpl = new SiteTemplateService(ds);
		this.client = client;
		this.dict = dict;
	}
	
	@Override
	public void run() {
		try {
			final WorkQueues<MessageWrapper<LinkMessage>> workQueues = new WorkQueues<MessageWrapper<LinkMessage>>(maxQueueSize);
			final CrawlThread[] crawlers = new CrawlThread[threadNum];
			for (int i = 0; i < crawlers.length; i++) {
				crawlers[i] = new CrawlThread(workQueues, crawlQueue, classifyQueue, prefDao, articleDao, blackSiteDetector, client, siteTpl, dict);
				crawlers[i].start();
			}
			
			log.info("start to crawl page...");
			crawlQueue.receive(LinkMessage.class, new Handler<MessageWrapper<LinkMessage>>() {
				
				@Override
				public void on(MessageWrapper<LinkMessage> mw) {
					LinkMessage msg = mw.getMessage();
					String url = msg.realUrl;
					log.debug("submit " + url);
					if (url == null) url = "http://t.cn/" + msg.sinaUrl;
					try {
						workQueues.submit(url, mw);
					} catch (InterruptedException e) {
						log.warn(e);
					}
				}
			}, false);
		} catch(IOException e) {
			log.error(e);
		}
	}
}
