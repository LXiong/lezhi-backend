package com.buzzinate.diffusion;

import java.util.List;

public class Cluster {
	private List<Long> ids;
	
	public Cluster(List<Long> ids) {
		this.ids = ids;
	}

	public List<Long> getIds() {
		return ids;
	}
}