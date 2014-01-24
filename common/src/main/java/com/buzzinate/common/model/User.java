package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

/**
 * 新浪里面的用户信息
 * 
 * @author Brad Luo
 *
 */
@Entity(value="user", noClassnameStored=true)
public class User {
	@Id
	private long id;
	private String name;
	@Indexed
	private String screenName;
	private String profileImageUrl;
	private String detail;
	
	@Indexed
	private String accessToken;
	private String secret;
	
	@Indexed(unique=true, dropDups=true)
	private long uid;
	
	@Indexed
	private String cookieId;
	
	// 粉丝数
	@Indexed
	private long followersSize;
	// 发表微博数
	@Indexed
	private long tweetsSize;

	@Indexed
	private boolean isLeziUser;
	
	private int prefSize;
	
	public int getPrefSize() {
		return prefSize;
	}

	public void setPrefSize(int prefSize) {
		this.prefSize = prefSize;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getScreenName() {
		return screenName;
	}
	
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	
	public String getProfileImageUrl() {
		return profileImageUrl;
	}
	
	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public boolean isLeziUser() {
		return isLeziUser;
	}

	public void setLeziUser(boolean isLeziUser) {
		this.isLeziUser = isLeziUser;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public String getCookieId() {
		return cookieId;
	}

	public void setCookieId(String cookieId) {
		this.cookieId = cookieId;
	}

	public long getFollowersSize() {
		return followersSize;
	}

	public void setFollowersSize(long followersSize) {
		this.followersSize = followersSize;
	}

	public long getTweetsSize() {
		return tweetsSize;
	}

	public void setTweetsSize(long tweetsSize) {
		this.tweetsSize = tweetsSize;
	}
}
