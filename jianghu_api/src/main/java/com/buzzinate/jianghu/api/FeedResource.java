package com.buzzinate.jianghu.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;

import com.buzzinate.common.dao.Page;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.User;
import com.buzzinate.jianghu.api.view.FeedVO;
import com.buzzinate.jianghu.api.view.StatusVO;
import com.buzzinate.jianghu.dao.FollowDao;
import com.buzzinate.jianghu.dao.StatusDao;
import com.buzzinate.jianghu.dao.UserStatusDao;
import com.buzzinate.jianghu.model.Status;
import com.buzzinate.jianghu.model.UserStatus;
import com.google.inject.Inject;

@Path("feed")
@Produces({"application/json"})
public class FeedResource {
	private static Logger log = Logger.getLogger(FeedResource.class);
	
	@Inject private UserDao userDao;
	@Inject private StatusDao statusDao;
	@Inject private FollowDao followDao;
	@Inject private UserStatusDao userStatusDao;
	
	@GET @Path("timeline")
	public FeedVO[] getFeed(@HeaderParam("userId") long loginUserId,
			@QueryParam("count") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page) {
		List<Long> userIds = followDao.findFriendIds(loginUserId);
		userIds.add(loginUserId);
		List<User> users = userDao.get(userIds);
		
		List<UserStatus> userStatuses = userStatusDao.findStatus(userIds, count, page);
		List<Long> statusIds = new ArrayList<Long>();
		for (UserStatus us: userStatuses) {
			statusIds.addAll(us.getLastStatusIds());
		}
		
		List<Status> statuses = statusDao.get(statusIds);
		
		UserStatus userStatus = userStatusDao.getOrNew(loginUserId);
		for (Status s: statuses) {
			if (userStatus.getLastViewedStatusId() < s.getId()) userStatus.setLastViewedStatusId(s.getId());
		}
		userStatusDao.save(userStatus);
		
		return FeedVO.make(users, statuses, userIds);
	}
	
	@GET @Path("user")
	public StatusVO[] getFeedByUser(@HeaderParam("userId") long loginUserId,
			@QueryParam("id") @DefaultValue("-1") long id,
			@QueryParam("sinceId") @DefaultValue("-1") long sinceId,
			@QueryParam("maxId") @DefaultValue("-1") long maxId,
			@QueryParam("count") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page) {
		if (id == -1) id = loginUserId;
		log.info("fetch feed for user: " + id);
		List<Status> statuses = statusDao.findByUserId(id, new Page(sinceId, maxId, count, page));
		return StatusVO.make(statuses).toArray(new StatusVO[0]);
	}
}
