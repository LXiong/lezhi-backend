package com.buzzinate.jianghu.api;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;

import com.buzzinate.common.dao.Page;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.User;
import com.buzzinate.common.util.MentionUtil;
import com.buzzinate.jianghu.api.view.CommentVO;
import com.buzzinate.jianghu.api.view.ExtView;
import com.buzzinate.jianghu.dao.CommentDao;
import com.buzzinate.jianghu.dao.FollowDao;
import com.buzzinate.jianghu.dao.MentionDao;
import com.buzzinate.jianghu.model.Comment;
import com.buzzinate.jianghu.model.Mention;
import com.google.inject.Inject;

@Path("comments")
@Produces({"application/json"})
public class CommentResource {
	private static Logger log = Logger.getLogger(CommentResource.class);
	
	@Inject private UserDao userDao;
	@Inject private CommentDao commentDao;
	@Inject private FollowDao followDao;
	@Inject private MentionDao mentionDao;
	
	@GET @Path("timeline")
	public ExtView<CommentVO>[] getComments(@HeaderParam("userId") long loginUserId,
			@QueryParam("sinceId") @DefaultValue("-1") long sinceId,
			@QueryParam("maxId") @DefaultValue("-1") long maxId,
			@QueryParam("count") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page,
			@QueryParam("sourceId") Long sourceId) {
		List<Comment> comments = commentDao.getComments(sourceId, new Page(sinceId, maxId, count, page));
		List<Long> friendIds = followDao.findFriendIds(loginUserId);
		ExtView<CommentVO>[] result = new ExtView[comments.size()];
		for (int i = 0; i < result.length; i++) {
			Comment c = comments.get(i);
			User user = userDao.get(c.getUserId());
			
			CommentVO co = new CommentVO(c, user, friendIds.contains(user.getId()));
			CommentVO sco = null;
			if (c.getCid() != -1) {
				Comment sc = commentDao.get(c.getCid());
				User u = userDao.get(sc.getUserId());
				sco = new CommentVO(sc, u, friendIds.contains(u.getId()));
			}
			result[i] = ExtView.combine(co, "sourceComment", sco);
		}
		return result;
	}
	
	@POST @Path("create")
	@Encoded
	public ExtView<CommentVO> create(@HeaderParam("userId") long loginUserId, 
			@FormParam("id") Long id,
			@FormParam("comment") String comment,
			@FormParam("cid") @DefaultValue("-1") long cid) {
		Comment c = new Comment();
		c.setCid(cid);
		c.setUserId(loginUserId);
		c.setSourceId(id);
		c.setText(comment);
		c.setCreateAt(System.currentTimeMillis());
		commentDao.save(c);
		
		notifyMentions(c);
		
		List<Long> friendIds = followDao.findFriendIds(loginUserId);
		User loginUser = userDao.get(loginUserId);
		CommentVO co = new CommentVO(c, loginUser, false);
		CommentVO sco = null;
		if (cid != -1) {
			Comment sc = commentDao.get(c.getCid());
			User user = userDao.get(sc.getUserId());
			sco = new CommentVO(sc, user, friendIds.contains(user.getId()));
		}
		return ExtView.combine(co, "sourceComment", sco);
	}

	@POST @Path("destroy")
	public CommentVO destroy(@HeaderParam("userId") long loginUserId, @FormParam("id") final Long id) {
		Comment c = commentDao.get(id);
		commentDao.deleteById(id);
		User user = userDao.get(c.getUserId());
		List<Long> friendIds = followDao.findFriendIds(loginUserId);
		return new CommentVO(c, user, friendIds.contains(user.getId()));
	}
	
	private void notifyMentions(Comment c) {
		List<String> mentions = MentionUtil.parseMentions(c.getText());
		List<User> users = userDao.findByScreenName(mentions);
		for (User user: users) {
			Mention mention = new Mention();
			mention.setUserId(user.getId());
			mention.setSourceId(c.getId());
			mention.setType(Mention.COMMENT_MENTION);
			mention.setCreateAt(System.currentTimeMillis());
			mentionDao.addMention(mention);
		}
	}
}
