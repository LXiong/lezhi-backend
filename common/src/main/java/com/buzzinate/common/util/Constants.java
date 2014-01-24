package com.buzzinate.common.util;

import java.util.Arrays;
import java.util.HashSet;

public class Constants {
	private Constants() { }
	
	public static final String CONSUMER_KEY = "1548997549";
	public static final String CONSUMER_SECRET = "b857a6ee0be123a3a1d77e86acdf0be7";
	
	public static final long BASE_RANK_TIME = 1293811200000l;	// 2011-1-1
		
	public static final String CRAWL_QUEUE = "crawl";
	public static final String REPOST_QUEUE = "repost";
	public static final String CLASSIFY_QUEUE = "classify";
	public static final String PREF_QUEUE = "pref";
	
	public static final String KEY_MONGO_URL = "mongodb.url";
	public static final String KEY_QUEUE_SIZE = "crawl.queueSize";
	public static final String KEY_THREAD_NUM = "crawl.threadNum";
	
	public static final String KEY_ELASTICSEARCH_HOSTS = "elasticsearch.hosts";
	
	public static final String KEY_OLYMPIC_QUEUE_SIZE = "olympic.crawl.queueSize";
	public static final String KEY_OLYMPIC_THREAD_NUM = "olympic.crawl.threadNum";
	
	public static final String KEY_VERTICAL_QUEUE_SIZE = "vertical.crawl.queueSize";
	public static final String KEY_VERTICAL_THREAD_NUM = "vertical.crawl.threadNum";
	public static final String KEY_VERTICAL_START_URL  = "vertical.crawl.startUrl";
	public static final String KEY_VERTICAL_CATEGORY = "vertical.crawl.category";
	
	public static final String KEY_ITEM_NUM = "rec.howManyItem";
	public static final String KEY_REC_INTERVAL = "rec.interval";
	public static final String KEY_LDA_MODEL_ROOT = "lda.model.root";
	
	public static final String KEY_MODEL_PATH = "rec.modelPath";
	public static final String KEY_WEIBO_INTERVAL = "weibo.interval";
	
	public static final String RSS_FEED_BASELINK = "rss.baselink";
	
	private static final int ONE_MINUTE = 1000 * 60;
	public static final long ONE_HOUR = ONE_MINUTE * 60;
	public static final long ONE_DAY = ONE_HOUR * 24;
	public static final long ONE_WEEK = ONE_DAY * 7;
	
	public static final long RECOMM_RANGE = ONE_DAY * 5;
		
	public static final int DEFAULT_INTERVAL = ONE_MINUTE * 300;
	public static final int MIN_INTERVAL = ONE_MINUTE * 120;
	public static final int MAX_INTERVAL = ONE_MINUTE * 720;
	
	public static final double BETA = 1.8;
	
	public static final int TOP_NEIGHBOR_NUM = 50;
	
	public static final int ITEM_MINHASH_NUM = 25;
	
	public static final int GEO_TWEET_MINSIZE = 1;
	public static final double GEO_TWEET_CAPACITY = 0.9;
	
	public static final int TREND_MIN_LENGTH = 5;
	public static final int TREND_INCLUDED_GRAM_NUMS = 2;
	
	public static final double REPOST_CAPACITY = 0.6;
	
	public static final HashSet<String> TARGET_LANGUAGES = new HashSet<String>();
	public static final HashSet<String> FILT_LANGUAGES = new HashSet<String>();
	static {
		TARGET_LANGUAGES.addAll(Arrays.asList("en", "zh-cn", "zh-tw"));
		FILT_LANGUAGES.addAll(Arrays.asList("ko", "ja"));
	}
}