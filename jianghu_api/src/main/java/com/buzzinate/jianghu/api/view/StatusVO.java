package com.buzzinate.jianghu.api.view;

import java.util.ArrayList;
import java.util.List;

import com.buzzinate.jianghu.model.Status;

public class StatusVO {
	public long createdAt;
	public String text;
	public long id;
	public int feature;
	public long sourceId;
	
	public StatusVO(Status status) {
		this.createdAt = status.getCreateAt();
		this.text = status.getText();
		this.id = status.getId();
		this.feature = status.getFeature();
		this.sourceId = status.getSourceId();
	}
	
	public static List<StatusVO> make(List<Status> statuses) {
		List<StatusVO> result = new ArrayList<StatusVO>();
		for (Status s: statuses) result.add(new StatusVO(s));
		return result;
	}
}
