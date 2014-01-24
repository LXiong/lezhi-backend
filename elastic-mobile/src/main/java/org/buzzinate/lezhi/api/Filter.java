package org.buzzinate.lezhi.api;

public class Filter {
	public long mintime;
//	public boolean _cache = false;
	
	public Filter() {}
	
	public Filter(long mintime) {
		this.mintime = mintime;
	}
}