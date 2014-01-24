package com.buzzinate.crawl.core.tpl;

import java.io.IOException;
import java.util.HashSet;

import org.jsoup.nodes.Element;

import com.buzzinate.crawl.core.extract.ArticleText;

public class SiteTemplate {
	private HashSet<String> freqTexts;
	
	public SiteTemplate(HashSet<String> freqTexts) {
		this.freqTexts = freqTexts;
	}
	
	public ArticleText extract(String url, Element root) throws IOException {
		return TplExtractor.extract(url, root, freqTexts);
	}
}