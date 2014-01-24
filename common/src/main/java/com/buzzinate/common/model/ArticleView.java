package com.buzzinate.common.model;

public class ArticleView {
	private int id;
	private String url;
	private String title;
	private String content;
	private String category;
	private long createAt;
	
	public ArticleView(){
		
	}
	
	public ArticleView(int id, String url, String title, String content, String category,
			long createAt) {
		super();
		this.id = id;
		this.url = url;
		this.title = title;
		this.content = content;
		this.category = category;
		this.createAt = createAt;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
	
	
	
}
