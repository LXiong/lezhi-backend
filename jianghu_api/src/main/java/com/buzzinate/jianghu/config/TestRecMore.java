package com.buzzinate.jianghu.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.RelatedArticleDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.RelatedArticle.RelatedItem;
import com.buzzinate.jianghu.api.ArticleResource;
import com.buzzinate.jianghu.sr.Algorithm;
import com.buzzinate.jianghu.sr.FastRecommender;
import com.buzzinate.jianghu.sr.RecommendService;
import com.buzzinate.jianghu.sr.TopItem;
import com.buzzinate.jianghu.util.ApiException;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestRecMore {
	public static void main(String[] args) throws Exception {
		testRecomm(12071L);
//		testRecomm(77926L);
//		testRecommRelated(3321446L);
	}
	
	private static void testRecommRelated(long id) {
		Injector injector = Guice.createInjector(new JianghuModule());
		Datastore ds = injector.getInstance(Datastore.class);
		
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		RelatedArticleDao raDao = new RelatedArticleDao(ds);
		
		FastRecommender fr = new FastRecommender(articleDao, raDao);
		Article article = articleDao.get(id);
		System.out.println(article.getTitle());
		List<RelatedItem> items = fr.recommendRelated(id, 5);
		for (RelatedItem item: items) {
			System.out.println(item.articleId + " ==> " + item.title);
		}
	}

	public static void testRecomm(long uid) throws ApiException {
		Injector injector = Guice.createInjector(new JianghuModule());
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		RecommendService rs = injector.getInstance(RecommendService.class);
//		ArticleResource ar = injector.getInstance(ArticleResource.class);
		
//		for (int k = 0; k < 10; k++) {
//			long start = System.currentTimeMillis();
//			ar.getRecList(uid, uid, 200, 1);
//			List<TopItem> items = rs.recommend(uid, 200, 0, 200);
//			System.out.println("time: " + (System.currentTimeMillis() - start));
//		}
		
		List<TopItem> items = rs.recommend(uid, 200, 0, 200);
//		for (Algorithm algo: Algorithm.values()) {
//			System.out.println("-------------------------");
//			System.out.println(algo);
//			List<TopItem> items = rs.recommend(algo.name(), uid);
//			if (items.size() > 100) items = items.subList(0, 100);
			List<Long> ids = new ArrayList<Long>();
			HashMap<Long, String> id2title = new HashMap<Long, String>();
			for (TopItem item: items) ids.add(item.getItemId());
			for (Article a: articleDao.get(ids, "title")) {
				if (a != null) id2title.put(a.getId(), a.getTitle());
			}
			for (TopItem item: items) {
				String title = id2title.get(item.getItemId());
				System.out.println(title + " ==> " + item);
			}
			
			System.out.println("-------------------------");
//		}
	}
}