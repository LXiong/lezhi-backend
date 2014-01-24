package com.buzzinate.common.geo;

public class GeoInfo {

	private Integer id;
	private String province;
	private String city;
	private String district;
	private Double longitude;
	private Double latitude;

	public GeoInfo() {
	}

	public GeoInfo(Integer id, String province, String city, String district, Double longitude, Double latitude) {
		this.id = id;
		this.province = province;
		this.city = city;
		this.district = district;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	/**
	 * 如果两个geoInfo的省市县都相同，则认为它们相等
	 */
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof GeoInfo) {
			GeoInfo anotherGeoInfo = (GeoInfo) anObject;
			if (province.equals(anotherGeoInfo.getProvince()) && city.equals(anotherGeoInfo.getCity()) && district.equals(anotherGeoInfo.getDistrict())) {
				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		return (province + city + district).hashCode();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String county) {
		this.district = county;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public String toString() {
		return "id: " + id + "\n" + "province: " + province + "\n" + "city: " + city + "\n" + "district: " + district + "\n" + "longitude: " + longitude + "\n" + "latitude: " + latitude + "\n";
	}
}
