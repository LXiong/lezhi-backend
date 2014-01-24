package com.buzzinate.link;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import weibo4j2.model.Status;

import com.buzzinate.common.geo.GeoInfo;
import com.buzzinate.common.message.LinkDetail;
import com.buzzinate.common.message.LinkMessage;
import com.buzzinate.common.model.User;
import com.buzzinate.common.util.GeoUtil;
import com.buzzinate.model.Tweet;
import com.google.common.collect.HashMultimap;

/**
 * 包含链接的原始微博内容及一些统计信息
 * 
 * @author Brad Luo
 * 
 */
public class TweetInfo {
	private static Logger log = Logger.getLogger(TweetInfo.class);

	private static Pattern trendPattern = Pattern.compile("#([^#]+)#");

	// 微博原创的用户
	public User user;

	public long id;
	public String text;
	public List<LinkInfo> links;
	public long tweetTime;

	public long retweetSize;
	public long commentSize;

	public TweetInfo(Status status, List<String> urls) {
		this.user = convert(status.getUser());
		this.id = Long.parseLong(status.getId());
		this.text = status.getText();
		this.tweetTime = status.getCreatedAt().getTime();
		this.retweetSize = status.getRepostsCount();
		this.commentSize = status.getCommentsCount();
		Double longitude = status.getLongitude();
		Double latitude = status.getLatitude();
		this.links = new ArrayList<LinkInfo>();

		for (String url : urls) {
			links.add(new LinkInfo(url));
		}
	}
	protected TweetInfo(User user, long id, String text, long tweetTime, List<String> urls) {
		this.user = user;
		this.id = id;
		this.text = text;
		this.tweetTime = tweetTime;
		links = new ArrayList<LinkInfo>();
		for (String url : urls) {
			links.add(new LinkInfo(url));
		}
	}

	public static Collection<User> toUsers(List<TweetInfo> tweetinfos) {
		HashMap<Long, User> id2user = new HashMap<Long, User>();
		for (TweetInfo ti : tweetinfos)
			id2user.put(ti.user.getUid(), ti.user);
		return id2user.values();
	}

	public static List<LinkMessage> toMessages(List<TweetInfo> tweetinfos, Set<String> popTrends) {
		HashMultimap<String, TweetInfo> sinaurl2tis = HashMultimap.create();
		HashMap<String, String> sinaurl2url = new HashMap<String, String>();
		for (TweetInfo ti : tweetinfos) {
			for (LinkInfo li : ti.links) {
				sinaurl2tis.put(li.sinaUrl, ti);
				sinaurl2url.put(li.sinaUrl, li.realUrl);
			}
		}

		List<LinkMessage> messages = new ArrayList<LinkMessage>();
		for (Map.Entry<String, Collection<TweetInfo>> e : sinaurl2tis.asMap().entrySet()) {
			LinkMessage message = new LinkMessage();
			message.sinaUrl = e.getKey();
			message.realUrl = sinaurl2url.get(message.sinaUrl);

			List<LinkDetail> details = new ArrayList<LinkDetail>();
			for (TweetInfo ti : e.getValue()) {
				Boolean isPopTrendTweet = false;
				String trendName = "";
				if (popTrends != null) {
					if (null != ti.text) {
						Matcher m = trendPattern.matcher(ti.text);
						if (m.find()) {
							trendName = m.group(1);
							if (popTrends.contains(trendName)) {
								isPopTrendTweet = true;
							}
						}
					}
				}
				LinkDetail detail = new LinkDetail(ti.user.getId(), ti.id, ti.tweetTime, ti.retweetSize, ti.commentSize, isPopTrendTweet, trendName);
				details.add(detail);
			}
			message.details = details;

			messages.add(message);
		}
		return messages;
	}

	public static List<LinkMessage> toTrendMessages(List<TweetInfo> tweetinfos, String trendName) {
		HashMultimap<String, TweetInfo> sinaurl2tis = HashMultimap.create();
		HashMap<String, String> sinaurl2url = new HashMap<String, String>();
		for (TweetInfo ti : tweetinfos) {
			for (LinkInfo li : ti.links) {
				sinaurl2tis.put(li.sinaUrl, ti);
				sinaurl2url.put(li.sinaUrl, li.realUrl);
			}
		}

		List<LinkMessage> messages = new ArrayList<LinkMessage>();
		for (Map.Entry<String, Collection<TweetInfo>> e : sinaurl2tis.asMap().entrySet()) {
			LinkMessage message = new LinkMessage();
			message.sinaUrl = e.getKey();

			message.realUrl = sinaurl2url.get(message.sinaUrl);
			List<LinkDetail> details = new ArrayList<LinkDetail>();
			for (TweetInfo ti : e.getValue()) {
				LinkDetail detail = new LinkDetail(ti.user.getId(), ti.id, ti.tweetTime, ti.retweetSize, ti.commentSize, true, trendName);
				details.add(detail);
			}
			message.details = details;

			messages.add(message);
		}
		return messages;
	}

	public Tweet toTweet() {
		Tweet tweet = new Tweet();
		tweet.setId(id);
		tweet.setUid(user.getId());
		tweet.setText(text);
		tweet.setTweetTime(tweetTime);
		tweet.setUrls(LinkInfo.toMap(links));
		tweet.setCommentSize(commentSize);
		tweet.setRetweetSize(retweetSize);
		return tweet;
	}

	public void expandLinks(BlackSiteDetector blackSiteDetector, Map<String, String> urlMaps) {
		List<LinkInfo> newLinks = new ArrayList<LinkInfo>();
		for (LinkInfo link : links) {
			String realUrl = urlMaps.get(link.sinaUrl);
			if (realUrl != null) {
				log.debug("http://t.cn/" + link.sinaUrl + " ===> " + realUrl);

				// 短链接已不存在
				if (realUrl.isEmpty()) {
					continue;
				}

				if (blackSiteDetector.isInBlackList(realUrl)) {
					log.info("Ignore link:" + realUrl);
					continue;
				}

				newLinks.add(new LinkInfo(link.sinaUrl, realUrl));
			}
		}
		this.links = newLinks;
	}

	private User convert(weibo4j2.model.User weiboUser) {
		User user = new User();
		if (weiboUser == null || weiboUser.getId() == null) {
			System.out.println(weiboUser);
		}
		user.setUid(Long.parseLong(weiboUser.getId()));
		user.setName(weiboUser.getName());
		user.setScreenName(weiboUser.getScreenName());
		URL profileImageURL = weiboUser.getProfileImageURL();
		if (profileImageURL != null)
			user.setProfileImageUrl(profileImageURL.toExternalForm());
		user.setDetail(weiboUser.toString());
		user.setFollowersSize(weiboUser.getFollowersCount());
		user.setTweetsSize(weiboUser.getStatusesCount());
		user.setPrefSize(1);
		return user;
	}

	@Override
	public String toString() {
		return "" + user.getName() + "(" + id + ")[text=" + text + ", links=" + links + ", tweetTime=" + tweetTime + ", retweetSize=" + retweetSize + ", commentSize=" + commentSize + "]";
	}
}
