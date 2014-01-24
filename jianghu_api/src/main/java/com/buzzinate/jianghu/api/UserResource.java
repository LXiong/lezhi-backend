package com.buzzinate.jianghu.api;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.NotFoundException;

import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.User;
import com.buzzinate.jianghu.api.view.ExtView;
import com.buzzinate.jianghu.api.view.UserVO;
import com.buzzinate.jianghu.dao.FollowDao;
import com.buzzinate.jianghu.dao.LikeDao;
import com.buzzinate.jianghu.security.UserService;
import com.google.inject.Inject;

@Path("user")
@Produces({"application/json"})
public class UserResource {
	private static Logger log = Logger.getLogger(UserResource.class);
	
	@Inject private UserDao userDao;
	@Inject private UserService userService;
	@Inject private FollowDao followDao;
	@Inject private LikeDao likeDao;
	
	@GET @Path("profile")
	public ExtView<User> getUserProfile(@HeaderParam("userId") long loginUserId, @QueryParam("id") final Long id) {
		log.info("fetch user profile: " + id);
		User user = userDao.get(id);
		if (user == null) throw new NotFoundException("user not found, id=" + id);
		int friendsCount = followDao.findFriendIds(id).size();
		int followerCount = followDao.countFollower(id);
		boolean following = followDao.findFriendIds(loginUserId).contains(id);
		int likeCount = likeDao.countLikePerUser(id);
		return ExtView.combine(user, "likeCount", likeCount, 
				"followersCount", followerCount, 
				"friendsCount", friendsCount,
				"following", following);
	}
	
	@GET @Path("follower")
	public ExtView<UserVO>[] getUserFollower(@HeaderParam("userId") long loginUserId, @QueryParam("id") Long id) {
		if (id == null) id = loginUserId;
		log.info("fetch user followers: " + id);
		List<Long> followerIds = followDao.findFollowerIds(id);
		List<User> followers = userDao.get(followerIds);
		
		return UserVO.make(followDao, likeDao, followers, loginUserId);
	}
	
	@GET @Path("following")
	public ExtView<UserVO>[] getUserFollowing(@HeaderParam("userId") long loginUserId, @QueryParam("id") Long id) {
		if (id == null) id = loginUserId;
		log.info("fetch user following: " + id);
		List<Long> userIds = followDao.findFriendIds(id);
		List<User> followings = userDao.get(userIds);
		return UserVO.make(followDao, likeDao, followings, loginUserId);
	}
	
	@POST
	@GET
	@Path("login")
	@PermitAll
	public long login(@FormParam("accessToken") String accessToken, @FormParam("secret") String secret) {
		return userService.login(accessToken, secret);
	}
	
	@POST
	@GET
	@Path("login2")
	@PermitAll
	public long login2(@FormParam("accessToken") String accessToken, @FormParam("uid") Long uid) {
		return userService.login2(accessToken, uid);
	}
}