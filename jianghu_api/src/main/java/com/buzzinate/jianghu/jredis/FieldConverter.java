package com.buzzinate.jianghu.jredis;

import java.util.Map;

public interface FieldConverter<T> {
	Map<String, String> toMap(T value);
	T fromMap(Map<String, String> m);
}