package com.buzzinate.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;

@Entity(value="fetchInfo", noClassnameStored=true)
@Indexes(@Index(value="-score, lastFetchTime", unique=false))
public class FetchInfo {
	@Id public String uid;
	
	public String name;
	public int friendsCount;
	public int statusesCount;
	public int score;
	
	public long lastStatusId;
	
	public long lastFetchTime;
	
	public static int scoreUser(weibo4j2.model.User weiboUser) {
		double score = 0;
		score += Math.log(1 + weiboUser.getFriendsCount());
		score += Math.log(1 + weiboUser.getStatusesCount());
		return (int)Math.ceil(score);
	}
	
	public static FetchInfo from(weibo4j2.model.User weiboUser) {
		FetchInfo fs = new FetchInfo();
		fs.uid = weiboUser.getId();
		fs.name = weiboUser.getScreenName();
		fs.friendsCount = weiboUser.getFriendsCount();
		fs.statusesCount = weiboUser.getStatusesCount();
		fs.score = scoreUser(weiboUser);
		return fs;
	}

	@Override
	public String toString() {
		return uid + "(" + name + ", score=" + score + ", lastStatusId=" + lastStatusId + ", lastFetchTime=" + lastFetchTime + ")";
	}
}