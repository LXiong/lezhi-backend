package com.buzzinate.jianghu.dao;

import java.util.List;

public interface AreaArticleDao {
	public List<Long> findProvinceItems(String provinceName, int count, int page, int cid);
	public List<Long> findCityItems(String cityName, int count, int page, int cid);
	public List<Long> findDistrictItems(String districtName, int count, int page, int cid);
}
