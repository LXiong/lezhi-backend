package com.buzzinate.classify;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.mongo.ArticleDaoImpl;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.util.Constants;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class TrainClassify {
	
	private static Logger log = Logger.getLogger(TrainClassify.class);
	
	@Inject(optional = true) @Named(Constants.KEY_MODEL_PATH)
	private static String MODEL_PATH;
	
	public static void main(String[] args) throws ParseException, IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Classifier classifier = injector.getInstance(Classifier.class);
		Datastore datastore = injector.getInstance(Datastore.class);
		ArticleDao articleDao = new ArticleDaoImpl(datastore);
		long fromId1 = 4468;
		long endId1 = 63746;
		
		long fromId2 = 4196197;
		long endId2 = 4196906;
		
		Query<Article> q1 = articleDao.createQuery()
				.field("isTraining").equal(true)
				.field("status").equal(Status.OK)
				.field("_id").greaterThanOrEq(fromId1)
				.field("_id").lessThanOrEq(endId1);
		List<Article> articles1 = articleDao.find(q1).asList();
		
		Query<Article> q2 = articleDao.createQuery()
		.field("isTraining").equal(true)
		.field("status").equal(Status.OK)
		.field("_id").greaterThanOrEq(fromId2)
		.field("_id").lessThanOrEq(endId2);
		List<Article> articles2 = articleDao.find(q2).asList();
		
		articles1.addAll(articles2);
		
		printNumOfArticlesInCategories(articles1);
		System.out.println("start train");
		classifier.train(articles1);
		System.out.println("train done");
		
		String backFile = MODEL_PATH + "/" + System.currentTimeMillis();
		new File(MODEL_PATH + "/current").renameTo(new File(backFile));
		
		classifier.saveClassifier(MODEL_PATH + "/current");
		System.out.println("save done");
		
		for (Category c: Category.values()) {
			List<Article> as = BaseDaoDefault.asList(articleDao.createQuery().filter("status", Status.OK).filter("category", c).order("-score").limit(1000));
			for (Article a: as) {
				Category nc = classifier.classify(a);
				if (nc.getCode() != a.getCategory().getCode()) System.out.println(a.getId() + "/" + a.getTitle() + ": " + a.getCategory() + " ==> " + nc);
			}
		}
	}

	private static void printNumOfArticlesInCategories(List<Article> articles) {
		Map<Integer, Integer> catCount = new HashMap<Integer, Integer>();
		for (Article a : articles) {
			int cat = a.getCategory().getCode();
			Integer count = (Integer) catCount.get(cat);
			if (count == null) {
				catCount.put(cat, 1);
			} else {
				catCount.put(cat, count + 1);
			}
		}
		
		System.out.println(Category.TECHNOLOGY.name() + " : " + catCount.get(Category.TECHNOLOGY.getCode()));
		System.out.println(Category.NEWS.name() + " : " + catCount.get(Category.NEWS.getCode()));
		System.out.println(Category.CULTURE.name() + " : " + catCount.get(Category.CULTURE.getCode()));
		System.out.println(Category.LIFE.name() + " : " + catCount.get(Category.LIFE.getCode()));
		System.out.println(Category.SPORTS.name() + " : " + catCount.get(Category.SPORTS.getCode()));
		System.out.println(Category.FINANCE.name() + " : " + catCount.get(Category.FINANCE.getCode()));
		System.out.println(Category.ENTERTAINMENT.name() + " : " + catCount.get(Category.ENTERTAINMENT.getCode()));
		System.out.println(Category.WOMEN.name() + " : " + catCount.get(Category.WOMEN.getCode()));
		
		log.info(Category.TECHNOLOGY.name() + " : " + catCount.get(Category.TECHNOLOGY.getCode()));
		log.info(Category.NEWS.name() + " : " + catCount.get(Category.NEWS.getCode()));
		log.info(Category.CULTURE.name() + " : " + catCount.get(Category.CULTURE.getCode()));
		log.info(Category.LIFE.name() + " : " + catCount.get(Category.LIFE.getCode()));
		log.info(Category.SPORTS.name() + " : " + catCount.get(Category.SPORTS.getCode()));
		log.info(Category.FINANCE.name() + " : " + catCount.get(Category.FINANCE.getCode()));
		log.info(Category.ENTERTAINMENT.name() + " : " + catCount.get(Category.ENTERTAINMENT.getCode()));
		log.info(Category.WOMEN.name() + " : " + catCount.get(Category.WOMEN.getCode()));
	}

}
