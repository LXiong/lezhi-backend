package com.buzzinate.batch;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.UserMinhashDao;
import com.buzzinate.main.MyModule;
import com.buzzinate.recomm.MHUserCFRecommender;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestUserCF {
	public static void main(String[] args) {
//		System.out.println(Math.log(2));
//		System.out.println(Math.log(3));
//		System.out.println(Math.log(4));
//		if (true) return;
		if (args.length < 3) {
			System.out.println("Usage: TestDiffusion <user id> <beta> <alpha>");
			System.exit(1);
		}
		long userId = Long.parseLong(args[0]);
		double beta = Double.parseDouble(args[1]);
		double alpha = Double.parseDouble(args[2]);
		
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		UserDao userDao = injector.getInstance(UserDao.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		UserMinhashDao umhDao = new UserMinhashDao(ds);
		PreferenceDao prefDao = new PreferenceDao(ds);
		
		MHUserCFRecommender recomm = new MHUserCFRecommender(prefDao, umhDao, userDao, articleDao);
		recomm.recommend(userId, 100, beta, alpha);
	}
}