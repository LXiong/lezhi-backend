package com.buzzinate.weibo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.buzzinate.common.util.Constants;

import weibo4j2.Oauth;
import weibo4j2.http.AccessToken;
import weibo4j2.model.Paging;
import weibo4j2.model.Status;
import weibo4j2.model.Trend;
import weibo4j2.model.Trends;
import weibo4j2.model.WeiboException;

public class SinaWeiboClient {
	private static Logger log = Logger.getLogger(SinaWeiboClient.class);
	private static final Oauth oauth = new Oauth();
	
	private static final String USER = "kun.xue@buzzinate.com";
	private static final String PASS = "c110py";
	//private static final String PASS = "cpy110py";
	
	private HashMap<String, WeiboExt> cachedWeibo = new HashMap<String, WeiboExt>();
	
	public SinaWeiboClient() {
	}
	
	public WeiboExt getWeibo(String accessToken) {
		WeiboExt weibo = cachedWeibo.get(accessToken);
		if (weibo == null || weibo.isExpired()) {
			long expiredTime = System.currentTimeMillis() + Constants.ONE_DAY * 20;
			weibo = new WeiboExt(expiredTime);
			weibo.setToken(accessToken);
			
			cachedWeibo.put(accessToken, weibo);
		}
		return weibo;
	}
	
	public WeiboExt getWeibo() throws WeiboException {
		WeiboExt weibo = cachedWeibo.get(USER);
		if (weibo == null || weibo.isExpired()) {
			AccessToken accessToken = oauth.authorize(USER, PASS);
			System.out.println(accessToken);
			long expiredTime = Long.parseLong(accessToken.getExpireIn()) * 1000 + System.currentTimeMillis();
			weibo = new WeiboExt(expiredTime);
			weibo.setToken(accessToken.getAccessToken());
			cachedWeibo.put(USER, weibo);
		}
		return weibo;
	}
	
	public static List<Status> getUserStatus(WeiboExt weibo, String uid, long sinceId) {
		try {
			Paging paging = new Paging();
			paging.setCount(200);
			if (sinceId > 0) paging.setSinceId(sinceId);
			return weibo.getUserTimeline(uid, paging);
		} catch (WeiboException e) {
			log.error("Failed to get the Sina Weibo Status. Reason: " + e.getMessage());
			return new ArrayList<Status>();
		}
	}
	
	public static List<Status> getShareStatus(WeiboExt weibo, String uid, long sinceId) {
		try {
			Paging paging = new Paging();
			paging.setCount(200);
			if (sinceId > 0) paging.setSinceId(sinceId);
			return weibo.getFriendsTimeline(paging);
		} catch (WeiboException e) {
			log.error("Failed to get the Sina Weibo Status. Reason: " + e.getMessage());
			return new ArrayList<Status>();
		}
	}
	
	public static Map<Long, Count> getCounts(WeiboExt weibo, List<Long> ids) {
		Map<Long, Count> counts = new HashMap<Long, Count>();
		int start = 0;
		int chunk = 100;
		while (start < ids.size()) {
			int to = Math.min(ids.size(), start + chunk);
			try {
				List<Long> subIds = ids.subList(start, to);
				List<Count> subCounts = weibo.getCountsExt(StringUtils.join(subIds, ","));
				for (Count subCount: subCounts) counts.put(subCount.getId(), subCount);
			} catch (WeiboException e) {
				log.error("Failed to get the tweet count. Reason: " + e.getMessage());
			}
			start += chunk;
		}
		return counts;
	}
	
	public static void main(String[] args) throws Exception {
		SinaWeiboClient client = new SinaWeiboClient();
		//WeiboExt weibo = client.getWeibo("2.00oqc7sB8B8pgB78a4d17ed322i_kB");
		WeiboExt weibo = client.getWeibo();
		
		Location loc = weibo.getLocation(116.31635, 39.97948);
		System.out.println(loc.getAddress());
		System.out.println("loc.getMore() => " + loc.getMore());
		
		List<Trends> trendsList = weibo.getTrends();
		for(Trends trends : trendsList){
			for(Trend trend: trends.getTrends()){
//				System.out.println("Query => " + trend.getQuery());
//				System.out.println("trend getDelta => " + trend.getDelta());
//				System.out.println("trend amount => " + trend.getAmount());
				List<Status> statuses = weibo.getTrendTimeline(trend);
				for(Status status : statuses){
					System.out.println(status.getText());
					System.out.println(status.getRepostsCount());
				}
				break;
//				System.out.println("statuses size => " + statuses.size());
//				for(Status status : statuses){
//				}
			}
		}
		//weibo.getTopicResult(trendsList);
//		List<Status> statuses = getUserStatus(weibo, "1642088277", -1);
//		List<Long> ids = new ArrayList<Long>();
//		for(Status status: statuses) {
//			System.out.println(status.getId() + " ==> " + status.getText());
//			ids.add(Long.parseLong(status.getId()));
//		}
//		if (ids.size() > 100) ids = ids.subList(0, 100);
//		Map<Long, Count> counts = getCounts(weibo, ids);
//		for (Count count: counts.values()) {
//			System.out.println(count);
//		}
//		System.out.println(weibo.shortUrlExpand(Arrays.asList("http://t.cn/zOXVT39", "http://t.cn/zOXIEoe", "zOXM22h")));
//		for (Status status: weibo.getShortUrlStatuses(Arrays.asList("http://t.cn/zOXVT39", "http://t.cn/zOXIEoe", "http://t.cn/zOXM22h"), 50)) {
//			
//			System.out.println(status.getText());
//		}
		
//		List<Status> statuses = getUserStatus(weibo, "2274659385", 3477223409216196L);
//		for (Status status: statuses) {
//			System.out.println(status.getId() + "/" + status.getUser().getScreenName() + "/" + status.getInReplyToUserId() + "->" + status.getText());
//			if (status.getRetweetedStatus() != null) {
//				Status retweet = status.getRetweetedStatus();
//				System.out.println(" >>>>>> " + retweet.getId() + "/" + retweet.getUser().getScreenName() + "/" + retweet.getInReplyToUserId() + "->" + retweet.getText());
//			}
//		}
	}
}
