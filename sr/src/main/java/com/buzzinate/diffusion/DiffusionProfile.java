package com.buzzinate.diffusion;

import java.util.List;

import com.buzzinate.common.model.KeywordFeature;
import com.buzzinate.common.model.UserFeature;

public class DiffusionProfile {
	private List<UserFeature> userFeatures;
	private List<KeywordFeature> keywordFeatures;
	
	public DiffusionProfile(List<UserFeature> userFeatures, List<KeywordFeature> keywordFeatures) {
		this.userFeatures = userFeatures;
		this.keywordFeatures = keywordFeatures;
	}

	public List<UserFeature> getUserFeatures() {
		return userFeatures;
	}

	public List<KeywordFeature> getKeywordFeatures() {
		return keywordFeatures;
	}
}