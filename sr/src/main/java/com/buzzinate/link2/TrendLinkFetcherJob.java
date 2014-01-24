package com.buzzinate.link2;

import static com.buzzinate.common.util.Constants.CRAWL_QUEUE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import weibo4j2.model.Paging;
import weibo4j2.model.Status;

import com.buzzinate.common.dao.TrendDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.mongo.UserDaoImpl;
import com.buzzinate.common.message.LinkMessage;
import com.buzzinate.common.model.Trend;
import com.buzzinate.common.model.User;
import com.buzzinate.common.queue.Queue;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.StringUtil;
import com.buzzinate.dao.TweetDao;
import com.buzzinate.link.BlackSiteDetector;
import com.buzzinate.link.LinkInfo;
import com.buzzinate.link.LinkUtil;
import com.buzzinate.link.TweetInfo;
import com.buzzinate.util.Native2AsciiUtils;
import com.buzzinate.weibo.SinaWeiboClient;
import com.buzzinate.weibo.WeiboExt;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * 抓取话题对应的微博
 * 
 * @author Feeling
 * 
 */
public class TrendLinkFetcherJob implements Runnable {
	private static Logger log = Logger.getLogger(TrendLinkFetcherJob.class);

	private Queue crawlQueue;
	private BlackSiteDetector blackSiteDetector;

	private UserDao userDao;
	private TweetDao tweetDao;
	private TrendDao trendDao;

	private SinaWeiboClient client;

	@Inject
	public TrendLinkFetcherJob(@Named(CRAWL_QUEUE) Queue crawlQueue, BlackSiteDetector blackSiteDetector, Datastore ds) {
		this.crawlQueue = crawlQueue;
		this.blackSiteDetector = blackSiteDetector;
		this.client = new SinaWeiboClient();
		this.userDao = new UserDaoImpl(ds);
		this.tweetDao = new TweetDao(ds);
		this.trendDao = new TrendDao(ds);
	}

	@Override
	public void run() {
		Thread.currentThread().setName("trend-fetcher" + Thread.currentThread().getId());
		while (true) {
			String accessToken = userDao.findByUid(1720400850L).getAccessToken();
			// accessToken="2.00AhNbrB8B8pgB749201f33dE6nD_E";
			try {
				WeiboExt weibo = client.getWeibo(accessToken);
				List<String> hotTopics = getHotTopics();
				// 最热门的话题的创建时间最靠后
				for (int i = hotTopics.size() - 1; i >= 0; i--) {
					updateTrend(hotTopics.get(i));
					Thread.currentThread().sleep(100l);
				}
				for (String topic : hotTopics) {
					List<Status> statuses = weibo.getTopicTimeline(topic);
					List<TweetInfo> linkTweets = fetchLinkTweet(weibo, statuses, blackSiteDetector);
					for (TweetInfo ti : linkTweets)
						tweetDao.save(ti.toTweet());
					HashMap<Long, Long> uid2id = new HashMap<Long, Long>();
					for (User nu : TweetInfo.toUsers(linkTweets)) {
						userDao.saveOrUpdate(nu);
						uid2id.put(nu.getUid(), nu.getId());
					}
					for (TweetInfo ti : linkTweets) {
						long userId = uid2id.get(ti.user.getUid());
						ti.user.setId(userId);
					}
					for (LinkMessage message : TweetInfo.toTrendMessages(linkTweets, topic)) {
						try {
							crawlQueue.send(message);
							log.info("send trend msg =>" + message.realUrl + " detail size: " + message.details.size() + " topic => " + topic);
						} catch (IOException e) {
							log.error(e);
						}
					}
				}
			} catch (Throwable t) {
				log.error("Error while fetching weibo", t);
			} finally {
				try {
					Thread.sleep(Constants.ONE_HOUR * 6);
				} catch (InterruptedException e) {

				}
			}
		}
	}

