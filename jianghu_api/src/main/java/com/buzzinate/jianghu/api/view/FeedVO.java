package com.buzzinate.jianghu.api.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.buzzinate.common.model.User;
import com.buzzinate.jianghu.model.Status;

public class FeedVO {
	public long createdAt;
	public UserVO user;
	public long id;
	public int feature;
	public FeedContentVO[] content;
	
	public FeedVO(User user, boolean following, int feature, Status[] statuses) {
		this.createdAt = 0L;
		this.user = new UserVO(user, following);
		this.feature = statuses[0].getFeature();
		content = new FeedContentVO[statuses.length];
		for (int i = 0; i < content.length; i++) {
			content[i] = new FeedContentVO(statuses[i]);
			if (createdAt < statuses[i].getCreateAt()) createdAt = statuses[i].getCreateAt();
		}
	}
	
	public static FeedVO[] make(List<User> users, List<Status> statuses, List<Long> friendIds) {
		HashMap<String, List<Status>> groups = new HashMap<String, List<Status>>();
		for (Status s: statuses) {
			String key = s.getUserId() + "#" + s.getFeature();
			List<Status> list = groups.get(key);
			if (list == null) {
				list = new ArrayList<Status>();
				groups.put(key, list);
			}
			list.add(s);
		}
		
		HashMap<Long, User> userMap = new HashMap<Long, User>();
		for (User u: users) {
			userMap.put(u.getId(), u);
		}
		
		List<FeedVO> feeds = new ArrayList<FeedVO>();
		for (List<Status> group: groups.values()) {
			long uid = group.get(0).getUserId();
			boolean following = friendIds.contains(uid);
			Collections.sort(group, new Comparator<Status>(){
				@Override
				public int compare(Status s1, Status s2) {
					if (s1.getCreateAt() < s2.getCreateAt()) return 1;
					else if (s1.getCreateAt() == s2.getCreateAt()) return 0;
					else return -1;
				}
				
			});
			FeedVO feed = new FeedVO(userMap.get(uid), following, group.get(0).getFeature(), group.toArray(new Status[0]));
			feeds.add(feed);
		}
		Collections.sort(feeds, new Comparator<FeedVO>() {
			@Override
			public int compare(FeedVO f1, FeedVO f2) {
				if (f1.createdAt < f2.createdAt) return 1;
				else if (f1.createdAt == f2.createdAt) return 0;
				else return -1;
			}
		});
		
		return feeds.toArray(new FeedVO[0]);
	}
}
