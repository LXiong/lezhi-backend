package com.buzzinate.crawl.core.tpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

import com.buzzinate.crawl.core.extract.ArticleText;
import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;
import com.buzzinate.crawl.core.util.DomainNames;
import com.buzzinate.crawl.core.util.ExtractUtils;
import com.buzzinate.crawl.core.util.FreqTextDetector;
import com.buzzinate.crawl.core.util.TextUtility;

public class TplExtractor {
	private static Logger log = Logger.getLogger(TplExtractor.class);
	
	public static final List<ExtractProcessor> processors = Arrays.asList(
			new TextNodeProcessor(),
			new TitleExtractProcessor(),
			new ForumProcessor(),
			new DeduplicateInlineTagProcessor(),
			new CleanProcessor(),
			new ThumbnailProcessor()
		);
	
	public static void main(String[] args) throws IOException {
		String url = "http://news.dayoo.com/guangzhou/201112/09/73437_20875972.htm";
		Response resp = PageFetcher.fetch(url);
		ArticleText text = TplExtractor.extract(url, resp.getDoc().body(), new HashSet<String>());
		System.out.println(text.getContent());
	}
	
	public static ArticleText extract(String url, Element root, HashSet<String> freqTexts) throws IOException {
		log.debug("Extract: " + url);
		ExtractUtils.preClean(root, url);
		
		FreqTextDetector ftd = new FreqTextDetector();
		ExtractUtils.detectFreqText(root, ftd);
		for (String text: ftd.freqPageText(3)) {
			if (!text.contains("#")) freqTexts.add(text);
		}
		
		ExtractState extractState = new ExtractState(root, DomainNames.safeGetPLD(url), freqTexts);
		
		for (ExtractProcessor p: processors) p.process(extractState);
		
		if (extractState.textNode == null) return new ArticleText(url, extractState.title, "", null, 0, 0);
		
		ExtractUtils.postClean(extractState.textNode);
		
		String content = ExtractUtils.format(extractState.textNode);
		int textWordCnt = TextUtility.countNumWords(extractState.textNode.text());
		int totalLength = TextUtility.countNumWords(content);
		
		String title = extractState.title;
		if (title == null) title = root.ownerDocument().title();
		return new ArticleText(url, title, content, extractState.thumbnail, textWordCnt, textWordCnt / (1f + totalLength));
	}
}