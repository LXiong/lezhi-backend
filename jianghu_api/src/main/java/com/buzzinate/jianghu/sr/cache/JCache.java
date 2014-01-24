package com.buzzinate.jianghu.sr.cache;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.alibaba.fastjson.parser.ParserConfig;
import com.buzzinate.jianghu.jredis.JedisUtil;
import com.google.common.base.Function;

public class JCache {
	private static Logger log = Logger.getLogger(JCache.class);
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
	private ShardedJedisPool pool;
	private ScheduledExecutorService bgExecutors;
	
	static {
		ParserConfig.getGlobalInstance().setAsmEnable(false);
	}

	public JCache(ShardedJedisPool pool) {
		this.pool = pool;
		this.bgExecutors = Executors.newScheduledThreadPool(100);
	}

	public <K, T> T sync(final String prefix, K id, Class<T> clazz, final long delay, Function<K, T> load) {
		T result = null;
		
		String cacheValue = getCache(prefix + id);
		if (cacheValue == null || "nil".equals(cacheValue)) {
			result = load.apply(id);
			update(prefix + id, 5 * delay / 1000, new JEntry<T>(result).toJson());
		} else {
			JEntry<T> cr = JEntry.parseJson(cacheValue, clazz);
			result = cr.value;
			if (cr.cacheTime + delay < System.currentTimeMillis()) {
				log.info("sync update " + prefix + id + " since " + df.format(new Date(cr.cacheTime)));
				loadAndUpdate(prefix, id, 5 * delay / 1000, load);
			}
		}
		
		return result;
	}
	
	public <K, T> List<T> syncs(final String prefix, final K id, Class<T> clazz, final long delay, Function<K, List<T>> load) {
		List<T> result = null;
		String cacheValue = getCache(prefix + id);
		if (cacheValue == null || "nil".equals(cacheValue)) {
			result = load.apply(id);
			update(prefix + id, 5 * delay / 1000, new JEntryList<T>(result).toJson());
		} else {
			JEntryList<T> cr = JEntryList.parseJson(cacheValue, clazz);
			result = cr.values;
			if (cr.cacheTime + delay < System.currentTimeMillis()) {
				log.info("syncs update " + prefix + id + " since " + df.format(new Date(cr.cacheTime)));
				loadAndUpdateList(prefix, id, 5 * delay / 1000, load);
			}
		}
		
		return result;
	}
	
	public <K, T> Map<K, List<T>> batchSyncs(final String prefix, final List<K> ids, Class<T> clazz, final long delay, final Function<List<K>, Map<K, List<T>>> load) {
		final Map<K, List<T>> result = new HashMap<K, List<T>>();
		
		final List<K> checkIds = new ArrayList<K>();
		final List<K> bgCheckIds = new ArrayList<K>();
		for (K id: ids) {
			String cacheValue = getCache(prefix + id);
			if (cacheValue == null || "nil".equals(cacheValue)) checkIds.add(id);
			else {
				JEntryList<T> cr = JEntryList.parseJson(cacheValue, clazz);
				result.put(id, cr.values);
				if (cr.cacheTime + delay < System.currentTimeMillis()) {
					log.info("batchsyncs update " + prefix + id + " since " + df.format(new Date(cr.cacheTime)));
					bgCheckIds.add(id);
				}
			}
		}
		result.putAll(load.apply(checkIds));
		
		// submit tasks
		bgExecutors.submit(new Callable<Object>(){
			@Override
			public Object call() throws Exception {
				for(Map.Entry<K, List<T>> e: load.apply(bgCheckIds).entrySet()) {
					JEntryList<T> je = new JEntryList<T>(e.getValue());
					updateCache(prefix + e.getKey(), je.toJson(), 5 * delay / 1000);
				}
				for (K checkId: checkIds) {
					List<T> vs = result.get(checkId);
					JEntryList<T> je = new JEntryList<T>(vs);
					updateCache(prefix + checkId, je.toJson(), 5 * delay / 1000);
				}
				return null;
			}
		});
				
		return result;
	}
	
	public <K, T> List<T> asyncs(final String prefix, final K id, Class<T> clazz, final long delay, Function<K, List<T>> load) {
		List<T> result = null;
		String cacheValue = getCache(prefix + id);
		if (cacheValue == null || "nil".equals(cacheValue)) {
			result = new ArrayList<T>();
			loadAndUpdateList(prefix, id, 5 * delay / 1000, load);
		} else {
			JEntryList<T> cr = JEntryList.parseJson(cacheValue, clazz);
			result = cr.values;
			if (cr.cacheTime + delay < System.currentTimeMillis()) {
				log.info("asyncs update " + prefix + id + " since " + df.format(new Date(cr.cacheTime)));
				loadAndUpdateList(prefix, id, 5 * delay / 1000, load);
			}
		}
		
		return result;
	}
	
	private String getCache(final String key) {
		return JedisUtil.using(pool).call(new Function<ShardedJedis, String>(){
			@Override
			public String apply(ShardedJedis jedis) {
				return jedis.get(key);
			}
		});
	}
	
	private void updateCache(final String key, final String value, final long delaySecs) {
		JedisUtil.using(pool).call(new Function<ShardedJedis, String>(){
			@Override
			public String apply(ShardedJedis jedis) {
				jedis.set(key, value);
				jedis.expire(key, (int)delaySecs);
				return null;
			}
		});
	}

	private <K, T> void loadAndUpdate(final String prefix, final K id, final long expireSecs, final Function<K, T> load) {
		bgExecutors.submit(new Callable<Object>(){
			@Override
			public Object call() throws Exception {
				T result = load.apply(id);
				JEntry<T> je = new JEntry<T>(result);
				updateCache(prefix + id, je.toJson(), expireSecs);
				return null;
			}
		});
	}
	
	private <K, T> void loadAndUpdateList(final String prefix, final K id, final long expireSecs, final Function<K, List<T>> load) {
		bgExecutors.submit(new Callable<Object>(){
			@Override
			public Object call() throws Exception {
				List<T> result = load.apply(id);
				JEntryList<T> je = new JEntryList<T>(result);
				updateCache(prefix + id, je.toJson(), expireSecs);
				return null;
			}
		});
	}

	private <T> void update(final String id, final long expireSecs, final String json) {
		bgExecutors.submit(new Callable<Object>(){
			@Override
			public Object call() throws Exception {
				updateCache(id, json, expireSecs);
				return null;
			}
		});
	}
}