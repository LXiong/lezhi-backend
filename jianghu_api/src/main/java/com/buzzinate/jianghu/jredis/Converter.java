package com.buzzinate.jianghu.jredis;

public interface Converter<T> {
	String toString(T value);
	T fromString(String str);
}