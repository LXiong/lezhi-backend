package com.buzzinate.crawl.core.extract;

import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.buzzinate.crawl.core.util.TextUtility;

/**
 * 找到正文节点里面可以看的图片
 * 
 * @author brad
 *
 */
public class ThumbnailProcessor implements ExtractionProcessor {
	
	@Override
	public void process(ArticleStat articleStat) {
		if (articleStat.textNode == null) return;
		String thumbnail = findBestThumbnail(articleStat);
		articleStat.thumbnail = thumbnail;
	}
	
	public String findBestThumbnail(ArticleStat articleStat) {
		String thumbnail = null;
		
		Element textNode = articleStat.textNode;
		int bestScore = 2;
		
		Elements imgs = textNode.getElementsByTag("img");
		for (int i = 0; i < imgs.size(); i++) {
			Element img = imgs.get(i);
			String ptag = img.parent().tagName();
			if (!ptag.equalsIgnoreCase("a")) {
				int score = scoreThumnail(img);
				if (score > bestScore) {
					bestScore = score;
					thumbnail = img.absUrl("src");
				}
			}
		}
		
		if (thumbnail != null) return thumbnail;
		
		for (int i = 0; i < imgs.size(); i++) {
			Element img = imgs.get(i);
			String ptag = img.parent().tagName();
			if (ptag.equalsIgnoreCase("a")) {
				int score = scoreThumnail(img);
				if (score > bestScore) {
					bestScore = score;
					thumbnail = img.absUrl("src");
				}
			}
		}
		return thumbnail;
	}
	
	private static int scoreThumnail(Element img) {
		String ptag = img.parent().tagName();
		String src = img.absUrl("src");
		int score = 0;
		if (ptag.equals("p")) score++;
		if (img.hasAttr("alt")) score++;
		if (!src.endsWith("gif")) score += 2;
		if (img.hasAttr("width") && TextUtility.parseInt(img.attr("width"), 50) >= 50) score += 2;
		if (img.hasAttr("height") && TextUtility.parseInt(img.attr("height"), 50) >= 50) score += 2;
		score += img.attributes().size();
		
		if (Pattern.compile("/[0-9]+/").matcher(src).find()) score += 3;
		
		return score;
	}
}
