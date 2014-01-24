package com.buzzinate.weibo;

import weibo4j2.http.Response;
import weibo4j2.model.WeiboException;
import weibo4j2.org.json.JSONArray;
import weibo4j2.org.json.JSONException;
import weibo4j2.org.json.JSONObject;

public class Location implements java.io.Serializable {
	private static final long serialVersionUID = 9076424494907778161L;

	private String longitude;
	private String latitude;
	private String city;
	private String province;
	private String city_name;
	private String province_name;
	private String address;
	private String pinyin;
	private String more;
	
	private String district_name;
	private String name;
	private String distance;
	private String direction;

	public Location(JSONObject json) throws WeiboException, JSONException {
		longitude = json.getString("longitude");
		latitude = json.getString("latitude");
		city = json.getString("city");
		province = json.getString("province");
		city_name = json.getString("city_name");
		province_name = json.getString("province_name");
		address = json.getString("address");
		pinyin = json.getString("pinyin");
		more = json.getString("more");
		district_name = json.getString("district_name");
		name = json.getString("name");
		distance = json.getString("distance");
		direction = json.getString("direction");
	}
	
	public static Location constructCounts(Response res) throws WeiboException {
		try {
			JSONArray list = (JSONArray) res.asJSONObject().get("geos");
			return new Location(list.getJSONObject(0));

		} catch (JSONException jsone) {
			throw new WeiboException(jsone);
		} catch (WeiboException te) {
			throw te;
		}
	}

	public String getDistrict_name() {
		return district_name;
	}

	public void setDistrict_name(String district_name) {
		this.district_name = district_name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity_name() {
		return city_name;
	}

	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public String getProvince_name() {
		return province_name;
	}

	public void setProvince_name(String province_name) {
		this.province_name = province_name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getMore() {
		return more;
	}

	public void setMore(String more) {
		this.more = more;
	}

	@Override
	public String toString() {
		return "Location{ longitude=" + longitude + ", latitude=" + latitude + ", city=" + city + ", province=" + province + ", city_name=" + city_name + ", province_name=" + province_name
				+ ", address=" + address + ", pinyin=" + pinyin + ", more=" + more + '}';
	}
}
