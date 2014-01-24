package com.buzzinate.crawl.core.extract;

public class NodeStat {
	public int puncNum = 0;
	public int totalPuncNum = 0;
	public int emptyNodeNum = 0;
	public int linkWordNum = 0;
	public int wordNum = 0;
	
	public void substract(NodeStat sub) {
		totalPuncNum -= sub.totalPuncNum;
		emptyNodeNum -= sub.emptyNodeNum;
		linkWordNum -= sub.linkWordNum;
		wordNum -= sub.wordNum;
	}
}
