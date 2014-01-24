package com.buzzinate.jianghu.api.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.buzzinate.common.model.Article;
import com.buzzinate.jianghu.sr.TopItem;

public class ArticleVO {
	public long id;
	public String title;
	public String summary;
	public String thumbnail;
	public String fromIcon;
	public String from;
	public String url;
	public boolean read;
	public boolean like;
	public int likeCount;
	public int commentCount;
	public int category;
	public String feedback;
	public long createAt;
	
	public ArticleVO(Article article, int likeCount, int commentCount, boolean read, boolean like) {
		this(article, likeCount, commentCount, read, like, null);
	}
	
	public ArticleVO(Article article, int likeCount, int commentCount, boolean read, boolean like, String feedback) {
		this.id = article.getId();
		this.title = article.getTitle();
		this.summary = article.getSummary();
		this.thumbnail = article.getThumbnail();
		this.fromIcon = article.getFromIcon();
		this.from = article.getFrom();
		this.url = article.getUrl();
		this.read = read;
		this.like = like;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
		this.category = article.getCategory().getCode();
		this.feedback = feedback;
		this.createAt = article.getCreateAt();
	}
	
	/**
	 * 针对非登陆用户
	 * @param articles
	 * @return
	 */
	public static ArticleVO[] make(List<Article> articles){
		ArticleVO[] results = new ArticleVO[articles.size()];
		for (int i = 0; i < articles.size(); i++) {
			Article article = articles.get(i);
			results[i] = new ArticleVO(article, 0, 0, false, false);
		}
		return results;
	}
	
	public static ArticleVO[] make(List<Article> articles, List<Long> reads, List<Long> likes) {
		ArticleVO[] results = new ArticleVO[articles.size()];
		for (int i = 0; i < articles.size(); i++) {
			Article article = articles.get(i);
			boolean read = reads.contains(article.getId());
			boolean like = likes.contains(article.getId());
			results[i] = new ArticleVO(article, 0, 0, read, like);
		}
		return results;
	}
	
	public static ArticleVO[] make(List<Article> articles, List<Long> reads, List<Long> likes, int count, Long cutTime) {
		List<ArticleVO> results = new ArrayList<ArticleVO>();
		
		for (int i = 0; i < articles.size(); i++) {
			Article article = articles.get(i);
			Long createAt = article.getCreateAt();
			if(createAt < cutTime){
				boolean read = reads.contains(article.getId());
				boolean like = likes.contains(article.getId());
			    results.add(new ArticleVO(article, 0, 0, read, like));
			    if(results.size() >= count) break;
			}
		}
		ArticleVO[] articleVOs = new ArticleVO[results.size()];
		results.toArray(articleVOs);
		return articleVOs;
	}
	
	public static ArticleVO[] make(List<TopItem> items, List<Article> articles) {
		HashMap<Long, Article> id2articles = new HashMap<Long, Article>();
		for (Article a: articles) id2articles.put(a.getId(), a);
		ArticleVO[] results = new ArticleVO[articles.size()];
		int idx = 0;
		for (TopItem item: items) {
			Article article = id2articles.get(item.getItemId());
			if (article != null) {
				results[idx] = new ArticleVO(article, 0, 0, false, false, formatFeedback(item));
				idx += 1;
			}
			if (idx >= results.length) break;
		}
		
		return results;
	}
	
	public static ArticleVO[] make(List<TopItem> items, List<Article> articles, List<Long> reads, List<Long> likes) {
		HashMap<Long, Article> id2articles = new HashMap<Long, Article>();
		for (Article a: articles) id2articles.put(a.getId(), a);
		ArticleVO[] results = new ArticleVO[articles.size()];
		int idx = 0;
		for (TopItem item: items) {
			Article article = id2articles.get(item.getItemId());
			if (article != null) {
				boolean read = reads.contains(article.getId());
				boolean like = likes.contains(article.getId());
				results[idx] = new ArticleVO(article, 0, 0, read, like, formatFeedback(item));
				idx += 1;
			}
			if (idx >= results.length) break;
		}
		
		return results;
	}
	
	public static ExtView<ArticleVO>[] makeDetail(List<TopItem> items, List<Article> articles, List<Long> reads, List<Long> likes) {
		HashMap<Long, TopItem> id2items = new HashMap<Long, TopItem>();
		for (TopItem item: items) id2items.put(item.getItemId(), item);
		ExtView<ArticleVO>[] results = new ExtView[articles.size()];
		for (int i = 0; i < articles.size(); i++) {
			Article article = articles.get(i);
			long id = article.getId();
			boolean read = reads.contains(article.getId());
			boolean like = likes.contains(article.getId());
			TopItem item = id2items.get(id);
			results[i] =  ExtView.combine(new ArticleVO(article, 0, 0, read, like, formatFeedback(id2items.get(article.getId()))));
		}
		return results;
	}
	
	private static String formatFeedback(TopItem item) {
		return item.toString();
	}
}
