package com.buzzinate.crawl.core.util;

import java.util.HashMap;
import java.util.Map.Entry;

public class MultiCharsetDetector {

	private static final HashMap<String, String> charset2super = new HashMap<String, String>();
	private static final HashMap<String, Integer> charset2p = new HashMap<String, Integer>(); 
																						
	static {
		charset2super.put("gb2312", "gb18030");
		charset2super.put("gbk", "gb18030");

		charset2p.put("gb18030", 2);
		charset2p.put("utf-8", 2);
		charset2p.put("gbk", 1);
		charset2p.put("gb2312", 1);
	}

	private HashMap<String, Double> charsetCnt = new HashMap<String, Double>();
	
	public void addCharset(String charset, Double weight){
		if(charset != null){
			if(charsetCnt.containsKey(charset)){
				charsetCnt.put(charset, charsetCnt.get(charset) + weight);
			}else {
				charsetCnt.put(charset, weight);
			}
			String subCharset = charset2super.get(charset.toLowerCase());
			if(null != subCharset){
				if(charsetCnt.containsKey(subCharset)){
					charsetCnt.put(subCharset, charsetCnt.get(subCharset) + weight);
				}else {
					charsetCnt.put(subCharset, weight);
				}
			}
		}
	}
	
	public void addCharset(String charset){
		addCharset(charset, 1.0);
	}
	
	public String getCharset(){
		Double max = 0.0;
		String finalCharset = "";
		
		for(Entry<String, Double> e : charsetCnt.entrySet()){
			String charset = e.getKey();
			Double cnt = e.getValue();
			Double score = cnt * 5 + (charset2p.containsKey(charset) ? charset2p.get(charset) : 0);
			if (score > max) {
		        max = score;
		        finalCharset = charset;
		      }
		}		
		return finalCharset;
	}
	
	
	public static void main(String[] args) {
		Double s = 3.0;
	}

}
