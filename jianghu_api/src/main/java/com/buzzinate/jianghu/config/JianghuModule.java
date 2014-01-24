package com.buzzinate.jianghu.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.buzzinate.lezhi.api.Client;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.VocabularyDao;
import com.buzzinate.common.dao.mongo.ArticleDaoImpl;
import com.buzzinate.common.dao.mongo.UserDaoImpl;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.util.Constants;
import com.buzzinate.jianghu.api.ApiStatusResource;
import com.buzzinate.jianghu.api.ArticleResource;
import com.buzzinate.jianghu.api.CommentResource;
import com.buzzinate.jianghu.api.CoverResource;
import com.buzzinate.jianghu.api.EngineResource;
import com.buzzinate.jianghu.api.FeedResource;
import com.buzzinate.jianghu.api.FriendshipResource;
import com.buzzinate.jianghu.api.NotificationResource;
import com.buzzinate.jianghu.api.UserResource;
import com.buzzinate.jianghu.api.servlet.FeedServlet;
import com.buzzinate.jianghu.dao.AreaArticleDao;
import com.buzzinate.jianghu.dao.ArticleTypeDao;
import com.buzzinate.jianghu.dao.CommentDao;
import com.buzzinate.jianghu.dao.CoverStoryDao;
import com.buzzinate.jianghu.dao.FeedbackDao;
import com.buzzinate.jianghu.dao.FollowDao;
import com.buzzinate.jianghu.dao.LikeDao;
import com.buzzinate.jianghu.dao.MentionDao;
import com.buzzinate.jianghu.dao.ReadDao;
import com.buzzinate.jianghu.dao.StatusDao;
import com.buzzinate.jianghu.dao.TrendArticleDao;
import com.buzzinate.jianghu.dao.TrendDao;
import com.buzzinate.jianghu.dao.UserStatusDao;
import com.buzzinate.jianghu.dao.mongo.AreaArticleDaoImpl;
import com.buzzinate.jianghu.dao.mongo.ArticleTypeDaoImpl;
import com.buzzinate.jianghu.dao.mongo.CommentDaoImpl;
import com.buzzinate.jianghu.dao.mongo.CoverStoryDaoImpl;
import com.buzzinate.jianghu.dao.mongo.FollowDaoImpl;
import com.buzzinate.jianghu.dao.mongo.LikeDaoImpl;
import com.buzzinate.jianghu.dao.mongo.MentionDaoImpl;
import com.buzzinate.jianghu.dao.mongo.ReadDaoImpl;
import com.buzzinate.jianghu.dao.mongo.StatusDaoImpl;
import com.buzzinate.jianghu.dao.mongo.StoryTypeConvert;
import com.buzzinate.jianghu.dao.mongo.TrendArticleDaoImpl;
import com.buzzinate.jianghu.dao.mongo.TrendDaoImpl;
import com.buzzinate.jianghu.dao.mongo.UserStatusDaoImpl;
import com.buzzinate.jianghu.model.Comment;
import com.buzzinate.jianghu.model.CoverStory;
import com.buzzinate.jianghu.security.DebugOAuthFilter;
import com.buzzinate.jianghu.security.OAuthFilter;
import com.buzzinate.jianghu.security.SecurityExceptionMapper;
import com.buzzinate.jianghu.security.UserService;
import com.buzzinate.jianghu.security.UserServiceImpl;
import com.buzzinate.jianghu.sr.RecommendService;
import com.buzzinate.jianghu.sr.cache.JCache;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.MongoURI;
import com.mongodb.ReadPreference;

public class JianghuModule implements Module {
    private Logger log = Logger.getLogger(JianghuModule.class);

