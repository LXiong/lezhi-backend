package com.buzzinate.jianghu.api.view;

public class CountVO {
	public boolean statusNew;
	public long menNewCount;
	public boolean recNew;
	
	public CountVO(boolean statusNew, long menNewCount, boolean recNew) {
		this.statusNew = statusNew;
		this.menNewCount = menNewCount;
		this.recNew = recNew;
	}
}
