package com.buzzinate.link2;

import static com.buzzinate.common.util.Constants.CRAWL_QUEUE;
import static com.buzzinate.common.util.Constants.REPOST_QUEUE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import weibo4j2.model.Paging;
import weibo4j2.model.Status;

import com.buzzinate.common.dao.TrendDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.mongo.UserDaoImpl;
import com.buzzinate.common.message.LinkMessage;
import com.buzzinate.common.message.RepostMessage;
import com.buzzinate.common.model.Trend;
import com.buzzinate.common.model.User;
import com.buzzinate.common.queue.Handler;
import com.buzzinate.common.queue.MessageWrapper;
import com.buzzinate.common.queue.Queue;
import com.buzzinate.common.util.Constants;
import com.buzzinate.link.BlackSiteDetector;
import com.buzzinate.link.LinkInfo;
import com.buzzinate.link.TweetInfo;
import com.buzzinate.weibo.SinaWeiboClient;
import com.buzzinate.weibo.WeiboExt;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class RepostFetcherJob implements Runnable{

	private static Logger log = Logger.getLogger(RepostFetcherJob.class);
	
	private Queue repostQueue;
	private Queue crawlQueue;
	private UserDao userDao;
	private TrendDao trendDao;
	
	private BlackSiteDetector blackSiteDetector;
	private SinaWeiboClient client;
	private String accessToken;
	
	private Set<String> popTrends = new HashSet<String>();
	private Long lastGetPopTrendTime = 0l;
	
	@Inject
	public RepostFetcherJob(@Named(REPOST_QUEUE) Queue repostQueue, @Named(CRAWL_QUEUE) Queue crawlQueue, BlackSiteDetector blackSiteDetector, Datastore ds){
		
		this.repostQueue = repostQueue;
		this.crawlQueue = crawlQueue;
		this.userDao = new UserDaoImpl(ds);
		this.trendDao = new TrendDao(ds);
		this.blackSiteDetector = blackSiteDetector;
		this.client = new SinaWeiboClient();
		
	}
	
	@Override
	public void run() {
		Thread.currentThread().setName("repostfetcher-" + Thread.currentThread().getId());
		
		while (true) {
			this.accessToken = userDao.findByUid(1720400850L).getAccessToken();
			//accessToken="2.00AhNbrB8B8pgB749201f33dE6nD_E";
			try {
				repostQueue.receive(RepostMessage.class, new Handler<MessageWrapper<RepostMessage>>() {
					
					@Override
					public void on(MessageWrapper<RepostMessage> mw) {
						RepostMessage msg = mw.getMessage();
						try {
							String statusId = msg.statusId;
							List<String> urls = msg.urls;
							WeiboExt weibo = client.getWeibo(accessToken);
							List<TweetInfo> tweetInfos = new ArrayList<TweetInfo>();
							Paging paging = new Paging();
							paging.setCount(200);
							List<Status> reposts = weibo.getreposttimeline(statusId, paging);
							
							for (Status repost: reposts) {
								if (repost.getUser() != null) tweetInfos.add(new TweetInfo(repost, urls));
							}
							
							
							HashSet<String> shortUrls = new HashSet<String>();
							for(TweetInfo t: tweetInfos) {
								for (LinkInfo link: t.links) shortUrls.add(link.sinaUrl);
							}
							
							List<TweetInfo> tweetsHasLink = new ArrayList<TweetInfo>();
							if (shortUrls.isEmpty()) return; 
							
							
									
							Map<String, String> urlMaps = weibo.shortUrlExpand(new ArrayList<String>(shortUrls));
							for(TweetInfo t: tweetInfos) {
								t.expandLinks(blackSiteDetector, urlMaps);
								if (t.links.size() > 0) tweetsHasLink.add(t);
							}
							tweetInfos = tweetsHasLink;
							
							if(tweetInfos.isEmpty()) return;
							
							Collection<User> users = TweetInfo.toUsers(tweetInfos);
							log.info("users.size => " + users.size());

							HashMap<Long, Long> uid2id = new HashMap<Long, Long>();
							for (User nu : users)
								uid2id.put(nu.getUid(), nu.getId());
							for (TweetInfo ti : tweetInfos) {
								long userId = uid2id.get(ti.user.getUid());
								ti.user.setId(userId);
							}
							for (LinkMessage message : TweetInfo.toMessages(tweetInfos, getPopTrendsSet())) {
								try {
									crawlQueue.send(message);
									log.info("send repost link msg =>" + message.realUrl + " detail size: " + message.details.size());
								} catch (IOException e) {
									log.error(e);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							log.error("Exception  " + e);
						}
					}
				}, true);
			} catch (Throwable t) {
				t.printStackTrace();
				log.error("Error while fetching weibo", t);
			}
		}
	}
	
	private Set<String> getPopTrendsSet() {
		if (this.popTrends.isEmpty() || System.currentTimeMillis() - lastGetPopTrendTime > Constants.ONE_HOUR * 6) {
			List<Trend> popTrends = trendDao.findPop(20, 1);
			Set<String> popTrendsSet = new HashSet<String>();
			for (Trend trend : popTrends) {
				popTrendsSet.add(trend.getName());
			}
			this.popTrends = popTrendsSet;
			lastGetPopTrendTime = System.currentTimeMillis();
		}

		return this.popTrends;
	}
}
