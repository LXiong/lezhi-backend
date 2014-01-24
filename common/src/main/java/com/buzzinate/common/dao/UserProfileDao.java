package com.buzzinate.common.dao;

import java.util.List;

import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.KeywordFeature;
import com.buzzinate.common.model.UserFeature;
import com.buzzinate.common.model.UserProfile;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class UserProfileDao extends BaseDaoDefault<UserProfile, Long> {

	public UserProfileDao(Datastore ds) {
		super(ds);
	}

	public void update(long userId, List<UserFeature> userFeatures, List<KeywordFeature> keywordFeatures) {
		Query<UserProfile> q = createQuery().filter("userId", userId);
		UpdateOperations<UserProfile> uop = createUpdateOperations().set("userFeatures", userFeatures).set("keywordFeatures", keywordFeatures).set("createAt", System.currentTimeMillis());
		ds.updateFirst(q, uop, true);
	}

	public void update(long userId, List<Keyword> keywords) {
		Query<UserProfile> q = createQuery().filter("userId", userId);
		UpdateOperations<UserProfile> uop = createUpdateOperations().set("keywords", keywords).set("createAt", System.currentTimeMillis());
		ds.updateFirst(q, uop, true);
	}
}