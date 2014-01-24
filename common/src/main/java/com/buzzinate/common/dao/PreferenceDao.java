package com.buzzinate.common.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.types.ObjectId;

import com.buzzinate.common.model.Preference;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.mapping.Mapper;
import com.google.code.morphia.query.Query;

public class PreferenceDao extends BaseDaoDefault<Preference, ObjectId> {

	public PreferenceDao(Datastore ds) {
		super(ds);
	}

	public List<Long> getAllItems() {
		return distinct(createQuery(), "pageId");
	}
	
	public List<Long> getAllUsers() {
		return distinct(createQuery(), "userId");
	}
	
	public List<Long> getItemsFromUser(long userId) {
		List<Long> ids = new ArrayList<Long>();
		List<Preference> prefs = createQuery().filter("userId", userId).asList();
		for (Preference pref: prefs) ids.add(pref.getPageId());
		Collections.sort(ids);
		return ids;
	}
	
	public List<Long> getTopItemsFromUser(long userId, int max) {
		List<Long> ids = new ArrayList<Long>();
		List<Preference> prefs = createQuery().filter("userId", userId).order("-pageId").limit(max).asList();
		for (Preference pref: prefs) ids.add(pref.getPageId());
		Collections.sort(ids);
		return ids;
	}
	
	public List<Long> getItemsFromUser(long userId, long minId) {
		List<Long> ids = new ArrayList<Long>();
		List<Preference> prefs = createQuery().filter("userId", userId).filter("pageId >=", minId).asList();
		for (Preference pref: prefs) ids.add(pref.getPageId());
		Collections.sort(ids);
		return ids;
	}
	
	public Preference findOne(long userID, long itemID) {
		return findOne(createQuery().filter("userId", userID).filter("pageId", itemID));
	}
	
	public List<Long> findPrefIds(long userId, long minId) {
		List<Preference> myPrefs = createQuery().filter("userId",userId).filter("pageId >", minId - 1).asList();
		List<Long> myPrefIds = new ArrayList<Long>();
		for (Preference pref: myPrefs) {
			myPrefIds.add(pref.getPageId());
		}
		return myPrefIds;
	}
	
	public List<Long> findPrefIds(long userId, List<Long> possibleIds) {
		List<Preference> myPrefs = createQuery().filter("userId",userId).filter("pageId in", possibleIds).asList();
		List<Long> myPrefIds = new ArrayList<Long>();
		for (Preference pref: myPrefs) {
			myPrefIds.add(pref.getPageId());
		}
		return myPrefIds;
	}

	public List<Preference> findLatest(ObjectId sinceId) {
		return createQuery().filter(Mapper.ID_KEY + " >", sinceId).asList();
	}

	public List<Long> getItemsFromUserSince(long userId, long since) {
		List<Long> ids = new ArrayList<Long>();
		List<Preference> prefs = createQuery().filter("userId", userId).filter("createAt >", since).asList();
		for (Preference pref: prefs) ids.add(pref.getPageId());
		return ids;
	}

	public int countPrefSize(long userId) {
		Query<Preference> q = createQuery().filter("userId", userId);
		return (int) count(q);
	}

	public List<Long> findLatestItems(long userId, int max) {
		List<Long> ids = new ArrayList<Long>();
		List<Preference> prefs = createQuery().filter("userId", userId).order("-" + Mapper.ID_KEY).limit(max).asList();
		for (Preference pref: prefs) ids.add(pref.getPageId());
		return ids;
	}
}
