package com.buzzinate.jianghu.jredis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.google.common.base.Function;

public class FieldValueCache<K, V> {
	private static Logger log = Logger.getLogger(FieldValueCache.class);
	
	public static final int ONE_HOUR = 3600;
	
	private ShardedJedisPool pool;
	private String prefix;
	private Converter<K> kc;
	private FieldConverter<V> vc;
	private int expireTime;

	public FieldValueCache(ShardedJedisPool pool, String prefix, Converter<K> kc, FieldConverter<V> vc, int expireTime) {
		this.pool = pool;
		this.prefix = prefix;
		this.kc =  kc;
		this.vc = vc;
		this.expireTime = expireTime;
	}
	
	public Map<K, V> getAllOrElse(final List<K> keys, final BatchFunction<K, V> load) {
		return JedisUtil.using(pool).call(new Function<ShardedJedis, Map<K, V>>(){
			@Override
			public Map<K, V> apply(ShardedJedis sj) {
				long start = System.currentTimeMillis();
				HashMap<K, V> result = new HashMap<K, V>();
				List<K> missKeys = new ArrayList<K>();
				for (K key: keys) {
					String rk = prefix + "." + kc.toString(key);
					Map<String, String> m = sj.hgetAll(rk);
					if (m == null || m.isEmpty()) missKeys.add(key);
					else result.put(key, vc.fromMap(m));
				}
				if (missKeys.size() > 0) {
					log.info(prefix + " miss total: " + missKeys.size());
					Map<K, V> newResult = load.apply(missKeys);
					result.putAll(newResult);
					for (Map.Entry<K, V> e: newResult.entrySet()) {
						String rk = prefix + "." + kc.toString(e.getKey());
						sj.hmset(rk, vc.toMap(e.getValue()));
						sj.expire(rk, expireTime);
					}
				}
				log.info("get article data cost: " + (System.currentTimeMillis() - start));
				return result;
			}
		});
	}
}