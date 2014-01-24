package com.buzzinate.jianghu.security;

import java.util.ArrayList;
import java.util.List;

import weibo4j.Paging;
import weibo4j.Status;
import weibo4j.Weibo;
import weibo4j.WeiboException;

import com.buzzinate.common.model.User;
import com.buzzinate.common.model.UserWeibo;

public class WeiboExtractor {
	
	public static List<UserWeibo> extractUserWeibos(Weibo weibo, User user) {
		List<UserWeibo> uws = new ArrayList<UserWeibo>();
		List<weibo4j.Status> statuses = getUserStatus(weibo, String.valueOf(user.getUid()), -1);
		for (weibo4j.Status status: statuses) {
			UserWeibo uw = new UserWeibo(user.getId(), status.getId(), status.getText(), status.getCreatedAt().getTime());
			uws.add(uw);
			Status rt = status.getRetweeted_status();
			if (rt != null && rt.getCreatedAt() != null) {
				UserWeibo ruw = new UserWeibo(user.getId(), rt.getId(), rt.getText(), rt.getCreatedAt().getTime());
				uws.add(ruw);
			}
		}
		
		return uws;
	}
	
	private static List<Status> getUserStatus(Weibo weibo, String uid, long maxId) {
		try {
			Paging paging = new Paging();
			paging.setCount(200);
			if (maxId > 0) paging.setMaxId(maxId);
			System.out.println("fetch usertimeline uid=" + uid + ", sinceId=" + maxId);
			return weibo.getUserTimeline(uid, paging);
		} catch (WeiboException e) {
			System.err.println("Failed to get the Sina Weibo Status. Reason: " + e.getMessage());
			return new ArrayList<Status>();
		}
	}
}