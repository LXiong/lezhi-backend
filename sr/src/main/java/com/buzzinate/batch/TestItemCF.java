package com.buzzinate.batch;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ItemMinhashDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.main.MyModule;
import com.buzzinate.recomm.MHItemCFRecommender;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestItemCF {
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: TestDiffusion <user id> <beta>");
			System.exit(1);
		}
		long userId = Long.parseLong(args[0]);
		double beta = Double.parseDouble(args[1]);
		
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		ItemMinhashDao imhDao = new ItemMinhashDao(ds);
		PreferenceDao prefDao = new PreferenceDao(ds);
		
		MHItemCFRecommender recomm = new MHItemCFRecommender(prefDao, imhDao, articleDao);
		recomm.recommend(userId, 200, beta);
	}
}