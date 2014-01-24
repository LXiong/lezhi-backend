package com.buzzinate.batch;

import java.util.List;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.UserProfileDao;
import com.buzzinate.common.dao.mongo.UserDaoImpl;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.diffusion.DiffusionProfile;
import com.buzzinate.diffusion.UserDiffusionProfileBuilder;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RebuildUserDiffusionProfile {

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		UserDao userDao = new UserDaoImpl(ds);
		PreferenceDao prefDao = new PreferenceDao(ds);
		UserProfileDao upDao = new UserProfileDao(ds);
		Dictionary dict = injector.getInstance(Dictionary.class);
		
		List<Long> leziUserIds = userDao.findLeziUserIds();
//		List<Long> leziUserIds = Arrays.asList(11604L, 12377L, 49449L, 89342L, 56719L);
//		List<Long> leziUserIds = Arrays.asList(12071L, 77926L, 188835L);
		for (long userId: leziUserIds) {
			DiffusionProfile dp = UserDiffusionProfileBuilder.build(articleDao, prefDao, userDao, dict, userId);
			if (dp != null) upDao.update(userId, dp.getUserFeatures(), dp.getKeywordFeatures());
		}
	}
}