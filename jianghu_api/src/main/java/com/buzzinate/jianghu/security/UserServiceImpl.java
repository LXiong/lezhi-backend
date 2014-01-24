package com.buzzinate.jianghu.security;

import static com.buzzinate.common.util.Constants.CONSUMER_KEY;
import static com.buzzinate.common.util.Constants.CONSUMER_SECRET;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.UnauthorizedException;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import weibo4j2.Users;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.UserWeiboDao;
import com.buzzinate.common.model.User;
import com.buzzinate.common.model.UserWeibo;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;

public class UserServiceImpl implements UserService {
	private static Logger log = Logger.getLogger(UserServiceImpl.class);
	
	private UserDao userDao;
	private UserWeiboDao uwDao;
	private ShardedJedisPool pool;
	private ThreadLocal<weibo4j.Weibo> weibo = new ThreadLocal<weibo4j.Weibo>() {
		@Override
		protected weibo4j.Weibo initialValue() {
			weibo4j.Weibo w = new weibo4j.Weibo();
			w.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
			return w;
		}
	};
	
	@Inject
	public UserServiceImpl(Datastore ds, ArticleDao articleDao, UserDao userDao, ShardedJedisPool pool) {
		this.userDao = userDao;
		uwDao = new UserWeiboDao(ds);
		this.pool = pool;
	}

	public long verifyUser(String accessToken) throws UnauthorizedException {
		long userId = checkCache(accessToken);
		if (userId != -1) return userId;
		
		User user = userDao.findByAccessToken(accessToken);
		if (user == null) throw new UnauthorizedException();
		cacheUser(accessToken, user.getId());
		return user.getId();
	}

	@Override
	public long login(String accessToken, String secret) throws UnauthorizedException {
		weibo.get().setToken(accessToken, secret);
		try {
			User user = userDao.findByAccessToken(accessToken);
			if (user != null && user.getSecret() != null) {
				if (!user.getSecret().equals(secret)) throw new UnauthorizedException("Incorrect secret");
				else {
					cacheUser(accessToken, user.getId());
					return user.getId();
				}
			}
			
			weibo4j.User sinaUser = weibo.get().verifyCredentials();
			
			boolean isNewUser = false;
			user = userDao.findByUid(sinaUser.getId());
			if (user == null) {
				user = new User();
				user.setName(sinaUser.getName());
				user.setScreenName(sinaUser.getScreenName());
				user.setProfileImageUrl(sinaUser.getProfileBackgroundImageUrl());
				user.setUid(sinaUser.getId());
				
				user.setAccessToken(accessToken);
				user.setSecret(secret);
				user.setLeziUser(true);
				userDao.save(user);
				
				isNewUser = true;
			} else {
				if (!user.isLeziUser()) isNewUser = true;
				
				user.setAccessToken(accessToken);
				user.setSecret(secret);
				user.setLeziUser(true);
				userDao.save(user);
			}
			
			if (isNewUser) {
				for (UserWeibo uw: WeiboExtractor.extractUserWeibos(weibo.get(), user)) {
					uwDao.save(uw);
				}
			}
			
			cacheUser(accessToken, user.getId());
			
			return user.getId();
		} catch (weibo4j.WeiboException e) {
			if (e.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) { 
				throw new UnauthorizedException(e);
			}
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public long login2(String accessToken, long uid) throws UnauthorizedException {
		try {
			boolean isNewUser = false;
			User user = userDao.findByUid(uid);
			if (user == null) {
				user = new User();
				weibo4j2.Weibo.client.setToken(accessToken);
				weibo4j2.model.User sinaUser = new Users().showUserById(String.valueOf(uid));
				user.setName(sinaUser.getName());
				user.setScreenName(sinaUser.getScreenName());
				user.setProfileImageUrl(sinaUser.getProfileImageUrl());
				user.setUid(uid);
				user.setAccessToken(accessToken);
				user.setLeziUser(true);
				userDao.save(user);
				
				isNewUser = true;
			} else {
				if (!user.isLeziUser()) isNewUser = true;
				user.setAccessToken(accessToken);
				user.setLeziUser(true);
				userDao.save(user);
			}
			
			if (isNewUser) {
				for (UserWeibo uw: WeiboExtractor2.extractWeibos(user.getId(), String.valueOf(uid), accessToken)) {
					uwDao.save(uw);
				}
			}
			
			cacheUser(accessToken, user.getId());
			
			return user.getId();
		} catch (weibo4j2.model.WeiboException e) {
			if (e.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) { 
				throw new UnauthorizedException(e);
			}
			throw new RuntimeException(e);
		}
	}

	private long checkCache(String accessToken) {
		ShardedJedis jedis = pool.getResource();
		try {
			String id = jedis.get(accessToken);
			if (id == null || id.equals("nil")) return -1;
			else return Long.parseLong(id);
		} catch(Exception e) {
			log.warn("Redis error", e);
			return -1;
		} finally {
			pool.returnResource(jedis);
		}
	}

	private void cacheUser(String accessToken, long userId) {
		ShardedJedis jedis = pool.getResource();
		try {
			jedis.set(accessToken, String.valueOf(userId));
		} catch(Exception e) {
			log.warn("Redis error", e);
		} finally {
			pool.returnResource(jedis);
		}
	}
}
