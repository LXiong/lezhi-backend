package com.buzzinate.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MentionUtil {
	private static final Pattern URL_PATTERN = Pattern.compile("http://(sinaurl|t).cn/(\\w+)");
	
	private static final int TEXT = 0;
	private static final int AT = 1;
	
	private MentionUtil() {
		
	}
	
	public static List<String> parseSlashMentions(String text) {
		List<String> mentions = new ArrayList<String>();
		List<String> allMentions = parseMentions(text);
		
		for(String m: allMentions) {
			if (text.contains("//@" + m)) mentions.add(m);
		}
		
		return mentions;
	}
	
	public static List<String> parseMentions(String text) {
		List<String> mentions = new ArrayList<String>();
		int state = TEXT;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (state == TEXT) {
				if (ch == '@') {
					state = AT;
					sb = new StringBuffer();
				}
			} else if (state == AT) {
				if (ch == '@') {
					mentions.add(sb.toString());
					sb = new StringBuffer();
				} else if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-') sb.append(ch);
				else {
					state = TEXT;
					mentions.add(sb.toString());
					sb = new StringBuffer();
				}
			}
		}
		if (state == AT) mentions.add(sb.toString());
		
		return mentions;
	}
	
	public static String removeMentions(String text) {
		int state = TEXT;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (state == TEXT) {
				if (ch == '@') {
					state = AT;
					sb.append(" ");
				} else sb.append(ch);
			} else if (state == AT) {
				if (ch != '@' && !Character.isLetterOrDigit(ch) && ch != '_' && ch != '-') {
					state = TEXT;
				}
			}
		}
		
		return sb.toString();
	}
	
	public static String cleanStatusText(String status) {
		String text = MentionUtil.removeMentions(status.replaceAll("转发|微博|更多|\\[[^\\]]*\\]", " "));
		text = URL_PATTERN.matcher(text).replaceAll(" ");
		return text;
	}
	
	public static void main(String[] args) {
		String text = "hu@张三 @李四//@Jason_abc，我的注释 @luo-tian bao";
		System.out.println(MentionUtil.removeMentions(text));
	}
}