    public void configure(final Binder binder) {
        try {
            Properties prop = getProperties("config.properties");
            prop.putAll(getProperties("config.properties.local"));
            Names.bindProperties(binder, prop);

            String url = prop.getProperty(Constants.KEY_MONGO_URL, "mongodb://127.0.0.1:27017");
            MongoURI mongourl = new MongoURI(url);
            MongoOptions options = mongourl.getOptions();
            options.connectionsPerHost = 100;
            options.threadsAllowedToBlockForConnectionMultiplier = 10;
            Mongo mongo = new Mongo(mongourl);
            mongo.setReadPreference(ReadPreference.SECONDARY);

            Morphia morphia = new Morphia();
            Datastore datastore = morphia.mapPackage(Article.class.getPackage().getName())
                    .mapPackage(Comment.class.getPackage().getName()).createDatastore(mongo, "jianghu");
            datastore.ensureIndexes();

            morphia.getMapper().getConverters().addConverter(StoryTypeConvert.class);
            morphia.map(CoverStory.StoryType.class);

            binder.bind(Datastore.class).toInstance(datastore);

            binder.bind(EngineResource.class);
            binder.bind(ApiStatusResource.class);
            binder.bind(FeedResource.class);
            binder.bind(UserResource.class);
            binder.bind(ArticleResource.class);
            binder.bind(FriendshipResource.class);
            binder.bind(CommentResource.class);
            binder.bind(NotificationResource.class);
            binder.bind(CoverResource.class);
			binder.bind(FeedServlet.class);

			binder.bind(PreferenceDao.class).toInstance(new PreferenceDao(datastore));
            binder.bind(UserDao.class).toInstance(new UserDaoImpl(datastore));
            binder.bind(ArticleDao.class).toInstance(new ArticleDaoImpl(datastore));
            binder.bind(CommentDao.class).to(CommentDaoImpl.class);
            binder.bind(StatusDao.class).to(StatusDaoImpl.class);
            binder.bind(MentionDao.class).to(MentionDaoImpl.class);
            binder.bind(FollowDao.class).to(FollowDaoImpl.class);
            binder.bind(LikeDao.class).to(LikeDaoImpl.class);
            binder.bind(ReadDao.class).to(ReadDaoImpl.class);
            binder.bind(UserStatusDao.class).to(UserStatusDaoImpl.class);
            binder.bind(CoverStoryDao.class).to(CoverStoryDaoImpl.class);

            binder.bind(ArticleTypeDao.class).to(ArticleTypeDaoImpl.class);
			binder.bind(TrendDao.class).to(TrendDaoImpl.class);
			binder.bind(TrendArticleDao.class).to(TrendArticleDaoImpl.class);
			binder.bind(AreaArticleDao.class).to(AreaArticleDaoImpl.class);
			
            binder.bind(FeedbackDao.class);
            Dictionary dict = new Dictionary(new VocabularyDao(datastore).createQuery().asList());
            binder.bind(Dictionary.class).toInstance(dict);

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxActive(100);
            config.setMaxIdle(20);
            config.setMaxWait(1000);
            config.setTestOnBorrow(true);

            List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
            String[] hosts = StringUtils.split(prop.getProperty("redis.hosts"), ",");
            for (String host : hosts) {
                JedisShardInfo si = new JedisShardInfo(host);
                shards.add(si);
            }

            GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
            poolConfig.testWhileIdle = true;
            poolConfig.minEvictableIdleTimeMillis = 60000;
            poolConfig.timeBetweenEvictionRunsMillis = 30000;
            poolConfig.numTestsPerEvictionRun = -1;
            ShardedJedisPool pool = new ShardedJedisPool(poolConfig, shards);
            binder.bind(ShardedJedisPool.class).toInstance(pool);

			JCache jcache = new JCache(pool);
			binder.bind(JCache.class).toInstance(jcache);
			
			String elastichosts = prop.getProperty(Constants.KEY_ELASTICSEARCH_HOSTS, "localhost");
			Client client = new Client(elastichosts);
			binder.bind(Client.class).toInstance(client);
			
            binder.bind(RecommendService.class).asEagerSingleton();

            binder.bind(UserService.class).to(UserServiceImpl.class);

            String mode = prop.getProperty("mode", "production");
            if (mode.equals("production"))
                binder.bind(OAuthFilter.class);
            if (mode.equals("development"))
                binder.bind(DebugOAuthFilter.class);
            binder.bind(SecurityExceptionMapper.class);
            binder.bind(ApiExceptionMapper.class);
			binder.bind(CommonExceptionMapper.class);
        } catch (UnknownHostException e) {
            log.error(e);
        } catch (MongoException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private Properties getProperties(String filename) {
        Properties prop = new Properties();
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            if (is == null) {
                log.warn("Could not found config file: " + filename);
                return prop;
            } else
                log.info("Found config file: " + filename);

            prop.load(is);
            return prop;
        } catch (IOException e) {
            log.warn("Error while loading file: " + filename, e);
            return prop;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
        }
    }
}