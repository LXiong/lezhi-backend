package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

/**
 * 保存需要过滤的网站列表，采用hostname或者Domain的方式
 * 
 * @author Brad Luo
 *
 */
@Entity(value="blackSite", noClassnameStored=true)
public class BlackSite {
	public enum Status { UnVerified, Verified };

	//微博ID
	@Id private String site;
	private String url;
	private String title;
	
	@Indexed
	private Status status;
	@Indexed
	private int score;
	
	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
}