package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

@Entity(value = "trendArticle", noClassnameStored = true)
public class TrendArticle {

	@Id
	private Long id;

	@Indexed(background = true)
	private long trendId;
	@Indexed(unique=true, dropDups=true)
	private long pageId;
	
	private Long createAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getTrendId() {
		return trendId;
	}

	public void setTrendId(long trendId) {
		this.trendId = trendId;
	}

	public long getPageId() {
		return pageId;
	}

	public void setPageId(long pageId) {
		this.pageId = pageId;
	}

	public Long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Long createAt) {
		this.createAt = createAt;
	}

}
