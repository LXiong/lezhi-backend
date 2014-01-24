package com.buzzinate.model;

import org.apache.commons.lang.StringUtils;

import com.buzzinate.crawl.core.util.DomainNames;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity(value="rawArticle", noClassnameStored=true)
public class RawArticle {
	// reverse host # depth, such as cn.com.sina.tech#2#1
	@Id public String id;
	
	public String url;
	public String title;
	public String content;
	
	public RawArticle() {
		
	}
	
	public RawArticle(String url, String title, String content) {
		this.id = makeId(url);
		this.url = url;
		this.title = title;
		this.content = content;
	}
	
	public static String makeId(String url) {
		String host = DomainNames.safeGetPLD(url);
		host = reverse(host);
		if (url.endsWith("/")) url = url.substring(0, url.length()-1);
		return host + "#" + (StringUtils.countMatches(url, "/") - 2) + "#" + Math.abs(url.hashCode() % 10);
	}

	public static String reverse(String host) {
		String[] parts = StringUtils.split(host, ".");
		StringBuffer sb = new StringBuffer();
		for (int i = parts.length-1; i >= 0; i--) {
			sb.append(parts[i]);
			if (i > 0) sb.append(".");
		}
		return sb.toString();
	}
}