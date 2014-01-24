package controllers;

import static utils.Constants.JSON_CAUSE;
import static utils.Constants.JSON_RESULT;
import static utils.Constants.ROLE_ADMIN;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import play.Play;
import play.data.validation.URL;
import play.modules.morphia.MorphiaPlugin;
import play.mvc.Controller;
import play.mvc.With;
import utils.Constants;
import utils.Module;
import utils.SimHash;

import com.buzzinate.common.dao.mongo.ArticleDaoImpl;
import com.buzzinate.common.message.ModelUpdated;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.queue.Queue;
import com.buzzinate.common.sr.Classifier;
import com.buzzinate.common.util.JsonResults;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import dao.ArticleDao;
import flexjson.JSONSerializer;

@Check(ROLE_ADMIN)
@With(Secure.class)
public class ArticleManage extends Controller {
	private static Logger log = Logger.getLogger(ArticleManage.class);
	
	public static void index() {
		List categories = Arrays.asList(Category.values());
		List statuses = Arrays.asList(Status.values());
		render(categories, statuses);
	}
	
	public static void getArticleByUrl(@URL String url, int begin, int max) {
		List<Article> articles = ArticleDao.getArticleByUrl(url, begin, max);
		renderJSON(articles);
	}

	public static void getArticles(Category category, Date dateStart, Date dateEnd,
			String from, String prefSize, Status status, int training, int begin, int max) {
		List<Article> articles = ArticleDao.getArticles(category, dateStart,
				dateEnd, from, prefSize, status, training, begin, max);
		renderJSON(articles);
	}
	
	public static void getDuplicateArticles(long articleId) {
		Article article = ArticleDao.getArticle(articleId);
		Datastore ds = MorphiaPlugin.ds();
		List<Article> articles = SimHash.findDuplicateContent(new ArticleDaoImpl(ds), article);
		renderJSON(articles);
	}
	
	public static void trainClassifierTest() throws IOException {
		JsonResults res = new JsonResults();
		List<Article> articles = ArticleDao.getTrainingArticle();
		int len = articles.size();
		int trainSetLen = len * 4 / 5;
		
		Classifier classifier = new Classifier();
		if (classifier.train(articles.subList(0, trainSetLen))) {
			String modelPath = Play.configuration.getProperty("rec.modelPath");
			String backFile = modelPath + "/" + System.currentTimeMillis();
			new File(modelPath + "/current").renameTo(new File(backFile));
			classifier.saveClassifier(modelPath + "/current");
		} else {
			res.set(false);
			renderJSON(res);
			return;
		}
		
		int correct = 0, total = 0, none = 0;
		List<Article> falseArticles = new ArrayList<Article>();
		for (Article a : articles.subList(trainSetLen, len)) {
			Category target = classifier.classify(a);
			if (target == Category.NONE) {
				none++;
			} else {
				total++;
				if (target.getCode() == a.getCategory().getCode()) {
					correct ++;
				} else {
					falseArticles.add(a);
				}
			}
		}
		
		res.addContent("articles", falseArticles);
		res.addContent("total", total);
		res.addContent("correct", correct);
		res.addContent("none", none);
		JSONSerializer serializer = new JSONSerializer(); 
		renderJSON(serializer.exclude("*classes").deepSerialize(res));
	}
	
	public static void trainClassifier() throws IOException {
		JsonResults res = new JsonResults();
		Injector injector = Guice.createInjector(new Module());
		Queue modelUpdatedQueue = injector.getInstance(Key.get(Queue.class, Names.named(Constants.MODEL_QUEUE)));

		List<Article> articles = ArticleDao.getTrainingArticle();
		
		Classifier classifier = new Classifier();
		try {
			classifier.train(articles);
		} catch (Exception e) {
			log.error(e);
		}
		String modelPath = Play.configuration.getProperty("rec.modelPath");
		String backFile = modelPath + "/" + System.currentTimeMillis();
		new File(modelPath + "/current").renameTo(new File(backFile));
		classifier.saveClassifier(modelPath + "/current");
		
		modelUpdatedQueue.send(new ModelUpdated(modelPath + "/current"));	
		renderJSON(res);
	}
	
	public static void updateArticle(Article article) {
		Map json = new HashMap();
		try {
			ArticleDao.update(article);
			json.put(JSON_RESULT, true);
			json.put("articleId", article.getId());
		} catch (Exception e) {
			log.error(e);
			json.put(JSON_RESULT, false);
			json.put(JSON_CAUSE, e.getMessage());
		} finally {
			renderJSON(json);
		}
	}
	
	public static void createArticle(Article article) {
		Map json = new HashMap();
		try {
			ArticleDao.createArticle(article);
			json.put(JSON_RESULT, true);
			json.put("articleId", article.getId());
		} catch (Exception e) {
			log.error(e);
			json.put(JSON_RESULT, false);
			json.put(JSON_CAUSE, e.getMessage());
		} finally {
			renderJSON(json);
		}
	}
	
	public static void deleteArticle(long articleId) {
		Map json = new HashMap();
		try {
			// not allowed to delete now...
			json.put(JSON_RESULT, false);
			json.put(JSON_CAUSE, "no matched article");
		} finally {
			renderJSON(json);
		}
	}
	
	public static void markspamArticle(long articleId) {
		Map json = new HashMap();
		try {
			boolean r = ArticleDao.markspamArticle(articleId);
			json.put(JSON_RESULT, r);
		} catch (Exception e) {
			log.error(e);
			json.put(JSON_RESULT, false);
			json.put(JSON_CAUSE, e.getMessage());
		} finally {
			renderJSON(json);
		}
	}
	
	public static void getTopArticles(Category category, int begin, int max) {
		List<Article> articles = ArticleDao.getTopArticlesByCategory(category, begin, max);
		renderJSON(articles);
	}
	
}
