package com.buzzinate.common.util;

import java.util.Comparator;

import com.buzzinate.common.geo.GeoInfo;

public class LatitudeComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		GeoInfo info0 = (GeoInfo) arg0;
		GeoInfo info1 = (GeoInfo) arg1;

		if (info0.getLatitude() > info1.getLatitude()) {
			return 1;
		} else if (info0.getLatitude() < info1.getLatitude()) {
			return -1;
		} else {
			return 0;
		}
	}
}