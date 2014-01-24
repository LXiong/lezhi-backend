package com.buzzinate.batch;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.mongo.UserDaoImpl;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.diffusion.Diffusions;
import com.buzzinate.common.diffusion.MyDiffusion;
import com.buzzinate.diffusion.DiffusionProfile;
import com.buzzinate.diffusion.UserDiffusionProfileBuilder;
import com.buzzinate.main.MyModule;
import com.buzzinate.recomm.DiffusionRecommender;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestDiffusion {
	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("Usage: TestDiffusion <user id> <alpha> <beta> <FD>");
			System.exit(1);
		}
		long userId = Long.parseLong(args[0]);
		double alpha = Double.parseDouble(args[1]);
		double beta = Double.parseDouble(args[2]);
		UserDiffusionProfileBuilder.FD = Integer.parseInt(args[3]);
		
		Diffusions.algos[0].diffusion = new MyDiffusion(alpha, beta);
		Diffusions.algos[1].diffusion = new MyDiffusion(beta, alpha);
		
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		UserDao userDao = new UserDaoImpl(ds);
		PreferenceDao prefDao = new PreferenceDao(ds);
		Dictionary dict = injector.getInstance(Dictionary.class);
		
		DiffusionProfile dp = UserDiffusionProfileBuilder.build(articleDao, prefDao, userDao, dict, userId);
		
		DiffusionRecommender recomm = new DiffusionRecommender(articleDao, prefDao, dict);
		recomm.recommend(userId, dp, 200);
	}
}