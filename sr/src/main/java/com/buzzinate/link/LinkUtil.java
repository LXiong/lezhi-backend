package com.buzzinate.link;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkUtil {
	private static final Pattern URL_PATTERN = Pattern.compile("http://(sinaurl|t).cn/(\\w+)");
	
	public static List<String> extractLinks(String text) {
		List<String> links = new ArrayList<String>();
		Matcher matcher = URL_PATTERN.matcher(text);
		while (matcher.find()) {
			String link = matcher.group(2);
			links.add(link);
		}
		return links;
	}
}
