package com.buzzinate.vocabulary;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import weibo4j2.model.Paging;
import weibo4j2.model.Status;
import weibo4j2.model.WeiboException;

import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.UserWeiboDao;
import com.buzzinate.common.model.User;
import com.buzzinate.common.model.UserWeibo;
import com.buzzinate.main.MyModule;
import com.buzzinate.weibo.SinaWeiboClient;
import com.buzzinate.weibo.WeiboExt;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class FetchWeibo {
	public static void main(String[] args) throws WeiboException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		UserDao userDao = injector.getInstance(UserDao.class);
		UserWeiboDao uwDao = new UserWeiboDao(ds);
		
		String accessToken = userDao.findByUid(1720400850L).getAccessToken();
		
		List<Long> leziUserIds = userDao.findLeziUserIds();
//		List<Long> leziUserIds = Arrays.asList(11604L, 12377L, 49449L, 89342L, 56719L, 369875L);
//		List<Long> leziUserIds = Arrays.asList(12071L, 77926L);
//		List<Long> leziUserIds = Arrays.asList(442522L, 163184L, 76763L, 531384L, 526340L);
		
		List<Long> errorIds = new ArrayList<Long>();
		for (long userId: leziUserIds) {
			User user = userDao.get(userId);
			WeiboExt weibo = new SinaWeiboClient().getWeibo(accessToken);
			Paging p = new Paging();
			p.setCount(200);
			try {
				processUser(userDao, uwDao, userId, user, weibo, p);
			} catch (WeiboException e) {
				errorIds.add(userId);
				e.printStackTrace();
			}
		}
		for (long userId: errorIds) {
			User user = userDao.get(userId);
			WeiboExt weibo = new SinaWeiboClient().getWeibo(accessToken);
			Paging p = new Paging();
			p.setCount(200);
			try {
				processUser(userDao, uwDao, userId, user, weibo, p);
			} catch (WeiboException e) {
				e.printStackTrace();
			}
		}
	}

	public static List<Status> processUser(UserDao userDao, UserWeiboDao uwDao, long userId, User user, WeiboExt weibo, Paging p) throws WeiboException {
		List<Status> statuses = weibo.getUserTimeline(String.valueOf(user.getUid()), p);
		for (Status status: statuses) {
			System.out.println(status.getText());
			User u = convert(status.getUser());
			userDao.saveOrUpdate(u);
			UserWeibo uw = new UserWeibo(userId, status.getIdstr(), status.getText(), status.getCreatedAt().getTime());
			uwDao.save(uw);
			Status rt = status.getRetweetedStatus();
			if (rt != null && rt.getCreatedAt() != null) {
				System.out.println(rt.getText());
				UserWeibo ruw = new UserWeibo(userId, rt.getIdstr(), rt.getText(), rt.getCreatedAt().getTime());
				uwDao.save(ruw);
			}
		}
		return statuses;
	}
	
	private static User convert(weibo4j2.model.User weiboUser) {
		User user = new User();
		user.setUid(Long.parseLong(weiboUser.getId()));
		user.setName(weiboUser.getName());
		user.setScreenName(weiboUser.getScreenName());
		URL profileImageURL = weiboUser.getProfileImageURL();
		if (profileImageURL != null) user.setProfileImageUrl(profileImageURL.toExternalForm());
		user.setDetail(weiboUser.toString());
		user.setFollowersSize(weiboUser.getFollowersCount());
		user.setTweetsSize(weiboUser.getStatusesCount());
		user.setPrefSize(1);
		return user;
	}
}