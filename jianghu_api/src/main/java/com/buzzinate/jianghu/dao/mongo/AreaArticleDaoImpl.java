package com.buzzinate.jianghu.dao.mongo;

import java.util.ArrayList;
import java.util.List;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.model.AreaArticle;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.util.StringUtil;
import com.buzzinate.jianghu.dao.AreaArticleDao;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;

public class AreaArticleDaoImpl extends BaseDaoDefault<AreaArticle, Long>
		implements AreaArticleDao {

	@Inject
	public AreaArticleDaoImpl(Datastore ds) {
		super(ds);
	}

	public List<Long> findProvinceItems(String provinceName, int count, int page, int cid){
		byte[] provinceHash = StringUtil.hash(provinceName);
		List<AreaArticle> areaArticles = new ArrayList<AreaArticle>();
		if(cid > 0){
			areaArticles = createQuery().filter("provinceHash", provinceHash).filter("category", Category.getCategory(cid)).order("-pageId").offset(count * (page-1)).limit(count).asList();
		} else {
			areaArticles = createQuery().filter("provinceHash", provinceHash).order("-pageId").offset(count * (page-1)).limit(count).asList();
		}
		
		List<Long> pageIds = new ArrayList<Long>();
		for (AreaArticle areaArticle: areaArticles) {
			pageIds.add(areaArticle.getPageId());
		}
		return pageIds;
	}

	public List<Long> findCityItems(String cityName, int count, int page, int cid){
		byte[] cityHash = StringUtil.hash(cityName);
		List<AreaArticle> areaArticles = new ArrayList<AreaArticle>();
		if(cid > 0){
			areaArticles = createQuery().filter("cityHash", cityHash).filter("category", Category.getCategory(cid)).order("-pageId").offset(count * (page-1)).limit(count).asList();
		} else {
			areaArticles = createQuery().filter("cityHash", cityHash).order("-pageId").offset(count * (page-1)).limit(count).asList();
		}
		List<Long> pageIds = new ArrayList<Long>();
		for (AreaArticle areaArticle: areaArticles) {
			pageIds.add(areaArticle.getPageId());
		}
		return pageIds;
	}
	
	public List<Long> findDistrictItems(String districtName, int count, int page, int cid){
		byte[] districtHash = StringUtil.hash(districtName);
		List<AreaArticle> areaArticles = new ArrayList<AreaArticle>();
		if(cid > 0){
			areaArticles = createQuery().filter("districtHash", districtHash).filter("category", Category.getCategory(cid)).order("-pageId").offset(count * (page-1)).limit(count).asList();
		} else {
			areaArticles = createQuery().filter("districtHash", districtHash).order("-pageId").offset(count * (page-1)).limit(count).asList();
		}
		List<Long> pageIds = new ArrayList<Long>();
		for (AreaArticle areaArticle: areaArticles) {
			pageIds.add(areaArticle.getPageId());
		}
		return pageIds;
	}
}
