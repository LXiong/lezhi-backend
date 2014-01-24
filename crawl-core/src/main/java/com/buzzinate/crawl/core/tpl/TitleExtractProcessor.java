package com.buzzinate.crawl.core.tpl;

import org.apache.commons.lang.StringUtils;

public class TitleExtractProcessor implements ExtractProcessor {
	private TitleExtractor te = new TitleExtractor();
	
	@Override
	public void process(ExtractState extractState) {
		String title = extractState.root.ownerDocument().title().trim();
		
		String bestTitle = te.extract(title);
		if (StringUtils.isBlank(bestTitle)) {
			extractState.title = title;
			return;
		}
		
		bestTitle = bestTitle.replaceAll("<[^>]+>", "").replaceAll("[\\\r\\\n]", " ").trim();
		
		if (!StringUtils.isBlank(bestTitle)) extractState.title = bestTitle;
		else extractState.title = title;
	}
}