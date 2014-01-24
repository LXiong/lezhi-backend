package com.buzzinate.crawl.core.extract;

import java.util.Arrays;
import java.util.HashSet;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TitleNodeCleanProcessor implements ExtractionProcessor {
	private static HashSet<String> headTags = new HashSet<String>(Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6"));

	@Override
	public void process(ArticleStat articleStat) {
		if (articleStat.textNode == null) return;
		
		StringComparator titleComparator = new StringComparator(articleStat.title);
		Elements es = articleStat.textNode.getAllElements();
		for (Element e: es) {
			if (headTags.contains(e.tagName())) {
				float sim = titleComparator.compare(e.text());
				if (sim > 0.7) e.remove();
			}
		}
	}
}