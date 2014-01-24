package com.buzzinate.common.dao;

import java.util.List;
import java.util.Map;

import com.buzzinate.common.model.Article;
import com.google.code.morphia.dao.DAO;
import com.google.code.morphia.query.Query;

/**
 * DAO基类，包含一些常用的，根据IDs得到实体，distinct结果集等
 * 
 * @author Brad Luo
 *
 * @param <T>
 * @param <K>
 */
public interface BaseDao<T, K> extends DAO<T, K> {
	public List<K> getIds(Query<T> q);
	public T getPrimary(K id);
	public List<T> get(List<K> ids);
	public List<T> get(List<K> ids, String...fields);
	public <V> Map<K, V> mapField(List<K> ids, String field);
	public Map<K, T> map2(List<K> ids, String... fields);
	public <R> List<R> distinct(Query<T> q, String field);
	public K findMaxId();
}
