package com.buzzinate.common.model;

/**
 * 文章抓取，正文抽取，分类等的结果状态码
 * 
 * @author Brad Luo
 *
 */
public enum Status {
	UNKNOWN_ERROR(-1), OK(0), CRAWL_ERROR(1), EXTRACT_ERROR(2), MARK_SPAM(3), DUPLICATE(4), POLITICAL(5);
	
	private int code;
	
	private Status(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	public static Status getCategory(int code) {
		switch (code) {
		case -1:
			return UNKNOWN_ERROR;
		case 0:
			return OK;
		case 1:
			return CRAWL_ERROR;
		case 2:
			return EXTRACT_ERROR;
		case 3:
			return MARK_SPAM;
		case 4:
			return DUPLICATE;
		case 5:
			return POLITICAL;
		default:
			return OK;
		}
	}
}
