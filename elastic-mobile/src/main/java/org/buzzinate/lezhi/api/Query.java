package org.buzzinate.lezhi.api;

public class Query {
	public String signature;
	public String keyword;
	public long reftime;
	public int maxword;
	
	public Query() {}
	
	public Query(String signature, String keyword, long reftime, int maxword) {
		this.signature = signature;
		this.keyword = keyword;
		this.reftime = reftime;
		this.maxword = maxword;
	}
}