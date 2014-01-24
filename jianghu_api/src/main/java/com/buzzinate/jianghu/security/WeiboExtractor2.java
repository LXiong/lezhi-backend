package com.buzzinate.jianghu.security;

import java.util.ArrayList;
import java.util.List;

import weibo4j2.Timeline;
import weibo4j2.Weibo;
import weibo4j2.model.Paging;
import weibo4j2.model.Status;
import weibo4j2.model.WeiboException;

import com.buzzinate.common.model.UserWeibo;

public class WeiboExtractor2 {
	public static List<UserWeibo> extractWeibos(long userId, String uid, String accessToken) {
		List<UserWeibo> weibos = new ArrayList<UserWeibo>();
		Weibo.client.setToken(accessToken);
		List<Status> statuses = getUserStatus(uid, accessToken, -1);
		for (Status status: statuses) {
			UserWeibo uw = new UserWeibo(userId, status.getIdstr(), status.getText(), status.getCreatedAt().getTime());
			weibos.add(uw);
			Status rt = status.getRetweetedStatus();
			if (rt != null && rt.getCreatedAt() != null) {
				UserWeibo ruw = new UserWeibo(userId, rt.getIdstr(), rt.getText(), rt.getCreatedAt().getTime());
				weibos.add(ruw);
			}
		}
		
		return weibos;
	}
	
	private static List<Status> getUserStatus(String uid, String accessToken, long maxId) {
		try {
			Paging paging = new Paging();
			paging.setCount(200);
			if (maxId > 0) paging.setMaxId(maxId);
			System.out.println("fetch usertimeline uid=" + uid + ", sinceId=" + maxId);
			return new Timeline().getUserTimelineByUid(uid, paging, 0, 0).getStatuses();
		} catch (WeiboException e) {
			System.err.println("Failed to get the Sina Weibo Status. Reason: " + e.getMessage());
			return new ArrayList<Status>();
		}
	}
}