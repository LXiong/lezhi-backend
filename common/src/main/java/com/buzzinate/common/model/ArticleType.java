package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

@Entity(value="articleType", noClassnameStored=true)
public class ArticleType {
	@Id private Integer id;
	
	@Indexed(background=true)
	private String title;
	@Indexed(background=true)
	private String type;
	
	@Indexed(background=true, value=IndexDirection.DESC)
	private Long createAt;
	
	public ArticleType() {
		super();
	}

	public ArticleType(Integer id, String title, String type, Long createAt) {
		super();
		this.id = id;
		this.title = title;
		this.type = type;
		this.createAt = createAt;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Long posttime) {
		this.createAt = posttime;
	}
	
	
	
}
