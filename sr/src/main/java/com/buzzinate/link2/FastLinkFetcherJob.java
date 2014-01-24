package com.buzzinate.link2;

import static com.buzzinate.common.util.Constants.CRAWL_QUEUE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import weibo4j2.model.Status;

import com.buzzinate.common.dao.TrendDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.UserWeiboDao;
import com.buzzinate.common.dao.mongo.UserDaoImpl;
import com.buzzinate.common.message.LinkMessage;
import com.buzzinate.common.model.Trend;
import com.buzzinate.common.model.User;
import com.buzzinate.common.model.UserWeibo;
import com.buzzinate.common.queue.Queue;
import com.buzzinate.common.util.Constants;
import com.buzzinate.dao.FetchInfoDao;
import com.buzzinate.dao.TweetDao;
import com.buzzinate.link.BlackSiteDetector;
import com.buzzinate.link.LinkInfo;
import com.buzzinate.link.LinkUtil;
import com.buzzinate.link.TweetInfo;
import com.buzzinate.model.FetchInfo;
import com.buzzinate.model.Tweet;
import com.buzzinate.util.TimeStats;
import com.buzzinate.weibo.SinaWeiboClient;
import com.buzzinate.weibo.WeiboExt;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * 抓取用户所对应的微博
 * 
 */
public class FastLinkFetcherJob implements Runnable {
	private static Logger log = Logger.getLogger(FastLinkFetcherJob.class);

	private Queue crawlQueue;
	private FetchInfoDao fiDao;
	private UserDao userDao;
	private UserWeiboDao uwDao;
	private TweetDao tweetDao;
	private BlackSiteDetector blackSiteDetector;
	private TrendDao trendDao;
	private SinaWeiboClient client;
	private MinhashAntiSpamLink linkSpam = new MinhashAntiSpamLink(250);

	private Set<String> popTrends = new HashSet<String>();
	private Long lastGetPopTrendTime = 0l;

	@Inject
	public FastLinkFetcherJob(@Named(CRAWL_QUEUE) Queue crawlQueue, BlackSiteDetector blackSiteDetector, Datastore ds) {
		this.crawlQueue = crawlQueue;
		this.fiDao = new FetchInfoDao(ds);
		this.userDao = new UserDaoImpl(ds);
		this.tweetDao = new TweetDao(ds);
		this.uwDao = new UserWeiboDao(ds);
		this.blackSiteDetector = blackSiteDetector;
		this.trendDao = new TrendDao(ds);
		client = new SinaWeiboClient();
	}

	@Override
	public void run() {
		Thread.currentThread().setName("fastfetcher-" + Thread.currentThread().getId());

		while (true) {
			String accessToken = userDao.findByUid(1720400850L).getAccessToken();
			//accessToken="2.00AhNbrB8B8pgB749201f33dE6nD_E";
			try {
				List<FetchInfo> fis = fiDao.findTop(100, System.currentTimeMillis() - Constants.ONE_HOUR * 4);
				log.info("Top user size=" + fis.size());

				for (FetchInfo fi : fis) {
					log.info("Processing user=" + fi);

					User user = userDao.findByUid(Long.parseLong(fi.uid));
					WeiboExt weibo = client.getWeibo(accessToken);
					List<Status> myStatuses = SinaWeiboClient.getUserStatus(weibo, fi.uid, fi.lastStatusId);
					List<Status> all = new ArrayList<Status>();
					all.addAll(myStatuses);
					all.addAll(SinaWeiboClient.getShareStatus(weibo, fi.uid, fi.lastStatusId));

					if (user != null && user.isLeziUser())
						saveLeziUserWeibos(user.getId(), myStatuses);

					List<TweetInfo> linkTweets = fetchLinkTweet(weibo, all, blackSiteDetector);
					log.info("linkTweets.size => " + linkTweets.size());

					List<Tweet> tweets = new ArrayList<Tweet>();
					for (TweetInfo ti : linkTweets)
						tweets.add(ti.toTweet());// tweetDao.save(ti.toTweet());
					tweetDao.save(tweets);

					Collection<User> users = TweetInfo.toUsers(linkTweets);
					log.info("users.size => " + users.size());
					userDao.saveOrUpdate(users);
					log.info("save user info done");

					HashMap<Long, Long> uid2id = new HashMap<Long, Long>();
					for (User nu : users)
						uid2id.put(nu.getUid(), nu.getId());
					for (TweetInfo ti : linkTweets) {
						long userId = uid2id.get(ti.user.getUid());
						ti.user.setId(userId);
					}
					for (LinkMessage message : TweetInfo.toMessages(linkTweets, getPopTrendsSet())) {
						try {
							crawlQueue.send(message);
							log.info("send link msg =>" + message.realUrl + " detail size: " + message.details.size());
						} catch (IOException e) {
							log.error(e);
						}
					}

					fi.lastFetchTime = System.currentTimeMillis();
					for (Status status : myStatuses) {
						if (fi.lastStatusId < status.getIdstr())
							fi.lastStatusId = status.getIdstr();
					}
					fiDao.save(fi);
					log.info("Done processing user=" + fi);

					ArrayList<FetchInfo> newfis = new ArrayList<FetchInfo>();
					for (Status status : all) {
						if (status.getUser() != null) {
							FetchInfo newfi = FetchInfo.from(status.getUser());
							if (newfi.score > 0)
								newfis.add(newfi);
						}
						Status retweet = status.getRetweetedStatus();
						if (retweet != null && retweet.getUser() != null) {
							FetchInfo newfi = FetchInfo.from(retweet.getUser());
							if (newfi.score > 0) newfis.add(newfi);
						}
					}
					fiDao.batchUpdate(newfis);

					TimeStats.infoLinkFetched();
				}
			} catch (Throwable t) {
				t.printStackTrace();
				log.error("Error while fetching weibo", t);
			}
		}
	}

