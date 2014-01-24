package com.buzzinate.jianghu.jredis;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;

public interface BatchFunction<K, V> extends Function<List<K>, Map<K, V>> {
}