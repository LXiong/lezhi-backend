package com.buzzinate.jianghu.api;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.NotFoundException;

import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.User;
import com.buzzinate.jianghu.api.view.UserVO;
import com.buzzinate.jianghu.dao.FollowDao;
import com.buzzinate.jianghu.util.ApiException;
import com.google.inject.Inject;
import com.mongodb.MongoException;

@Path("friendships")
@Produces({"application/json"})
public class FriendshipResource {
	private static Logger log = Logger.getLogger(FriendshipResource.class);
	
	@Inject private UserDao userDao;
	@Inject private FollowDao followDao;
	
	@POST
	@Path("create")
	public UserVO followUser(@HeaderParam("userId") long loginUserId, @FormParam("id") final Long id) throws ApiException {
		log.info("following user: " + id);
		try {
			User user = userDao.get(id);
			if (user == null) throw new NotFoundException("user not found, id=" + id);
			followDao.addFollowing(loginUserId, id);
			return new UserVO(user, true);
		} catch (MongoException.DuplicateKey e) {
			log.warn(loginUserId + " follow already " + id, e);
			throw new ApiException(ApiException.DUPLICATE, "已关注 " + id, e);
		}
	}
	
	@POST
	@Path("destroy")
	public UserVO unfollowUser(@HeaderParam("userId") long loginUserId, @FormParam("id") final Long id) {
		log.info("unfollowing user: " + id);
		User user = userDao.get(id);
		if (user == null) throw new NotFoundException("user not found, id=" + id);
		followDao.removeFollowing(loginUserId, id);
		return new UserVO(user, false);
	}
}
