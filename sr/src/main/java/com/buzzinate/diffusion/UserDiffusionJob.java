package com.buzzinate.diffusion;

import static com.buzzinate.common.util.Constants.PREF_QUEUE;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.UserProfileDao;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.message.Message;
import com.buzzinate.common.message.Message.MessageType;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.model.User;
import com.buzzinate.common.model.UserProfile;
import com.buzzinate.common.queue.Handler;
import com.buzzinate.common.queue.MessageWrapper;
import com.buzzinate.common.queue.Queue;
import com.buzzinate.util.TimeStats;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * This job do incremental calculation of user diffusion profile.
 * It calculate the new user / page pairs by each user. For user A, it's similarity with
 * these users need to be recalculated:
 * - The users who already have similarity with A
 * - The users who also shared / liked these new pages
 * 
 *
 */
public class UserDiffusionJob implements Runnable {
	private static Logger log = Logger.getLogger(UserDiffusionJob.class);
	
	private Queue prefQueue;
	
	private ArticleDao articleDao;
	private PreferenceDao prefDao;
	private UserDao userDao;
	private UserProfileDao upDao;
	private Dictionary dict;
	
	@Inject
	public UserDiffusionJob(Datastore ds, ArticleDao articleDao, UserDao userDao, Dictionary dict, @Named(PREF_QUEUE) Queue prefQueue) {
		this.articleDao = articleDao;
		this.prefQueue = prefQueue;
		this.userDao = userDao;
		this.dict = dict;
		
		prefDao = new PreferenceDao(ds);
		this.upDao = new UserProfileDao(ds);
	}

	@Override
	public void run() {
		try {
			log.info("Start to update user diffusion profile ...");
			prefQueue.receive(Message.class, new Handler<MessageWrapper<Message>>() {

				@Override
				public void on(MessageWrapper<Message> mw) {
					Message msg = mw.getMessage();
					
					if (msg.type == MessageType.Liked) {
						like(msg.userId, msg.pageId, msg.isLike);
						
						User user = userDao.get(msg.userId);
						if (user.isLeziUser()) {
							UserProfile up = upDao.get(user.getId());
							if (up == null || up.getUserFeatures() == null || up.getUserFeatures().size() < 10) {
								log.info("Update user diffusion profile for user=" + user.getId());
								DiffusionProfile dp = UserDiffusionProfileBuilder.build(articleDao, prefDao, userDao, dict, user.getId());
								if (dp != null) upDao.update(user.getId(), dp.getUserFeatures(), dp.getKeywordFeatures());
							}
						}
					}
					
					TimeStats.infoSimUpdated();
				}
			}, true);
		} catch(IOException e) {
			log.error(e);
		}
	}
	
	public void like(long userId, long pageId, boolean isLike) {
		if (userId <= 0 && pageId <= 0) {
			return;
		}
		
		try {
			Query<Preference> q = prefDao.createQuery().filter("userId", userId).filter("pageId", pageId);
			Preference pref = prefDao.findOne(q);
			if (pref == null && isLike) {
				pref = new Preference (userId, pageId, System.currentTimeMillis());
				prefDao.save(pref);
			} else if (pref != null && !isLike) {
				prefDao.deleteByQuery(q);
			}
		} catch (Exception e) {
			// ingore, already liked in user feed
		}
	}
}
