package com.buzzinate.nlp.util;

public class MatrixUtils {
	private MatrixUtils() {}
	
    public static void normalize(double[] vs) {
    	double sum = 0;
    	for (double v: vs) sum += v * v;
    	double m = Math.sqrt(sum);
    	for (int i = 0; i < vs.length; i++) vs[i] = vs[i] / m;
    }
    
	public static double cosine(double[] x, double[] y) {
		double sumxy = 0;
		double sumxx = 0;
		double sumyy = 0;
		for (int i = 0; i < x.length; i++) {
			sumxy += x[i] * y[i];
			sumxx += x[i] * x[i];
			sumyy += y[i] * y[i];
		}
		if (sumxx <= 0 || sumyy <= 0) return 0;
		return sumxy / Math.sqrt(sumxx * sumyy);
	}
}