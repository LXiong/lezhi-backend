package com.buzzinate.jianghu.dao;

import java.util.List;

import com.buzzinate.common.model.Trend;


public interface TrendDao {
	public List<Trend> findPop(int count, int page);
	
	public Trend findByHash(byte[] hash);
}
