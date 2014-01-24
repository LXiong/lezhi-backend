package com.buzzinate.common.model;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * 保存Diffusion中的Top User set
 *
 */
@Entity(value="userProfile", noClassnameStored=true)
public class UserProfile {
	@Id private long userId;
	
	// diffusion part
	private List<UserFeature> userFeatures = new ArrayList<UserFeature>();
	private List<KeywordFeature> keywordFeatures = new ArrayList<KeywordFeature>();
	
	//content part
	private List<Keyword> keywords = new ArrayList<Keyword>();
	
	private long createAt;
	
	public long getUserId() {
		return userId;
	}

	public List<UserFeature> getUserFeatures() {
		return userFeatures;
	}

	public void setUserFeatures(List<UserFeature> userFeatures) {
		this.userFeatures = userFeatures;
	}
	
	public void addUFs(List<UserFeature> features) {
		userFeatures.addAll(features);
	}

	public List<KeywordFeature> getKeywordFeatures() {
		return keywordFeatures;
	}
	
	public void addKFs(List<KeywordFeature> features) {
		keywordFeatures.addAll(features);
	}

	public void setKeywordFeatures(List<KeywordFeature> keywordFeatures) {
		this.keywordFeatures = keywordFeatures;
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
}