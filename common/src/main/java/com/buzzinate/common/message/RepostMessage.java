package com.buzzinate.common.message;

import java.util.List;

public class RepostMessage {
	
	public String statusId;
	public Integer repostCount;
	public List<String> urls;
	
	public RepostMessage() { }
	
	public RepostMessage(String statusId, Integer repostCount, List<String> urls){
		this.statusId = statusId;
		this.repostCount = repostCount;	
		this.urls = urls;
	}
	
	public int hashCode() {
		StringBuilder sb = new StringBuilder();
		sb.append(statusId);
		for(String url : urls){
			sb.append(url);
		}
		return sb.toString().hashCode();
	}
}
