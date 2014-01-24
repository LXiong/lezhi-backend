package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Embedded;

@Embedded
public class KeywordFeature {
	private String keyword;
	private int clusterId;
	private String algo;
	private double resource;

	public KeywordFeature() {}
	
	public KeywordFeature(String keyword, int clusterId, String algo, double resource) {
		this.keyword = keyword;
		this.clusterId = clusterId;
		this.algo = algo;
		this.resource = resource;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getClusterId() {
		return clusterId;
	}
	
	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}
	
	public String getAlgo() {
		return algo;
	}
	
	public void setAlgo(String algo) {
		this.algo = algo;
	}
	
	public double getResource() {
		return resource;
	}
	
	public void setResource(double resource) {
		this.resource = resource;
	}
}