package com.buzzinate.jianghu.sr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.ShardedJedisPool;

import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.jianghu.dao.ReadDao;

public class ItemFilter {
	private PreferenceDao prefDao;
	private ReadDao readDao;
	
	public ItemFilter(ShardedJedisPool pool, PreferenceDao prefDao, ReadDao readDao) {
		this.prefDao = prefDao;
		this.readDao = readDao;
	}
	
	public List<Long> filterOldItem(long userId, List<Long> itemIds) {
		List<Long> result = new ArrayList<Long>(itemIds);
		if (result.size() > 0) {
		    // user doesn't read
		    HashSet<Long> readIds = new HashSet<Long>(readDao.findReadIds(userId, result));
		    HashSet<Long> prefIds = new HashSet<Long>(prefDao.findPrefIds(userId, result));
		    result.removeAll(readIds);
		    result.removeAll(prefIds);
		}
		
	    return result;
	}
	
	public List<Long> filterPrefItem(long userId, List<Long> itemIds) {
		List<Long> result = new ArrayList<Long>(itemIds);
		
		if (result.size() > 0) {
		    // user doesn't read
		    HashSet<Long> readIds = new HashSet<Long>(readDao.findReadIds(userId, result));
		    HashSet<Long> prefIds = new HashSet<Long>(prefDao.findPrefIds(userId, result));
		    result.removeAll(readIds);
		    result.removeAll(prefIds);
		}
		
	    return result;
	}
	
	public List<Long> filterOldItem(long userId, List<Long> itemIds, Set<Long> prefIds) {
		List<Long> result = new ArrayList<Long>(itemIds);
		
		if (result.size() > 0) {
		    // user doesn't read
		    HashSet<Long> readIds = new HashSet<Long>(readDao.findReadIds(userId, result));
		    result.removeAll(readIds);
		}
	    result.removeAll(prefIds);
	    
	    return result;
	}
}