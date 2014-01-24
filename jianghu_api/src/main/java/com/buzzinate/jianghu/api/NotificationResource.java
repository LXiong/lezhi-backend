package com.buzzinate.jianghu.api;

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
import com.buzzinate.jianghu.api.view.CommentVO;
import com.buzzinate.jianghu.api.view.CountVO;
import com.buzzinate.jianghu.api.view.ExtView;
import com.buzzinate.jianghu.api.view.MentionVO;
import com.buzzinate.jianghu.dao.CommentDao;
import com.buzzinate.jianghu.dao.FollowDao;
import com.buzzinate.jianghu.dao.MentionDao;
import com.buzzinate.jianghu.dao.StatusDao;
import com.buzzinate.jianghu.dao.UserStatusDao;
import com.buzzinate.jianghu.model.Comment;
import com.buzzinate.jianghu.model.Mention;
import com.buzzinate.jianghu.model.UserStatus;
import com.buzzinate.jianghu.sr.RecommendService;
import com.google.inject.Inject;

@Path("notification")
@Produces({"application/json"})
public class NotificationResource {
	private static Logger log = Logger.getLogger(NotificationResource.class);
	
	@Inject private UserDao userDao;
	@Inject private FollowDao followDao;
	@Inject private MentionDao mentionDao;
	@Inject private CommentDao commentDao;
	@Inject private StatusDao statusDao;
	@Inject private RecommendService recommendService;
	@Inject private UserStatusDao userStatusDao;
	
	@GET @Path("timeline")
	public ExtView<MentionVO>[] getFeed(@HeaderParam("userId") long loginUserId,
			@QueryParam("sinceId") @DefaultValue("-1") long sinceId,
			@QueryParam("maxId") @DefaultValue("-1") long maxId,
			@QueryParam("count") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page) {
		log.info("Fetch feed timeline for user " + loginUserId);
		List<Mention> mentions = mentionDao.findByUser(loginUserId, new Page(sinceId, maxId, count, page));
		
		UserStatus userStatus = userStatusDao.getOrNew(loginUserId);
		for (Mention m: mentions) {
			if (userStatus.getLastViewedMentionId() < m.getId()) userStatus.setLastViewedMentionId(m.getId());
		}
		userStatusDao.save(userStatus);
		
		List<Long> friendIds = followDao.findFriendIds(loginUserId);
		ExtView<MentionVO>[] result = new ExtView[mentions.size()];
		for (int i = 0; i < result.length; i++) {
			Mention m = mentions.get(i);
			if (m.getType() == Mention.COMMENT_MENTION) {
				Comment comment = commentDao.get(m.getSourceId());
				User user = userDao.get(comment.getUserId());
				MentionVO mo = new MentionVO(m, comment, user, friendIds.contains(user.getId()));
				
				CommentVO sco = null;
				if (comment.getCid() != -1) {
					Comment sc = commentDao.get(comment.getCid());
					User u = userDao.get(sc.getUserId());
					sco = new CommentVO(sc, u, friendIds.contains(u.getId()));
				}
				result[i] = ExtView.combine(mo, "source", sco);
			}
		}
		return result;
	}
	
	@GET @Path("count")
	public CountVO getCount(@HeaderParam("userId") long loginUserId) {
		UserStatus userStatus = userStatusDao.getOrNew(loginUserId);
		long newMenSize = mentionDao.countSince(loginUserId, userStatus.getLastViewedMentionId());
		List<Long> friendIds = followDao.findFriendIds(loginUserId);
		long newStatusSize = statusDao.countSince(friendIds, userStatus.getLastViewedStatusId());
		boolean hasNewRec = recommendService.checkNew(loginUserId, userStatus.getLastRecommendArticleTime());
		return new CountVO(newStatusSize > 0, newMenSize, hasNewRec);
	}
}
