package com.buzzinate.crawl.core.util;

import java.util.List;


public class FreqTextDetector {
	private Counter<String> pageTextCnt = new Counter<String>();
	private Counter<String> siteTextCnt = new Counter<String>();

	public void add(String text) {
		pageTextCnt.add(text);
	}

	public void shift() {
		for (String text: pageTextCnt.toMap().keySet()) {
			siteTextCnt.add(text);
		}
		pageTextCnt = new Counter<String>();
	}

	public List<String> freqSiteText(int minFreq) {
		return siteTextCnt.freqItems(minFreq);
	}
	
	public List<String> freqPageText(int minFreq) {
		return pageTextCnt.freqItems(minFreq);
	}
}