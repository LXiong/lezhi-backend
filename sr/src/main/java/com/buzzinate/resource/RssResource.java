package com.buzzinate.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.buzzinate.common.geo.GeoInfo;
import com.buzzinate.common.util.GeoUtil;

/**
 * RSS内容源
 * @author feeling
 *
 */
public class RssResource {
	
	private static List<String> rssResource = loadRssResource();
	
	public static List<String> getRssList(){
		return rssResource;
	}
	
	private static List<Entry<String, GeoInfo>> geoArticlesRssResource = loadGeoArticleRssResource();

	public static List<Entry<String, GeoInfo>> getGeoRssList() {
		return geoArticlesRssResource;
	}

	private static List<Entry<String, GeoInfo>> loadGeoArticleRssResource() {
		List<Entry<String, GeoInfo>> geoArticlesRssList = new ArrayList<Entry<String, GeoInfo>>();
		Map<String, GeoInfo> geoArticlesRssMap = new HashMap<String, GeoInfo>();
		BufferedReader br = null;
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("geo_article_rss");
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] splits = line.split(",");
				String url = splits[0];
				Double longitude = Double.valueOf(splits[1]);
				Double latitude = Double.valueOf(splits[2]);
				GeoInfo geoInfo = GeoUtil.getNearestGeoInfo(longitude, latitude);
				geoArticlesRssMap.put(url, geoInfo);
			}
			geoArticlesRssList.addAll(geoArticlesRssMap.entrySet());
			br.close();
			return geoArticlesRssList;
		} catch (IOException e) {
			e.printStackTrace();
			return geoArticlesRssList;
		} 
	}

	private static List<String> loadRssResource() {
		List<String> rssList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("rss.txt");
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				rssList.add(line);
			}
			return rssList;
		} catch (IOException e) {
			e.printStackTrace();
			return rssList;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
			}
		}
	}
}
