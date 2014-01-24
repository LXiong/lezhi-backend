package com.buzzinate.weibo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import weibo4j2.model.WeiboException;

public class GeoInfoGenerator {

	// 中国最东南西北位置的经纬度
	private static final Double WESTLINE = 75.0;
	private static final Double EASTLINE = 135.0;

	private static final Double SOUTHLINE = 18.0;
	private static final Double NORTHLINE = 53.0;

	private static final Double LONGITUDE_STEP = 0.1;
	private static final Double LATITUDE_STEP = 0.1;

	private static final String geoinfoDir = "/home/feeling/geoinfo.txt";
	private static final String geoLogDir = "/home/feeling/geo.log";

	public static void generateGeoInfo() throws WeiboException, IOException, InterruptedException {
		SinaWeiboClient client = new SinaWeiboClient();
		// WeiboExt weibo = client.getWeibo("2.00oqc7sB8B8pgB78a4d17ed322i_kB");
		WeiboExt weibo = client.getWeibo();

		File geoinfoFile = new File(geoinfoDir);
		BufferedWriter geoinfobw = new BufferedWriter(new FileWriter(geoinfoFile));

		Set<String> missCoordinates = new HashSet<String>();

		for (Double longitude = WESTLINE; longitude <= EASTLINE; longitude = Math.round((longitude + LONGITUDE_STEP) * 10) / 10.0) {
			for (Double latitude = SOUTHLINE; latitude <= NORTHLINE; latitude = Math.round((latitude + LATITUDE_STEP) * 10) / 10.0) {
				Location loc;
				try {
					loc = weibo.getLocation(longitude, latitude);
				} catch (Exception e) {
					missCoordinates.add(longitude + "," + latitude);
					System.out.println("miss => longitude : " + longitude + " latitude : " + latitude);
					continue;
				}
				String province = loc.getProvince_name();
				if (province.isEmpty()) {
					continue;
				}
				System.out.println(loc.getAddress());
				String city = loc.getCity_name();
				String district = loc.getDistrict_name();
				geoinfobw.append(province).append(" ").append(city).append(" ").append(district).append(" ").append(String.valueOf(longitude)).append(" ").append(String.valueOf(latitude))
						.append("\n");
			}
			System.out.println("longitude => " + longitude + " completed !");
			Thread.currentThread().sleep(1000 * 60l);
		}

		while (!missCoordinates.isEmpty()) {
			Integer counter = 0;
			for (String coordinate : missCoordinates) {
				counter++;
				if (counter % 300 == 0) {
					Thread.currentThread().sleep(1000 * 60l);
				}
				String[] splits = coordinate.split(",");
				Double longitude = Double.valueOf(splits[0]);
				Double latitude = Double.valueOf(splits[1]);
				Location loc;
				try {
					loc = weibo.getLocation(longitude, latitude);
				} catch (Exception e) {
					System.out.println("miss => longitude : " + longitude + " latitude : " + latitude);
					continue;
				}

				String province = loc.getProvince_name();
				if (province.isEmpty()) {
					missCoordinates.remove(coordinate);
					continue;
				}
				System.out.println(loc.getAddress());
				String city = loc.getCity_name();
				String district = loc.getDistrict_name();
				geoinfobw.append(province).append(" ").append(city).append(" ").append(district).append(" ").append(String.valueOf(longitude)).append(" ").append(String.valueOf(latitude))
						.append("\n");
				missCoordinates.remove(coordinate);
			}
			System.out.println("remained miss coordinate size => " + missCoordinates.size());
		}

		geoinfobw.close();

	}
	
	public static void completeMissResults() throws IOException, WeiboException {
		
		SinaWeiboClient client = new SinaWeiboClient();
		// WeiboExt weibo = client.getWeibo("2.00oqc7sB8B8pgB78a4d17ed322i_kB");
		WeiboExt weibo = client.getWeibo();
		
		File geoinfoFile = new File("/home/feeling/geoinfoMiss.txt");
		BufferedWriter geoinfobw = new BufferedWriter(new FileWriter(geoinfoFile));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(geoLogDir))));
		String line = br.readLine();
		Integer counter = 0;
		while ((line= br.readLine()) != null){
			counter++;
			if(counter % 500 == 0){
				System.out.println("complete => " + counter);
			}
			if(line.startsWith("miss")){
				String[] splits = line.split("\\s+");
				Double longitude = Double.valueOf(splits[4]);
				Double latitude = Double.valueOf(splits[7]);
				
				Location loc;
				try {
					loc = weibo.getLocation(longitude, latitude);
				} catch (Exception e) {
					//missCoordinates.add(longitude + "," + latitude);
					System.out.println("miss => longitude : " + longitude + " latitude : " + latitude);
					continue;
				}
				String province = loc.getProvince_name();
				if (province.isEmpty()) {
					continue;
				}
				System.out.println(loc.getAddress());
				String city = loc.getCity_name();
				String district = loc.getDistrict_name();
				geoinfobw.append(province).append(" ").append(city).append(" ").append(district).append(" ").append(String.valueOf(longitude)).append(" ").append(String.valueOf(latitude))
						.append("\n");
				
			}
		}
		
		geoinfobw.close();
	}
	

	public static void main(String[] args) throws WeiboException, IOException, InterruptedException {
		completeMissResults();
		//generateGeoInfo();
	}
}
