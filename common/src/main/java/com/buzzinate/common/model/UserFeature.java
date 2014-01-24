package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Embedded;

@Embedded
public class UserFeature {
	private long userId;
	private int clusterId;
	private String algo;
	private double resource;
	private int prefSize;

	public UserFeature() {}
	
	public UserFeature(long userId, int clusterId, String algo, double resource, int prefSize) {
		this.userId = userId;
		this.clusterId = clusterId;
		this.algo = algo;
		this.resource = resource;
		this.prefSize = prefSize;
	}
	
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
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
	
	public int getPrefSize() {
		return prefSize;
	}

	public void setPrefSize(int prefSize) {
		this.prefSize = prefSize;
	}
}