package com.buzzinate.jianghu.config;

import com.buzzinate.jianghu.sr.cache.JCache;
import com.google.common.base.Function;
import com.google.inject.Guice;
import com.google.inject.Injector;

class TestItem {
	public String id;
	public String data;
}

public class TestJCache {
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new JianghuModule());
		JCache cache = injector.getInstance(JCache.class);
		TestItem r = cache.sync("test-key-", "id", TestItem.class, 1000 * 20, new Function<String, TestItem>(){
			@Override
			public TestItem apply(String id) {
				TestItem ti = new TestItem();
				ti.id = id;
				ti.data = "test data";
				return ti;
			}
		});
		System.out.println(r.id + ": " + r.data);
	}
}