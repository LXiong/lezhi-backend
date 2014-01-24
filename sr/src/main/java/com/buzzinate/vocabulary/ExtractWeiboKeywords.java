package com.buzzinate.vocabulary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import weibo4j2.model.WeiboException;

import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.UserWeiboDao;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.model.UserWeibo;
import com.buzzinate.common.util.MentionUtil;
import com.buzzinate.keyword.KeywordUtil;
import com.buzzinate.keywords.Keyword;
import com.buzzinate.keywords.MobileKeywordsExtractor;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ExtractWeiboKeywords {
	public static void main(String[] args) throws WeiboException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		UserDao userDao = injector.getInstance(UserDao.class);
		UserWeiboDao uwDao = new UserWeiboDao(ds);
		
		Dictionary dict = injector.getInstance(Dictionary.class);
		
		List<Long> leziUserIds = Arrays.asList(11604L);
//		List<Long> leziUserIds = userDao.findLeziUserIds();
//		List<Long> leziUserIds = Arrays.asList(11604L, 12377L, 49449L, 89342L, 56719L);
//		List<Long> leziUserIds = Arrays.asList(12071L, 77926L, 188835L);
		for (long userId: leziUserIds) {
			System.out.println("Processing user " + userId);
			List<UserWeibo> weibos = uwDao.findLatest(userId, 200);
			List<String> texts =  new ArrayList<String>();
			for (UserWeibo weibo: weibos) {
				String text = MentionUtil.cleanStatusText(weibo.getText());
				texts.add(text);
			}
			
			List<Keyword> kws = MobileKeywordsExtractor.extract(texts, 30);
//			List<Keyword> kws = KeywordUtil.extract(dict, "", texts);
			System.out.println(kws);
		}
	}
}