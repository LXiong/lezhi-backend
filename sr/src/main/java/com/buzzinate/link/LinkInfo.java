package com.buzzinate.link;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkInfo {
	public String sinaUrl;
	public String realUrl;
	
	public LinkInfo(String sinaUrl) {
		this.sinaUrl = sinaUrl;
	}
	
	public LinkInfo(String sinaUrl, String realUrl) {
		this.sinaUrl = sinaUrl;
		this.realUrl = realUrl;
	}
	
	public static Map<String, String> toMap(List<LinkInfo> linkInfos) {
		Map<String, String> result = new HashMap<String, String>();
		for (LinkInfo li: linkInfos) result.put(li.sinaUrl, li.realUrl);
		return result;
	}
}
