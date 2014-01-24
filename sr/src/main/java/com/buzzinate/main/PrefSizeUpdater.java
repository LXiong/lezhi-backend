package com.buzzinate.main;

import org.apache.log4j.Logger;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.mongo.ArticleDaoImpl;
import com.buzzinate.common.dao.mongo.UserDaoImpl;
import com.buzzinate.common.model.Preference;
import com.google.code.morphia.AbstractEntityInterceptor;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.mapping.Mapper;
import com.mongodb.DBObject;

public class PrefSizeUpdater extends AbstractEntityInterceptor {
	private Logger log = Logger.getLogger(PrefSizeUpdater.class);
	
	private UserDao userDao;
	private ArticleDao articleDao;
	
	public PrefSizeUpdater(Datastore ds) {
		this.userDao = new UserDaoImpl(ds);
		this.articleDao = new ArticleDaoImpl(ds);
	}

	@Override
	public void postPersist(Object ent, DBObject dbObj, Mapper mapr) {
		// 当新插入Preference时，增加相应的user和article的prefsize
		if (ent instanceof Preference) {
			try {
				Preference p = (Preference) ent;
				userDao.incPrefSize(p.getUserId());
				articleDao.incPrefSize(p.getPageId());
			} catch (Throwable t) {
				log.warn("could not incr user or article prefSize", t);
			}
		}
	}
}
