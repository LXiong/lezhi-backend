package com.buzzinate.model;

import static com.buzzinate.common.util.Constants.DEFAULT_INTERVAL;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * 新浪微博抓取的种子用户
 * 僵尸帐号，LeziUser，是阅读器登录的用户，都是必须抓取的种子用户
 * 僵尸帐号允许添加或者删除关注关系，但是LeziUser不允许改变关注关系
 * 
 * @author Brad Luo
 *
 */
@Entity(value="feedUser", noClassnameStored=true)
public class FeedUser {
	public enum Role { FeedOnly, All, LeziUser};
	
	@Id public long uid;
	public String screenName;
	
	public String accessToken;
	public String secret;
	
	public Role role;
	
	public long lastStatusId; 
	public long lastFetchTime;
	public long interval = DEFAULT_INTERVAL;
}
