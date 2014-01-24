package com.buzzinate.jianghu.model;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

/**
 * 用户相关的一些状态信息，比如最近看的最后一条statusid，等
 * 
 * @author brad
 *
 */
@Entity(value="userStatus", noClassnameStored=true)
public class UserStatus {
	
	// user id
	@Id private long id;
	
	private long lastViewedMentionId;
	private long lastViewedStatusId;
	private long lastRecommendArticleTime;
	private List<Long> lastStatusIds = new ArrayList<Long>();
	@Indexed
	private long lastStatusTime = 0;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getLastViewedMentionId() {
		return lastViewedMentionId;
	}

	public void setLastViewedMentionId(long lastViewedMentionId) {
		this.lastViewedMentionId = lastViewedMentionId;
	}

	public long getLastViewedStatusId() {
		return lastViewedStatusId;
	}

	public void setLastViewedStatusId(long lastViewedStatusId) {
		this.lastViewedStatusId = lastViewedStatusId;
	}

	public long getLastRecommendArticleTime() {
		return lastRecommendArticleTime;
	}

	public void setLastRecommendArticleTime(long lastRecommendArticleTime) {
		this.lastRecommendArticleTime = lastRecommendArticleTime;
	}

	public List<Long> getLastStatusIds() {
		return lastStatusIds;
	}

	public void setLastStatusIds(List<Long> lastStatusIds) {
		this.lastStatusIds = lastStatusIds;
	}

	public long getLastStatusTime() {
		return lastStatusTime;
	}

	public void setLastStatusTime(long lastStatusTime) {
		this.lastStatusTime = lastStatusTime;
	}

	public void addStatusIds(long statusId, long statusTime) {
		lastStatusIds.add(0, statusId);
		if (lastStatusIds.size() > 5) lastStatusIds = lastStatusIds.subList(0, 5);
		this.lastStatusTime = statusTime;
	}
}
