package com.buzzinate.jianghu.sr.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.impl.GenericObjectPool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class CacheUtils {
	public static String getset(ShardedJedisPool pool, String key, String value) {
		ShardedJedis r = pool.getResource();
		try {
			Jedis s = r.getShard(key);
			return s.getSet(key, value);
		} finally {
			pool.returnResource(r);
		}
	}
	
	public static String get(ShardedJedisPool pool, String key) {
		ShardedJedis r = pool.getResource();
		try {
			Jedis s = r.getShard(key);
			return s.get(key);
		} finally {
			pool.returnResource(r);
		}
	}
	
	public static void hmset(ShardedJedisPool pool, String key, HashMap<String, String> hash, int expireInSec) {
		ShardedJedis r = pool.getResource();
		try {
			Jedis s = r.getShard(key);
			s.hmset(key, hash);
			s.expire(key, expireInSec);
		} finally {
			pool.returnResource(r);
		}
	}
	
	public static HashMap<String, String> hmget(ShardedJedisPool pool, String key, List<String> fields) {
		ShardedJedis r = pool.getResource();
		try {
			Jedis s = r.getShard(key);
			List<String> values = s.hmget(key, fields.toArray(new String[0]));
			HashMap<String, String> result = new HashMap<String, String>();
			for (int i = 0; i < fields.size(); i++) {
				if (values.get(i) != null) result.put(fields.get(i), values.get(i));
			}
			return result;
		} finally {
			pool.returnResource(r);
		}
	}
	
	public static boolean exists(ShardedJedisPool pool, String key) {
		ShardedJedis r = pool.getResource();
		try {
			Jedis s = r.getShard(key);
			return s.exists(key);
		} finally {
			pool.returnResource(r);
		}
	}

	public static void cleanItems(ShardedJedisPool pool, String key, long expireTime) {
		ShardedJedis r = pool.getResource();
		try {
			Jedis s = r.getShard(key);
			s.zremrangeByScore(key, 0, expireTime);
		} finally {
			pool.returnResource(r);
		}
	}
	
	public static void cleanOldItems(ShardedJedisPool pool, String pattern, long expireTime) {
		ShardedJedis r = pool.getResource();
		try {
			for (Jedis s: r.getAllShards()) {
				Set<String> keys = s.keys(pattern);
				for (String key: keys) s.zremrangeByScore(key, 0, expireTime);
			}
		} finally {
			pool.returnResource(r);
		}
	}
	
	public static void main(String[] args) {
		ShardedJedisPool pool = create("localhost");
		HashMap<String, String> hash = new HashMap<String, String>();
		hash.put("one", "1");
		hash.put("two", "2");
		hmset(pool, "test", hash, 3600);
		
		HashMap<String, String> result = hmget(pool, "test", Arrays.asList("one", "two", "three"));
		System.out.println(result);
	}
	
	private static ShardedJedisPool create(String hoststr) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(100);
		config.setMaxIdle(20);
		config.setMaxWait(1000);
		config.setTestOnBorrow(true);

		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		String[] hosts = StringUtils.split(hoststr, ",");
		for (String host: hosts) {
			JedisShardInfo si = new JedisShardInfo(host);
			shards.add(si);
		}
		
		GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
		poolConfig.testWhileIdle = true;
		poolConfig.minEvictableIdleTimeMillis = 60000;
		poolConfig.timeBetweenEvictionRunsMillis = 30000;
		poolConfig.numTestsPerEvictionRun = -1;
		return new ShardedJedisPool(poolConfig, shards);
	}
}