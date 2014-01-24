package com.buzzinate.crawl.core.tpl;

import com.buzzinate.crawl.core.util.TextUtility;

public class NodeStat {
	public int nWords;
	public int nFreqWords;
	public int nLinkWords;
	public int nSiteLinks;
	public int nPuncs;
	
	public NodeStat() {
		
	}
	
	public NodeStat(int nWords, int nFreqWords, int nLinkWords, int nSiteLinks, int nPuncs) {
		this.nWords = nWords;
		this.nFreqWords = nFreqWords;
		this.nLinkWords = nLinkWords;
		this.nSiteLinks = nSiteLinks;
		this.nPuncs = nPuncs;
	}
	
	public static NodeStat from(String text) {
		int nWords = TextUtility.countNumWords(text);
		int nPuncs = TextUtility.countPuncs(text);
		
		return new NodeStat(nWords, 0, 0, 0, nPuncs);
	}
	
	public void merge(NodeStat ns) {
		this.nWords += ns.nWords;
		this.nFreqWords += ns.nFreqWords;
		this.nLinkWords += ns.nLinkWords;
		this.nSiteLinks += ns.nSiteLinks;
		this.nPuncs += ns.nPuncs;
	}
}