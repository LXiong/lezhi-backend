package com.buzzinate.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.buzzinate.lezhi.api.Client;

import com.buzzinate.classify.TrainClassify;
import com.buzzinate.common.dao.AreaArticleDao;
import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.BlackSiteDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.VocabularyDao;
import com.buzzinate.common.dao.mongo.ArticleDaoImpl;
import com.buzzinate.common.dao.mongo.UserDaoImpl;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.model.Similarity;
import com.buzzinate.common.util.Constants;
import com.buzzinate.dao.FeedUserDao;
import com.buzzinate.link.BlackSiteDetector;
import com.buzzinate.model.FeedUser;
import com.buzzinate.weibo.WeiboExt;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;

public class MyModule implements Module {
	private Logger log = Logger.getLogger(MyModule.class);
	
	@Override
	public void configure(Binder binder) {
		try {
			Properties prop = getProperties("config.properties");
			prop.putAll(getProperties("config.properties.local"));
			Names.bindProperties(binder, prop);
			
			binder.requestStaticInjection(TrainClassify.class);
			binder.requestStaticInjection(WeiboExt.class);
			
			String mongourl = prop.getProperty(Constants.KEY_MONGO_URL, "mongodb://127.0.0.1:27017");
			Mongo mongo = new Mongo(new MongoURI(mongourl));
			
			Morphia morphia = new Morphia();
			Datastore datastore = morphia.mapPackage(Similarity.class.getPackage().getName())
				.mapPackage(FeedUser.class.getPackage().getName())
				.createDatastore(mongo, "jianghu");
			datastore.ensureIndexes();
			binder.bind(Datastore.class).toInstance(datastore);
			binder.bind(ArticleDao.class).toInstance(new ArticleDaoImpl(datastore));
			binder.bind(UserDao.class).toInstance(new UserDaoImpl(datastore));
			binder.bind(FeedUserDao.class).toInstance(new FeedUserDao(datastore));
			binder.bind(BlackSiteDao.class).toInstance(new BlackSiteDao(datastore));
			binder.bind(AreaArticleDao.class).toInstance(new AreaArticleDao(datastore));
			binder.bind(BlackSiteDetector.class);
			
			String elastichosts = prop.getProperty(Constants.KEY_ELASTICSEARCH_HOSTS, "localhost");
			Client client = new Client(elastichosts);
			binder.bind(Client.class).toInstance(client);
			
			Dictionary dict = new Dictionary(new VocabularyDao(datastore).createQuery().asList());
			binder.bind(Dictionary.class).toInstance(dict);
			
			morphia.getMapper().addInterceptor(new PrefSizeUpdater(datastore));
		} catch (UnknownHostException e) {
			log.error(e);
		} catch (MongoException e) {
			log.error(e);
		}
	}

	private Properties getProperties(String filename) {
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = ClassLoader.getSystemResourceAsStream(filename);
			if (is != null) {
				prop.load(is);
			}
			return prop;
		} catch (Exception e) {
			log.warn("Error while loading file: " + filename, e);
			return prop;
		} finally {
			try {
				if (is != null) is.close();
			} catch (IOException e) {}
		}
	}
}
