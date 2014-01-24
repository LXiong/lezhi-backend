package com.buzzinate.jianghu.jredis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool.impl.GenericObjectPool.Config;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

public class TestJedis {

	public static void main(String[] args) {
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		JedisShardInfo si = new JedisShardInfo("localhost", 6379);
		shards.add(si);
		ShardedJedisPool pool = new ShardedJedisPool(new Config(), shards);
		pool = JedisUtil.destroyOnShutdown(pool);
		List<Long> keys = Arrays.asList(1L, 2L, 43L, 9L, 11L);
		//List<Long> ids = Arrays.asList(1L, 2L, 43L, 9L);
		ValueCache<Long, List<Long>> vc = new ValueCache<Long, List<Long>>(pool, "testlist", Converters.LONG, Converters.LONG_LIST, ValueCache.ONE_HOUR);
		Map<Long, List<Long>> r = vc.getAllOrElse(keys, new BatchFunction<Long, List<Long>>(){

			@Override
			public Map<Long, List<Long>> apply(List<Long> keys) {
				HashMap<Long, List<Long>> newResult = new HashMap<Long, List<Long>>();
				for (Long key: keys) {
					newResult.put(key, Arrays.asList(key));
				}
				return newResult;
			}
			
		});
		System.out.println("result: " + r);
	}
}