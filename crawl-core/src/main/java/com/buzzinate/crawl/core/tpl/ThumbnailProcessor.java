package com.buzzinate.crawl.core.tpl;

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
public class ThumbnailProcessor implements ExtractProcessor {
	
	@Override
	public void process(ExtractState extractState) {
		if (extractState.textNode == null) return;
		
		String thumbnail = findBestThumbnail(extractState.textNode);
		extractState.thumbnail = thumbnail;
	}
	
	public String findBestThumbnail(Element textNode) {
		String thumbnail = null;
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