	public List<TweetInfo> fetchLinkTweet(WeiboExt weibo, List<Status> statuses, BlackSiteDetector blackSiteDetector) {
		List<TweetInfo> tweetInfos = new ArrayList<TweetInfo>();
		try {
			List<Status> flatten = new ArrayList<Status>();
			for (Status status : statuses) {
				if (status.getUser() == null)
					continue;
				flatten.add(status);
				Status retweet = status.getRetweetedStatus();
				if (retweet != null && retweet.getUser() != null) {
					log.info("Retweet: " + retweet.getRepostsCount() + " => " + retweet.getText());
					flatten.add(retweet);
				}
			}

			for (Status status : flatten) {
				if (linkSpam.isFull() && linkSpam.checkSim(status.getText()) >= 0.4) {
					log.warn("May LinkSpam: " + status.getText());
					continue;
				}
				Status retweet = status.getRetweetedStatus();
				List<String> urls = LinkUtil.extractLinks(status.getText());
				List<String> allurls = new ArrayList<String>();
				allurls.addAll(urls);
				if (retweet != null) allurls.addAll(LinkUtil.extractLinks(retweet.getText()));
				if (allurls.size() > 0) {
					tweetInfos.add(new TweetInfo(status, allurls));
					log.info("urls size: " + urls.size() + "/" + allurls.size() + " => " + status.getText());
				}
			}

			HashSet<String> shortUrls = new HashSet<String>();
			for (TweetInfo t : tweetInfos) {
				for (LinkInfo link : t.links) shortUrls.add(link.sinaUrl);
			}

			List<TweetInfo> tweetsHasLink = new ArrayList<TweetInfo>();
			if (shortUrls.isEmpty()) return tweetsHasLink;

			Map<String, String> urlMaps = weibo.shortUrlExpand(new ArrayList<String>(shortUrls));
			
			for (TweetInfo t: tweetInfos) {
				boolean isAnti = false;
				for (LinkInfo li: t.links) {
					String realurl = urlMaps.get(li.sinaUrl);
					if (realurl != null && linkSpam.isAntiHost(realurl)) isAnti = true;
				}
				if (isAnti) linkSpam.add(t.text);
			}
			
			for (TweetInfo t : tweetInfos) {
				t.expandLinks(blackSiteDetector, urlMaps);
				if (t.links.size() > 0) tweetsHasLink.add(t);
			}
			tweetInfos = tweetsHasLink;

			return tweetInfos;
		} catch (Exception e) {
			log.error("Exception while fetch links from user.", e);
			return tweetInfos;
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

	private void saveLeziUserWeibos(long leziUserId, List<Status> statuses) {
		for (Status status : statuses) {
			System.out.println(status.getText());
			UserWeibo uw = new UserWeibo(leziUserId, status.getIdstr(), status.getText(), status.getCreatedAt().getTime());
			uwDao.save(uw);
			Status rt = status.getRetweetedStatus();
			if (rt != null && rt.getCreatedAt() != null) {
				// System.out.println(rt.getText());
				UserWeibo ruw = new UserWeibo(leziUserId, rt.getIdstr(), rt.getText(), rt.getCreatedAt().getTime());
				uwDao.save(ruw);
			}
		}
	}
}