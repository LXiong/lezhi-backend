package com.buzzinate.jianghu.sr.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.buzzinate.jianghu.jredis.BatchFunction;
import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.cache.Weigher;
import com.google.common.collect.ImmutableMap;

class CacheEntry<V> {
	private V value;
	private long cacheTime;
	
	public CacheEntry(V value) {
		this(value, System.currentTimeMillis());
	}
	
	public CacheEntry(V value, long cacheTime) {
		this.value = value;
		this.cacheTime = cacheTime;
	}

	public V getValue() {
		return value;
	}

	public long getCacheTime() {
		return cacheTime;
	}
}

public class LocalCacher<K, V> {
	private static ConcurrentLinkedQueue<Cache<?, ?>> caches = new ConcurrentLinkedQueue<Cache<?, ?>>();
	private Cache<K, CacheEntry<V>> cache = CacheBuilder.newBuilder()
				.maximumWeight(10000)
				.weigher(new Weigher<K, CacheEntry<V>>() {
					@Override
					public int weigh(K key, CacheEntry<V> ce) {
						if (List.class.isAssignableFrom(ce.getValue().getClass())) {
							List<?> vs = (List<?>) ce.getValue();
							return vs.size();
						} else return 1;
					}
				})
				.concurrencyLevel(16)
				.recordStats()
				.build();
	
	private long expireAfterCache;
	
	public LocalCacher(long expireAfterCache) {
		this.expireAfterCache = expireAfterCache;
		caches.add(cache);
	}
	
	public static CacheStats getStats() {
		CacheStats cs = new CacheStats(0, 0, 0, 0, 0, 0);
		for (Cache<?, ?> cache: caches) {
			cs = cs.plus(cache.stats());
		}
		return cs;
	}
	
	public void update(K key, V value) {
		CacheEntry<V> ce = cache.getIfPresent(key);
		if (ce == null) ce = new CacheEntry<V>(value);
		else ce = new CacheEntry<V>(value, ce.getCacheTime());
		cache.put(key, ce);
	}
	
	public V getOrElse(K key, Function<K, V> load) {
		CacheEntry<V> ce = cache.getIfPresent(key);
		long now = System.currentTimeMillis();
		if (ce == null || now - ce.getCacheTime() > expireAfterCache) {
			V value = load.apply(key);
			if (value == null) return value;
			
			ce = new CacheEntry<V>(value);
			cache.put(key, ce);
		}
		return ce.getValue();
	}
	
	public Map<K, V> getAllOrElse(List<K> keys, BatchFunction<K, V> load) {
		ImmutableMap<K, CacheEntry<V>> cacheResult = cache.getAllPresent(keys);
		
		HashMap<K, V> result = new HashMap<K, V>();
		for (Map.Entry<K, CacheEntry<V>> e: cacheResult.entrySet()) {
			result.put(e.getKey(), e.getValue().getValue());
		}
		
		List<K> missKeys = new ArrayList<K>();
		for (K key: keys) {
			if (!cacheResult.containsKey(key)) missKeys.add(key);
		}
		missKeys.addAll(expireKeys(cacheResult, expireAfterCache));
		
		if (missKeys.size() > 0) {
			Map<K, V> newResult = load.apply(missKeys);
			for (Map.Entry<K, V> e: newResult.entrySet()) {
				cache.put(e.getKey(), new CacheEntry<V>(e.getValue()));
				result.put(e.getKey(), e.getValue());
			}
		}
		
		return result;
	}
	
	private static <K, V> List<K> expireKeys(Map<K, CacheEntry<V>> m, long expireAfterCache) {
		List<K> keys = new ArrayList<K>();
		long now = System.currentTimeMillis();
		for (Map.Entry<K, CacheEntry<V>> e: m.entrySet()) {
			if (now - e.getValue().getCacheTime() > expireAfterCache) keys.add(e.getKey());
		}
		return keys;
	}
	
	public static void main(String[] args) throws InterruptedException {
		LocalCacher<String, List<String>> local = new LocalCacher<String, List<String>>(1000L * 20);
		local.getOrElse("expire", new Function<String, List<String>>(){

			@Override
			public List<String> apply(String key) {
				return Arrays.asList("expire value");
			}
			
		});
		
		Thread.sleep(1000L * 20);
		Map<String, List<String>> result = local.getAllOrElse(Arrays.asList("test", "expire"), new BatchFunction<String, List<String>>() {
			@Override
			public Map<String, List<String>> apply(List<String> keys) {
				HashMap<String, List<String>> result = new HashMap<String, List<String>>();
				for (String key: keys) result.put(key, Arrays.asList(key + " value"));
				return result;
			}
			
		});
		System.out.println(result);
		System.out.println(LocalCacher.getStats());
	}
}