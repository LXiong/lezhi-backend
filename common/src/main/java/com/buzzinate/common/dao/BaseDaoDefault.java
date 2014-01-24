package com.buzzinate.common.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buzzinate.common.util.IdGenerator;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.mapping.Mapper;
import com.google.code.morphia.query.MorphiaIterator;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryImpl;
import com.mongodb.WriteConcern;

/**
 * BaseDao的默认实现
 * 
 * @author Brad Luo
 *
 * @param <T>
 * @param <K>
 */
public class BaseDaoDefault<T, K> extends BasicDAO<T, K> implements BaseDao<T, K> {
	public BaseDaoDefault(Class<T> entityClass, Datastore ds) {
		super(entityClass, ds);
	}

	protected BaseDaoDefault(Datastore ds) {
		super(ds);
	}
	
	/**
	 * create query from the primary node only, to make sure no sync delay
	 * 
	 */
	public Query<T> createPrimaryQuery() {
		return super.createQuery().queryPrimaryOnly();
	}

	@Override
	public Query<T> createQuery() {
		return super.createQuery().queryNonPrimary();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<K> getIds(Query<T> q) {
		List<Key<T>> keys = q.asKeyList();
		List<K> ids = new ArrayList<K>();
		for (Key<T> key: keys) {
			ids.add((K) key.getId());
		}
		return ids;
	}
	
	@Override
	public T get(K id) {
		Query<T> query = ds.find(ds.getCollection(getEntityClass()).getName(), getEntityClass());
		query.disableValidation();
		query.offset(0);
		query.limit(1);
		return query.filter(Mapper.ID_KEY, id).enableValidation().get();
	}
	
	@Override
	public T getPrimary(K id) {
		Query<T> query = ds.find(ds.getCollection(getEntityClass()).getName(), getEntityClass());
		query.queryPrimaryOnly();
		query.disableValidation();
		query.offset(0);
		query.limit(1);
		return query.filter(Mapper.ID_KEY, id).enableValidation().get();
	}

	@Override
	public List<T> get(List<K> ids) {
		if (ids.isEmpty()) return new ArrayList<T>();
		List<T> es = createQuery().filter(Mapper.ID_KEY + " in", ids).asList();
		Field idField = ds.getMapper().getMappedClass(getEntityClass()).getIdField();
		try {
			HashMap<K, T> id2entry = new HashMap<K, T>();
			for (T e: es) {
				id2entry.put((K)idField.get(e), e);
			}
			List<T> entries = new ArrayList<T>();
			for (int i = 0; i < ids.size(); i++) {
				entries.add(id2entry.get(ids.get(i)));
			}
			return entries;
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<T> get(List<K> ids, String...fields) {
		if (ids.isEmpty()) return new ArrayList<T>();
		return createQuery().retrievedFields(true, fields).filter(Mapper.ID_KEY + " in", ids).asList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> List<R> distinct(Query<T> q, String field) {
		// 存在一定风险，暂时方案
		QueryImpl<T> qi = (QueryImpl<T>) q;
		return getCollection().distinct(field, qi.getQueryObject());
	}
	
	/**
	 * 保存POJO
	 *   如果ID是Long或者long，值是null或者0，则系统会自动生成一个ID
	 *   如果定义了唯一索引，则此次操作被忽略
	 */
	@Override
	public Key<T> save(T entity) {
		try {
			Field idField = ds.getMapper().getMappedClass(entity).getIdField();
			if (idField.getType() == Long.class || idField.getType() == long.class) {
				if (idField.get(entity) == null || idField.get(entity).equals(0L)) { 
					idField.set(entity, IdGenerator.generateLongId(ds, getEntityClass()));
				}
			}
			return super.save(entity);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 保存POJO
	 *   如果ID是Long或者long，值是null或者0，则系统会自动生成一个ID
	 *   如果定义了唯一索引，则此次操作被忽略
	 */
	@Override
	public Key<T> save(T entity, WriteConcern wc) {
		try {
			Field idField = ds.getMapper().getMappedClass(entity).getIdField();
			if (idField.getType() == Long.class || idField.getType() == long.class) {
				if (idField.get(entity) == null || idField.get(entity).equals(0L)) {
					idField.set(entity, IdGenerator.generateLongId(ds, getEntityClass()));
				}
			}
			return super.save(entity, wc);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void filterByPage(Query<T> query, Page page) {
		query.order("-" + Mapper.ID_KEY);
		if (page.getSinceId() > -1) query.filter(Mapper.ID_KEY + " >", page.getSinceId());
		if (page.getMaxId() > -1) query.filter(Mapper.ID_KEY + " <=", page.getMaxId());
		if (page.getCount() > -1 && page.getPage() > -1) {
			query.offset(page.getCount() * (page.getPage() - 1));
			query.limit(page.getCount());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> asList(Query<T> query) {
		List<T> results = new ArrayList<T>();
		MorphiaIterator<T,T> iter = (MorphiaIterator<T, T>) query.fetch().iterator();
		while (true) {
			try {
				if (!iter.hasNext()) break;
				results.add(iter.next());
			} catch (Throwable t) {
				// ignore
			}
		}
		return results;
	}

	@Override
	public K findMaxId() {
		List<K> keys = getIds(createQuery().order("-" + Mapper.ID_KEY).limit(1));
		if (!keys.isEmpty()) return keys.get(0);
		return null;
	}

	@Override
	public <V> Map<K, V> mapField(List<K> ids, String fieldName) {
		HashMap<K, V> result = new HashMap<K, V>();
		if (ids.isEmpty()) return result;
		
		Field idField = null;
		Field field = null;
		List<T> rs = createQuery().retrievedFields(true, fieldName).filter(Mapper.ID_KEY + " in", ids).asList();
		for (T r: rs) {
			if (idField == null) {
				idField = ds.getMapper().getMappedClass(r).getIdField();
			}
			if (field == null) {
				field = ds.getMapper().getMappedClass(r).getMappedFieldByJavaField(fieldName).getField();
			}
			try {
				result.put((K)(idField.get(r)), (V)(field.get(r)));
			} catch (IllegalArgumentException e) {
				// Igonre
			} catch (IllegalAccessException e) {
				// Igonre
			}
		}
		return result;
	}

	@Override
	public Map<K, T> map2(List<K> ids, String... fields) {
		HashMap<K, T> result = new HashMap<K, T>();
		if (ids.isEmpty()) return result;
		
		Field idField = null;
		List<T> rs = createQuery().retrievedFields(true, fields).filter(Mapper.ID_KEY + " in", ids).asList();
		for (T r: rs) {
			if (idField == null) {
				idField = ds.getMapper().getMappedClass(r).getIdField();
			}
			try {
				result.put((K)(idField.get(r)), r);
			} catch (IllegalArgumentException e) {
				// Igonre
			} catch (IllegalAccessException e) {
				// Igonre
			}
		}
		return result;
	}
}