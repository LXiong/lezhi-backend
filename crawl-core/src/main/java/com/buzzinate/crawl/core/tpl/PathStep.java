package com.buzzinate.crawl.core.tpl;

import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;

import com.buzzinate.crawl.core.util.TextUtility;

public class PathStep {
	private String tag;
	private String id;
	private Set<String> classNames;
	
	public PathStep(String tag, String id, Set<String> classNames) {
		this.tag = tag;
		this.id = id;
		this.classNames = classNames;
	}
	
	public static PathStep from(Element e) {
		return new PathStep(e.tagName(), TextUtility.formatNum(e.id()), e.classNames());
	}

	public static boolean isSame(PathStep ps1, PathStep ps2) {
		int cnt = 0;
		for (String cn: ps1.classNames) {
			if (ps2.classNames.contains(cn)) cnt++;
		}
		return ps1.tag.equals(ps2.tag) && ps1.id.equals(ps2.id) && cnt > 0;
	}
	
	public static int countSame(List<PathStep> p1, List<PathStep> p2) {
		int cnt = 0;
		int start = 0;
		int end = 1;
		for (start = 0; start < p1.size() && start < p2.size(); start++) {
			if (isSame(p1.get(start), p2.get(start))) cnt++;
			else break;
		}
		for (end = 1; end <= p1.size() - start && end <= p2.size() - start; end++) {
			if (isSame(p1.get(p1.size() - end), p2.get(p2.size() - end))) cnt++;
			else break;
		}
		return cnt;
	}

	public String getTag() {
		return tag;
	}

	public String getId() {
		return id;
	}

	public Set<String> getClassNames() {
		return classNames;
	}
}