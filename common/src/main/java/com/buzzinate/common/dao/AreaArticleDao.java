package com.buzzinate.common.dao;

import java.util.ArrayList;
import java.util.List;

import com.buzzinate.common.model.AreaArticle;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.util.StringUtil;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class AreaArticleDao extends BaseDaoDefault<AreaArticle, Long>{
	
	public AreaArticleDao(Datastore ds) {
		super(ds);
	}
	
	public List<Long> findProvinceItems(String provinceName, int count, int page){
		byte[] provinceHash = StringUtil.hash(provinceName);
		List<AreaArticle> areaArticles = createQuery().filter("provinceHash", provinceHash).order("-pageId").offset(count * (page-1)).limit(count).asList();
		List<Long> pageIds = new ArrayList<Long>();
		for (AreaArticle areaArticle: areaArticles) {
			pageIds.add(areaArticle.getPageId());
		}
		return pageIds;
	}

	public List<Long> findCityItems(String cityName, int count, int page){
		byte[] cityHash = StringUtil.hash(cityName);
		List<AreaArticle> areaArticles = createQuery().filter("cityHash", cityHash).order("-pageId").offset(count * (page-1)).limit(count).asList();
		List<Long> pageIds = new ArrayList<Long>();
		for (AreaArticle areaArticle: areaArticles) {
			pageIds.add(areaArticle.getPageId());
		}
		return pageIds;
	}
	
	public List<Long> findDistrictItems(String districtName, int count, int page){
		byte[] districtHash = StringUtil.hash(districtName);
		List<AreaArticle> areaArticles = createQuery().filter("districtHash", districtHash).order("-pageId").offset(count * (page-1)).limit(count).asList();
		List<Long> pageIds = new ArrayList<Long>();
		for (AreaArticle areaArticle: areaArticles) {
			pageIds.add(areaArticle.getPageId());
		}
		return pageIds;
	}
	
	public void updateCategory(long id, Category cat) {
		Query<AreaArticle> q = createQuery().filter("id", id);
		UpdateOperations<AreaArticle> uo = createUpdateOperations().set("category", cat);
		update(q, uo);
	}
	
	public Boolean isExistInArea(Long pageId){
		List<AreaArticle> trendArticles = createQuery().filter("pageId", pageId).asList();
		return trendArticles.size() > 0 ? true : false;
	}
}

