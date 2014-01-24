package com.buzzinate.common.util;

import java.util.Comparator;

import com.buzzinate.common.geo.GeoInfo;

public class LongitudeComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		GeoInfo info0 = (GeoInfo) arg0;
		GeoInfo info1 = (GeoInfo) arg1;

		// 首先比较年龄，如果年龄相同，则比较名字
		if (info0.getLongitude() > info1.getLongitude()) {
			return 1;
		} else if (info0.getLongitude() < info1.getLongitude()) {
			return -1;
		} else {
			return 0;
		}
	}
}
