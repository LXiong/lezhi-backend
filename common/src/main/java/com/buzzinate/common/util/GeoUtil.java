package com.buzzinate.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.buzzinate.common.geo.GeoInfo;

public class GeoUtil {
	
	public static final HashSet<String> MUNICIPALITIES = new HashSet<String>(Arrays.asList("北京","天津","上海","重庆"));

	private static final Double DEFAULT_SEARCH_RADIUS = 0.11;
	
	//我们的网格划分步长是0.1，那么当一个query 坐标距离它最近的网格点的长度都大于如下值时，则说明这个query坐标不在国内
	private static final Double MIN_DISTANCE_THRESHOLD = Math.sqrt(0.02);
	
	private static final String GEOINFO_FILE = "geoinfo.txt";
	
	private static final String ADMIN_AREA_FILE = "admin_area.txt";

	public static List<GeoInfo> geoInfos = loadGeoInfos(GEOINFO_FILE);
	
	public static List<GeoInfo> adminAreaInfos = loadGeoInfos(ADMIN_AREA_FILE);

	// sorted with longitude, from west to east
	public static List<GeoInfo> lonGeoInfos = loadLonGeoInfos();

	// sorted with latitude, from south to north
	public static List<GeoInfo> latGeoInfos = loadLatGeoInfos();

	private static final Integer GEOS_SIZE = geoInfos.size();

	private static List<GeoInfo> loadLonGeoInfos() {
		List<GeoInfo> lonGeoInfos = new ArrayList<GeoInfo>();
		lonGeoInfos.addAll(geoInfos);
		Collections.sort(lonGeoInfos, new LongitudeComparator());
		return lonGeoInfos;
	}

	private static List<GeoInfo> loadLatGeoInfos() {
		List<GeoInfo> latGeoInfos = new ArrayList<GeoInfo>();
		latGeoInfos.addAll(geoInfos);
		Collections.sort(latGeoInfos, new LatitudeComparator());
		return latGeoInfos;
	}

	private static List<GeoInfo> loadGeoInfos(String sourceFile) {
		List<GeoInfo> geoInfos = new ArrayList<GeoInfo>();
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(sourceFile);

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			Integer counter = 0;
			while ((line = br.readLine()) != null) {
				String[] splits = line.split("[\\s,\\s+]");
				Integer id = counter.intValue();
				String province = splits[0];
				String city = splits[1];
				String district = splits[2];
				Double longitude = Double.valueOf(splits[3]);
				Double latitude = Double.valueOf(splits[4]);
				geoInfos.add(new GeoInfo(id, province, city, district, longitude, latitude));
				counter++;
			}
			br.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return geoInfos;
	}

	public static GeoInfo getNearestGeoInfo(Double longitude, Double latitude) {
		
		if(longitude < 0.0 || latitude < 0.0){
			return new GeoInfo(-1, "", "", "", longitude, latitude);
		}

		List<GeoInfo> centLatGeoInfos = getCentreLatGeoInfo(latitude, DEFAULT_SEARCH_RADIUS);
		List<GeoInfo> centLonGeoInfos = getCentreLonGeoInfo(longitude, DEFAULT_SEARCH_RADIUS);

		Set<Integer> centLatIdSet = new HashSet<Integer>();
		Set<Integer> centLonIdSet = new HashSet<Integer>();
		Set<Integer> intersectIdSet = new HashSet<Integer>();

		for (GeoInfo geo : centLatGeoInfos) {
			centLatIdSet.add(geo.getId());
		}

		for (GeoInfo geo : centLonGeoInfos) {
			centLonIdSet.add(geo.getId());
		}

		for (Integer id : centLatIdSet) {
			if (centLonIdSet.contains(id)) {
				intersectIdSet.add(id);
			}
		}
		
		GeoInfo nearestGeo = null;
		Double minDistance = Double.MAX_VALUE;
		for (Integer id : intersectIdSet) {
			GeoInfo curGeoInfo = geoInfos.get(id);
			Double curDistance = Math.pow(longitude - curGeoInfo.getLongitude(), 2) + Math.pow(latitude - curGeoInfo.getLatitude(), 2);
			if (curDistance < minDistance){
				nearestGeo = curGeoInfo;
				minDistance = curDistance;
			}	
		}
		
		if(minDistance > MIN_DISTANCE_THRESHOLD){
			// 说明这个坐标对应的地址不在中国境内
			return new GeoInfo(-1, "", "", "", longitude, latitude);
		}
		return nearestGeo;
	}

	public static List<GeoInfo> getCentreLonGeoInfo(Double centLongtitude, Double radius) {

		Double westLine = centLongtitude - radius;
		Double eastLine = centLongtitude + radius;

		int startPos = 0;
		int endPos = GEOS_SIZE;

		int start = 0;
		int end = GEOS_SIZE - 1;

		while (end - start > 1) {
			int mid = start + (end - start) / 2;
			if (lonGeoInfos.get(mid).getLongitude() > westLine) {
				end = mid;
			} else if (lonGeoInfos.get(mid).getLongitude() < westLine) {
				start = mid;
			} else {
				startPos = mid;
				break;
			}
		}
		if (end - start == 1) {
			startPos = start;
		}

		start = 0;
		end = GEOS_SIZE - 1;

		while (end - start > 1) {
			int mid = start + (end - start) / 2;
			if (lonGeoInfos.get(mid).getLongitude() > eastLine) {
				end = mid;
			} else if (lonGeoInfos.get(mid).getLongitude() < eastLine) {
				start = mid;
			} else {
				endPos = mid;
				break;
			}
		}
		if (end - start == 1) {
			endPos = end;
		}

		return lonGeoInfos.subList(startPos, endPos);

	}

	public static List<GeoInfo> getCentreLatGeoInfo(Double centLatitude, Double radius) {

		Double southLine = centLatitude - radius;
		Double northLine = centLatitude + radius;

		int startPos = 0;
		int endPos = GEOS_SIZE;

		int start = 0;
		int end = GEOS_SIZE - 1;

		while (end - start > 1) {
			int mid = start + (end - start) / 2;
			if (latGeoInfos.get(mid).getLatitude() > southLine) {
				end = mid;
			} else if (latGeoInfos.get(mid).getLatitude() < southLine) {
				start = mid;
			} else {
				startPos = mid;
				break;
			}
		}
		if (end - start == 1) {
			startPos = start;
		}

		start = 0;
		end = GEOS_SIZE - 1;

		while (end - start > 1) {
			int mid = start + (end - start) / 2;
			if (latGeoInfos.get(mid).getLatitude() > northLine) {
				end = mid;
			} else if (latGeoInfos.get(mid).getLatitude() < northLine) {
				start = mid;
			} else {
				endPos = mid;
				break;
			}
		}
		if (end - start == 1) {
			endPos = end;
		}

		return latGeoInfos.subList(startPos, endPos);

	}
	
	
	

	public static void main(String[] args) {
		GeoInfo res = getNearestGeoInfo(116.4,39.9);
		System.out.println(res.getProvince().length());

	}

}
