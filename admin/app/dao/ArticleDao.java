package dao;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.modules.morphia.MorphiaPlugin;

import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.util.DateTimeUtil;
import com.buzzinate.common.util.IdGenerator;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;

public class ArticleDao {
	
	public static void createArticle(Article article) {
		Datastore ds = MorphiaPlugin.ds();
		long id = IdGenerator.generateLongId(ds, Article.class);
		article.setId(id);
		ds.save(article);
	}
	
	public static Article getArticle(long id) {
		Datastore ds = MorphiaPlugin.ds();
		return ds.createQuery(Article.class).filter("_id", id).get();
	}

	public static List<Article> getArticleByUrl(String url, int begin, int max) {
		Datastore ds = MorphiaPlugin.ds();
		if (max <= 0) {
			max = 50;
		}
		if (begin <= 1) {
			begin = 1;
		}
		Query query = ds.createQuery(Article.class).field("url").equal(url);
		query.offset((begin - 1) * max);
		query.limit(max);
		return query.asList();
	}
	
	public static List<Article> getTrainingArticle() {
		Datastore ds = MorphiaPlugin.ds();
		Query query = ds.createQuery(Article.class)
			.field("isTraining").equal(true)
			.field("category").notEqual(Category.NONE)
			.field("status").equal(Status.OK);
		return query.asList();
	}

	public static List<Article> getArticles(Category category, Date dateStart,
			Date dateEnd, String from, String prefSize, Status status, int training,
			int begin, int max) {
		Datastore ds = MorphiaPlugin.ds();
		if (max <= 0) {
			max = 50;
		}
		if (begin <= 1) {
			begin = 1;
		}
		Query query = ds.createQuery(Article.class);
		query.offset((begin - 1) * max);
		query.limit(max);
		if (category != null) {
			query.field("category").equal(category);
		}
		if (dateStart != null && dateEnd != null) {
			query.field("createAt").greaterThanOrEq(dateStart);
			query.field("createAt").lessThanOrEq(dateEnd);
		} else if (dateStart != null) {
			query.field("createAt").greaterThanOrEq(dateStart);
			query.field("createAt").lessThanOrEq(DateTimeUtil.plusDays(dateStart, 1));
		} else if (dateEnd != null) {
			query.field("createAt").greaterThanOrEq(dateEnd);
			query.field("createAt").lessThanOrEq(DateTimeUtil.plusDays(dateEnd, 1));
		}
		if (!StringUtils.isEmpty(from)) {
			query.field("from").equal(from);
		}
		if (status != null) {
			query.field("status").equal(status);
		}
		if (!StringUtils.isEmpty(prefSize) && prefSize.matches("[+-]?[0-9]+")) {
			if (prefSize.matches("[0-9]+")) {
				query.field("prefSize").greaterThanOrEq(Integer.parseInt(prefSize));
			} else if (prefSize.startsWith("+")) {
				query.field("prefSize").greaterThanOrEq(
						Integer.parseInt(prefSize.substring(1)));
			} else {
				query.field("prefSize").lessThanOrEq(
						Integer.parseInt(prefSize.substring(1)));
			}
		}
		if (training == 1) {
			query.field("isTraining").equal(true);
		} else if (training == 0) {
			query.field("isTraining").notEqual(true);
		}
		return query.order("-score").asList();
	}

	/**
	 * return whether updated successful.
	 * 
	 * @param articleId
	 * @param title
	 * @param content
	 * @param category
	 * @return
	 */
	public static boolean update(Article article2) {
		Datastore ds = MorphiaPlugin.ds();
		Article article = ds.find(Article.class, "_id =", article2.getId()).get();
		String title = article2.getTitle();
		String content = article2.getContent();
		Category category = article2.getCategory();
		Double score = article2.getScore();
		boolean isTraining = article2.isTraining();
		boolean needUpdate = false;
		if (article == null) {
			return false;
		}
		if (!StringUtils.isEmpty(title) && !title.equals(article.getTitle())) {
			needUpdate = true;
			article.setTitle(title);
		}
		if (!StringUtils.isEmpty(content) && !content.equals(article.getContent())) {
			needUpdate = true;
			article.setContent(content);
		}
		if (category != null && category != article.getCategory()) {
			needUpdate = true;
			article.setCategory(category);
		}
		if (isTraining != article.isTraining()) {
			needUpdate = true;
			article.setTraining(isTraining);
		}
		if (score != null && score.intValue() > 0 && !score.equals(article.getScore())) {
			needUpdate = true;
			article.setScore(score);
		}
		if (needUpdate) {
			ds.save(article);
			return true;
		}
		return false;
	}
	
	public static boolean markspamArticle(long articleId) {
		Datastore ds = MorphiaPlugin.ds();
		Article article = ds.find(Article.class, "_id =", articleId).get();
		if (article != null) {
			try {
				article.setStatus(Status.MARK_SPAM);
				ds.save(article);
				return true;
			} catch (Exception e) {
				// do nothing now
			}
		}
		return false;
	}

	public static List<Article> getArticlesByMinHash(int minhash) {
		Datastore ds = MorphiaPlugin.ds();
		return ds.find(Article.class, "minHash =", minhash).asList();
	}
	
	public static List<Article> getTopArticlesByCategory(Category category, int begin, int max) {
		if (max <= 0) {
			max = 50;
		}
		if (begin <= 1) {
			begin = 1;
		}
		Datastore ds = MorphiaPlugin.ds();
		Query<Article> q = ds.createQuery(Article.class)
				.filter("status", Status.OK).filter("category", category)
				.order("-score").offset((begin-1)*max).limit(max);
		return q.asList();
	}
}
