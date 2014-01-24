package com.buzzinate.common.sr;

public class Neighborhood {
	private long userId;
	private double similarity;
	
	public Neighborhood(long userId, double similarity) {
		this.userId = userId;
		this.similarity = similarity;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public double getSimilarity() {
		return similarity;
	}
}
