package com.buzzinate.jianghu.jredis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.google.common.base.Function;

public class ValueCache<K, V> {
	private static Logger log = Logger.getLogger(ValueCache.class);
	public static final int ONE_HOUR = 3600;
	
	private static final String NIL = "nil";
	private ShardedJedisPool pool;
	private String prefix;
	private Converter<K> kc;
	private Converter<V> vc;
	private int expireTime;

	public ValueCache(ShardedJedisPool pool, String prefix, Converter<K> kc, Converter<V> vc, int expireTime) {
		this.pool = pool;
		this.prefix = prefix;
		this.kc =  kc;
		this.vc = vc;
		this.expireTime = expireTime;
	}
	
	public V getOrElse(final K key, final Function<K, V> load) {
		return JedisUtil.using(pool).call(new Function<ShardedJedis, V>(){
			@Override
			public V apply(ShardedJedis sj) {
				String rk = prefix + "." + kc.toString(key);
				String str = sj.get(rk);
				if (str == null || NIL.equals(str)) {
					V v = load.apply(key);
					if (v != null) sj.setex(rk, expireTime, vc.toString(v));
					return v;
				}
				else return vc.fromString(str);
			}
		});
	}
	
	public V put(final K key, final V value) {
		
		return JedisUtil.using(pool).call(new Function<ShardedJedis, V>(){

			@Override
			public V apply(ShardedJedis sj) {
				String rk = prefix + "." + kc.toString(key);
				String str = sj.set(rk, vc.toString(value));
				if (str == null || NIL.equals(str)) return null;
				else return vc.fromString(str);
			}
		});
	}
	
	public Map<K, V> getAllOrElse(final List<K> keys, final BatchFunction<K, V> load) {
		return JedisUtil.using(pool).call(new Function<ShardedJedis, Map<K, V>>(){
			@Override
			public Map<K, V> apply(ShardedJedis sj) {
				HashMap<K, V> result = new HashMap<K, V>();
				List<K> missKeys = new ArrayList<K>();
				for (K key: keys) {
					String rk = prefix + "." + kc.toString(key);
					String str = sj.get(rk);
					if (str == null || NIL.equals(str)) missKeys.add(key);
					else result.put(key, vc.fromString(str));
				}
				if (missKeys.size() > 0) {
					log.info(prefix + " miss total: " + missKeys.size());
					Map<K, V> newResult = load.apply(missKeys);
					result.putAll(newResult);
					for (Map.Entry<K, V> e: newResult.entrySet()) {
						String rk = prefix + "." + kc.toString(e.getKey());
						sj.setex(rk, expireTime, vc.toString(e.getValue()));
					}
				}
				return result;
			}
		});
	}
}