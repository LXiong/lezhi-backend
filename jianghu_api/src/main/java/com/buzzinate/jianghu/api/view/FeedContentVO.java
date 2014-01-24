package com.buzzinate.jianghu.api.view;

import com.buzzinate.jianghu.model.Status;

public class FeedContentVO {
	public String text;
	public long fid;
	
	public FeedContentVO(Status status) {
		this.text = status.getText();
		this.fid = status.getId();
	}
}
