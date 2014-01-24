package com.buzzinate.jianghu.api.view;

import java.util.List;

import com.buzzinate.common.model.Article;

public class ArticleContent {
	
	public long id;
	public String title;
	public String summary;
	public String thumbnail;
	public String url;
	public long createAt;
	
	public ArticleContent(long id, String title, String summary, String thumbnail, String url, long createAt){
		this.id = id;
		this.title = title;
		this.summary = summary;
		this.thumbnail = thumbnail;
		this.url = url;
		this.createAt = createAt;
	}
	
	/**
	 * 针对非登陆用户
	 * @param articles
	 * @return
	 */
	public static ArticleContent[] make(List<Article> articles){
		ArticleContent[] results = new ArticleContent[articles.size()];
		for (int i = 0; i < articles.size(); i++) {
			Article article = articles.get(i);
			results[i] = new ArticleContent(article.getId(), article.getTitle(), article.getSummary(), article.getThumbnail(), article.getUrl(), article.getCreateAt());
		}
		return results;
	}
}
