package com.buzzinate.common.model;

import java.util.List;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

@Entity(value="relatedArticle", noClassnameStored=true)
public class RelatedArticle {
	@Id private long articleId;
	
	private List<RelatedItem> items;
	
	@Indexed
	private long createAt;
	
	public RelatedArticle() {
	}
	
	public RelatedArticle(long articleId, List<RelatedItem> items, long createAt) {
		this.articleId = articleId;
		this.items = items;
		this.createAt = createAt;
	}

	public long getArticleId() {
		return articleId;
	}

	public void setArticleId(long articleId) {
		this.articleId = articleId;
	}

	public List<RelatedItem> getItems() {
		return items;
	}

	public void setItems(List<RelatedItem> items) {
		this.items = items;
	}

	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
	
	@Embedded
	public static class RelatedItem {
		public long articleId;
		public String title;
		public double score;
		
		public RelatedItem() {
		}
		
		public RelatedItem(long articleId, String title, double score) {
			this.articleId = articleId;
			this.title = title;
			this.score = score;
		}
	}
}