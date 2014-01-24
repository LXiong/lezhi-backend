package com.buzzinate.jianghu.sr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.RelatedArticleDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.RelatedArticle;
import com.buzzinate.common.model.RelatedArticle.RelatedItem;

/**
 * BShareRecommender的简化版，主要是避免了重复访问mongodb的代码，比如，先把ID取出来，然后再去取内容，oh
 * 
 * @author Brad Luo
 *
 */
public class FastRecommender {
	private ArticleDao articleDao;
	private RelatedArticleDao raDao;
	
	public FastRecommender(ArticleDao articleDao, RelatedArticleDao raDao) {
		this.articleDao = articleDao;
		this.raDao = raDao;
	}

	/**
	 * 推荐相关文章
	 */
	public List<RelatedItem> recommendRelated(long id, int max) {
		List<RelatedItem> items = new ArrayList<RelatedItem>();
		RelatedArticle ra = raDao.get(id);
		if (ra == null) return items;
		
		List<Long> ids = new ArrayList<Long>();
		for (RelatedItem item: ra.getItems()) {
			ids.add(item.articleId);
		}
		ids.add(id);
		List<Article> as = articleDao.get(ids, "title");
		HashMap<Long, String> id2title = new HashMap<Long, String>();
		for (Article a: as) {
			if (a != null) id2title.put(a.getId(), a.getTitle());
		}
		
		HashSet<String> nrs = extractNR(id2title.get(id));
		for (RelatedItem item: ra.getItems()) {
			if (item.score > 0.2) items.add(item);
			else {
				HashSet<String> rnrs = extractNR(id2title.get(item.articleId));
				rnrs.retainAll(nrs);
				if (rnrs.size() > 0) items.add(item);
			}
		}
		return items;
	}

	// TODO: Drop this class
	private HashSet<String> extractNR(String title) {
		HashSet<String> nrs = new HashSet<String>();
		if (title == null) return nrs;
		
		return nrs;
	}
}