package com.buzzinate.crawl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.MapMaker;

public class LocalCacher<K, V> {
	private ConcurrentMap<K, V> caches = new MapMaker()
    	.expiration(5, TimeUnit.HOURS)
    	.makeMap();
	
	public List<K> checkCache(List<K> keys, Map<K, V> result) {
		List<K> miss = new ArrayList<K>();
		for (K key: keys) {
			V value = caches.get(key);
			if (value == null) miss.add(key);
			else result.put(key, value);
		}
		return miss;
	}
	
	public void refresh(Map<K, V> result, List<K> missKeys) {
		for (K key: missKeys) {
			V value = result.get(key);
			if (value != null) caches.put(key, value);
		}
	}
	
	public void put(K key, V value) {
		caches.put(key, value);
	}
	
	public static void main(String[] args) {
		LocalCacher<String, String> local = new LocalCacher<String, String>();
		HashMap<String, String> result = new HashMap<String, String>();
		List<String> miss = local.checkCache(Arrays.asList("test"), result);
		result.put("test", "test value");
		local.refresh(result, miss);
		HashMap<String, String> nr = new HashMap<String, String>();
		List<String> nm = local.checkCache(Arrays.asList("test"), nr);
		System.out.println(nr);
	}
}