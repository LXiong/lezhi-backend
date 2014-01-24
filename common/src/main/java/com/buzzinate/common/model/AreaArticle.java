package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

@Entity(value = "area", noClassnameStored = true)
public class AreaArticle {

	@Id
	private Long id;

	private String provinceName;
	
	private String cityName;
	
	private String districtName;

	@Indexed(background=true)
	private byte[] provinceHash;
	
	@Indexed(background=true)
	private byte[] cityHash;
	
	@Indexed(background=true)
	private byte[] districtHash;
	
	@Indexed(unique=true, dropDups=true)
	private long pageId;
	
	private Category category = Category.NONE;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getDistrictName() {
		return districtName;
	}

	public void setDistrictName(String districtName) {
		this.districtName = districtName;
	}

	public byte[] getProvinceHash() {
		return provinceHash;
	}

	public void setProvinceHash(byte[] provinceHash) {
		this.provinceHash = provinceHash;
	}

	public byte[] getCityHash() {
		return cityHash;
	}

	public void setCityHash(byte[] cityHash) {
		this.cityHash = cityHash;
	}

	public byte[] getDistrictHash() {
		return districtHash;
	}

	public void setDistrictHash(byte[] districtHash) {
		this.districtHash = districtHash;
	}

	public long getPageId() {
		return pageId;
	}

	public void setPageId(long pageId) {
		this.pageId = pageId;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
}