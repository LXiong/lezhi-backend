package com.buzzinate.jianghu.util;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentCleaner {

	public static String regxpForHtml = "<dd[^>]*>[^<]*?</dd>"; // 匹配所有以<dd>开头以</dd>结尾的标签
	public static Pattern pattern = Pattern.compile(regxpForHtml);

	/**
	 * 如果content中标签为 <dd>***</dd> 内的内容只有图片链接，则更改这个标签为图片标签
	 * 
	 * @param oriContent
	 * @return
	 */
	public static String clean(String oriContent) {
		try {
			Matcher matcher = pattern.matcher(oriContent);
			StringBuffer sb = new StringBuffer();
			boolean result = matcher.find();
			while (result) {
				String group = matcher.group();
				String groupContent = group.replaceAll("<([^>]*)>", "").trim();

				try {
					new URL(groupContent);
					if (groupContent.endsWith("jpg") || groupContent.endsWith("jpeg")) {
						String imgSrc = "<img src=\"" + groupContent + "\" />";
						matcher.appendReplacement(sb, imgSrc);
					}
				} catch (Throwable rt) {
				}
				result = matcher.find();
			}
			matcher.appendTail(sb);
			return sb.toString();
		} catch (Throwable rt) {
			return oriContent;
		}
	}
}