	/**
	 * 直接抓取http://huati.weibo.com/得到热点话题
	 * 
	 * @param hotHuatiUrl
	 * @return
	 * @throws IOException
	 */
	private List<String> getHotTopics() throws IOException {
		List<String> hotHuatiList = new ArrayList<String>();
		DefaultHttpClient httpclient = new DefaultHttpClient();

		HttpClientParams.setCookiePolicy(httpclient.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);

		HttpResponse respGet = httpclient.execute(new HttpGet("http://huati.weibo.com"));
		EntityUtils.consume(respGet.getEntity());

		String cookieVal = null;
		List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getDomain() != null && cookie.getDomain().equalsIgnoreCase("huati.weibo.com") && cookie.getName().equalsIgnoreCase("USRHAWB")) {
				cookieVal = cookie.getValue();
			}
		}

		String url = "http://huati.weibo.com/aj_topiclist/big?ctg1=99&ctg2=0&prov=0&sort=time&p=1&t=1&_t=0&__rnd=" + System.currentTimeMillis();
		HttpGet huati = new HttpGet(url);

		huati.addHeader("host", "huati.weibo.com");
		huati.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:17.0) Gecko/20100101 Firefox/17.0");
		huati.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		huati.addHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		huati.addHeader("Accept-Encoding", "gzip, deflate");
		huati.addHeader("Connection", "keep-alive");
		huati.addHeader("Content-Type", "application/x-www-form-urlencoded");
		huati.addHeader("X-Requested-With", "XMLHttpRequest");
		huati.addHeader("Referer", "http://huati.weibo.com/");

		huati.addHeader("Cookie", cookieVal);

		HttpResponse respHuati = httpclient.execute(huati);
		String content = EntityUtils.toString(respHuati.getEntity(), "UTF-8");

		String regex = ">#([\\S\\s]+?)#<";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			hotHuatiList.add(Native2AsciiUtils.ascii2Native(matcher.group(1)));
		}
		return hotHuatiList;
	}

	private void updateTrend(String trendName) {
		byte[] hash = StringUtil.hash(trendName);
		// Boolean isNewTrend = false;
		Trend trend = trendDao.findByHash(hash);
		if (null == trend) {
			trend = new Trend();
			trend.setHash(hash);
			trend.setName(trendName);
			trend.setCreateAt(System.currentTimeMillis());
			trend.setCount(0l);
		} else {
			trend.setCreateAt(System.currentTimeMillis());
		}
		trendDao.save(trend);
	}

	private List<TweetInfo> fetchLinkTweet(WeiboExt weibo, List<Status> statuses, BlackSiteDetector blackSiteDetector) {
		List<TweetInfo> tweetInfos = new ArrayList<TweetInfo>();
		try {
			List<Status> flatten = new ArrayList<Status>();
			for (Status status : statuses) {
				if (status.getUser() == null)
					continue;
				flatten.add(status);
			}

			for (Status status : flatten) {
				Status retweet = status.getRetweetedStatus();
				List<String> urls = LinkUtil.extractLinks(status.getText());
				List<String> allurls = new ArrayList<String>();
				allurls.addAll(urls);
				if (retweet != null)
					allurls.addAll(LinkUtil.extractLinks(retweet.getText()));
				if (allurls.size() > 0) {
					if (status.getUser() != null && status.getUser().getId() != null) {
						tweetInfos.add(new TweetInfo(status, allurls));
					}
				}
				if (urls.size() > 0 && status.getRepostsCount() > 0) {
					Paging paging = new Paging();
					paging.setCount(200);
					List<Status> reposts = weibo.getreposttimeline(status.getId(), paging);
					for (Status repost : reposts) {
						if (repost.getUser() != null && repost.getUser().getId() != null) {
							tweetInfos.add(new TweetInfo(repost, urls));
						}
					}
				}
			}

			HashSet<String> shortUrls = new HashSet<String>();
			for (TweetInfo t : tweetInfos) {
				for (LinkInfo link : t.links)
					shortUrls.add(link.sinaUrl);
			}

			List<TweetInfo> tweetsHasLink = new ArrayList<TweetInfo>();
			Map<String, String> urlMaps = weibo.shortUrlExpand(new ArrayList<String>(shortUrls));
			for (TweetInfo t : tweetInfos) {
				t.expandLinks(blackSiteDetector, urlMaps);
				if (t.links.size() > 0)
					tweetsHasLink.add(t);
			}
			return tweetsHasLink;
		} catch (Exception e) {
			log.error("Exception while fetch links from user.", e);
			return tweetInfos;
		}
	}

	public static void main(String[] args) throws IOException {
		List<String> hotHuatiList = new ArrayList<String>();
		DefaultHttpClient httpclient = new DefaultHttpClient();

		HttpClientParams.setCookiePolicy(httpclient.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);

		HttpResponse respGet = httpclient.execute(new HttpGet("http://huati.weibo.com"));
		EntityUtils.consume(respGet.getEntity());

		String cookieVal = null;
		List<Cookie> cookies = httpclient.getCookieStore().getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getDomain() != null && cookie.getDomain().equalsIgnoreCase("huati.weibo.com") && cookie.getName().equalsIgnoreCase("USRHAWB")) {
				cookieVal = cookie.getValue();
			}
		}

		String url = "http://huati.weibo.com/aj_topiclist/big?ctg1=99&ctg2=0&prov=0&sort=time&p=1&t=1&_t=0&__rnd=" + System.currentTimeMillis();
		HttpGet huati = new HttpGet(url);

		huati.addHeader("host", "huati.weibo.com");
		huati.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:17.0) Gecko/20100101 Firefox/17.0");
		huati.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		huati.addHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		huati.addHeader("Accept-Encoding", "gzip, deflate");
		huati.addHeader("Connection", "keep-alive");
		huati.addHeader("Content-Type", "application/x-www-form-urlencoded");
		huati.addHeader("X-Requested-With", "XMLHttpRequest");
		huati.addHeader("Referer", "http://huati.weibo.com/");

		huati.addHeader("Cookie", cookieVal);

		HttpResponse respHuati = httpclient.execute(huati);
		String content = EntityUtils.toString(respHuati.getEntity(), "UTF-8");

		String regex = ">#([\\S\\s]+?)#<";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			hotHuatiList.add(Native2AsciiUtils.ascii2Native(matcher.group(1)));
		}
	}
}
