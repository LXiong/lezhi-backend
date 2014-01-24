package com.buzzinate.common.dao.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.Page;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.model.Status;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.mapping.Mapper;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class ArticleDaoImpl extends BaseDaoDefault<Article, Long> implements ArticleDao {

	public ArticleDaoImpl(Datastore ds) {
		super(ds);
	}

	@Override
	public List<Article> findPop(int count, int page) {
		Query<Article> q = createQuery().filter("status", Status.OK).order("-score").offset(count * (page-1)).limit(count);
		return q.asList();
	}

	@Override
	public List<Article> findPop(Category category, int count, int page) {
		Query<Article> q = createQuery().filter("status", Status.OK).filter("category", category).order("-score").offset(count * (page-1)).limit(count);
		return q.asList();
	}
	
	@Override
	public List<Article> top(int offset, int length) {
		Query<Article> q = createQuery().filter("status", Status.OK).order("-score").offset(offset).limit(length);
		return q.asList();
	}
	
	@Override
	public List<Article> top(Category category, int offset, int length) {
		Query<Article> q = createQuery().filter("status", Status.OK).filter("category", category).order("-score").offset(offset).limit(length);
		return q.asList();
	}

	@Override
	public List<Article> get(List<Long> articleIds, Page page) {
		if (articleIds.isEmpty()) return new ArrayList<Article>();
		
		Query<Article> q = createQuery().filter("id in", articleIds).filter("status", Status.OK);
		filterByPage(q, page);
		return q.asList();
	}

	@Override
	public Article findByHash(byte[] hash) {
		return createPrimaryQuery().filter("hash", hash).get();
	}

	@Override
	public void incPrefSize(long pageId) {
		// Use primary only to ensure no sync detail
		Article article = getPrimary(pageId);
		article.setPrefSize(article.getPrefSize() + 1);
		article.updateScore();
		
		save(article);
	}

	@Override
	public List<Long> filterByStatus(Collection<Long> ids) {
		if (ids.isEmpty()) return new ArrayList<Long>();
		return getIds(createQuery().filter("id in", ids).filter("status", Status.OK));
	}

	@Override
	public void updateCategory(long id, Category cat) {
		Query<Article> q = createQuery().filter("id", id);
		UpdateOperations<Article> uo = createUpdateOperations().set("category", cat);
		update(q, uo);
	}

	@Override
	public List<Article> findLatest(long sinceId, String... fields) {
		return createQuery().retrievedFields(true, fields).filter(Mapper.ID_KEY + " >", sinceId).filter("status", Status.OK).asList();
	}
}