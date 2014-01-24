package com.buzzinate.common.model;

public enum Category {
	NONE(0), TECHNOLOGY(1), SPORTS(2), CULTURE(3), NEWS(4), FINANCE(5), LIFE(6), ENTERTAINMENT(7), WOMEN(8);
	
	private int code;
	
	private Category(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	public static String getCnCategoryName(int code){
		switch (code) {
		case 0:
			return "综合";
		case 1:
			return "科技";
		case 2:
			return "体育";
		case 3:
			return "文化";
		case 4:
			return "新闻";
		case 5:
			return "财经";
		case 6:
			return "生活";
		case 7:
			return "娱乐";
		case 8: 
			return "女性";
		default:
			return "综合";	
		}
	}
	
	public static Category getCategory(int code) {
		switch (code) {
		case 0:
			return NONE;
		case 1:
			return TECHNOLOGY;
		case 2:
			return SPORTS;
		case 3:
			return CULTURE;
		case 4:
			return NEWS;
		case 5:
			return FINANCE;
		case 6:
			return LIFE;
		case 7:
			return ENTERTAINMENT;
		case 8:
			return WOMEN;
		default:
			return NONE;	
		}
	}
}
