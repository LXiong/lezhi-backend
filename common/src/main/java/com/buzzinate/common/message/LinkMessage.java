package com.buzzinate.common.message;

import java.util.ArrayList;
import java.util.List;

import com.buzzinate.common.geo.GeoInfo;

public class LinkMessage {
	public String sinaUrl;
	public String realUrl;
	public GeoInfo geoInfo;
	public long pubTime = Long.MAX_VALUE;
	public List<LinkDetail> details = new ArrayList<LinkDetail>();
	
	public LinkMessage() {}
	
	public LinkMessage(String realUrl) {
		this.sinaUrl = "";
		this.realUrl = realUrl;
	}
	
	public LinkMessage(String realUrl, long pubTime) {
		this.sinaUrl = "";
		this.realUrl = realUrl;
		this.pubTime = pubTime;
	}
	
	public LinkMessage(String realUrl, GeoInfo geoInfo) {
		this.sinaUrl = "";
		this.realUrl = realUrl;
		this.geoInfo = geoInfo;
	}
	
	public LinkMessage(String sinaUrl, String realUrl, List<LinkDetail> details) {
		this.sinaUrl = sinaUrl;
		this.realUrl = realUrl;
		this.details = details;
	}
	
	public long getMinTime(long base) {
		long min = base;
		if(pubTime < min){
			min = pubTime;
		}
		for (LinkDetail detail: details) {
			if (detail.tweetTime < min) min = detail.tweetTime;
		}
		long now = System.currentTimeMillis();
		// if the min time is bigger than now , it is wrong , use currentTime instead
		if(System.currentTimeMillis() < min){
			min = now;
		}
		return min;
	}
}