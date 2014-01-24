package com.buzzinate.crawl.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.buzzinate.crawl.core.fetch.PageFetcher;

public class ExtractUtils {
	// 需要删除的tag和属性
	private static HashSet<String> junkTags = new HashSet<String>(Arrays.asList("style", "iframe", "script", "button", "input", "textarea", "embed", "select", "form"));
	private static HashSet<String> keepAtts = new HashSet<String>(Arrays.asList("src", "href", "post.class"));
	private static HashSet<String> headers = new HashSet<String>(Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6"));
	private static HashSet<String> keepTags = new HashSet<String>(Arrays.asList("ul", "li", "dl", "dt", "dd", "h2", "p"));
		
	private ExtractUtils() {
		
	}
	
	public static void preClean(Element node, String baseUrl) {
		cleanJunkTags(node);
		
		Elements tags = node.getElementsByAttribute("href");
	    for (Element tag: tags) {
	    	String href = tag.absUrl("href");
	       	if (href.startsWith("javascript:")) tag.remove();
	       	else if (!href.isEmpty()) {
	       		tag.attr("href", href);
	       	} else tag.removeAttr("href");
	    }
	    
	    tags = node.getElementsByAttribute("src");
	    for (Element tag: tags) {
	       	String src = tag.absUrl("src");
	       	if (!src.isEmpty()) {
	       		tag.attr("src", src);
	       	}
	    }
	    
	    cleanComment(node);
	}
	
	public static void postClean(Element node) {
		Elements es = node.getAllElements();
		for (Element e: es) {
			Attributes atts = e.attributes();
			List<String> toRemoveAtts = new ArrayList<String>();
			for (Attribute att: atts) {
				if (!keepAtts.contains(att.getKey())) toRemoveAtts.add(att.getKey());
			}
			for (String att: toRemoveAtts) e.removeAttr(att);
			for (Attribute att: atts) {
				if (att.getKey().equals("post.class")) att.setKey("class");
			}
		}
	}
	
	public static String format(Element node) {
		Elements es = node.getAllElements();
		for (Element e: es) {
			if (headers.contains(e.tagName())) e.tagName("h2");
			if (e.isBlock() && !keepTags.contains(e.tagName())) e.tagName("div"); 
		}
		return node.outerHtml();
	}
	
	public static void detectFreqText(Element e, FreqTextDetector freqTextDetector) {
		List<Node> childs = e.childNodes();
		for (Node child: childs) {
			if (child instanceof TextNode) {
				String txt = ((TextNode)child).text().trim();
				if (TextUtility.countNumWords(txt) >= 20) continue;
				String p = ExtractUtils.pattern(txt);
				if (p.contains("#-#-#") && p.length() > 5) continue;
				if (p.trim().length() > 0) freqTextDetector.add("<" + e.tagName() + ">" + p);
			}
			if (child instanceof Element) {
				Element c = (Element)child;
				if (c.tagName().equals("img")) freqTextDetector.add("<img>" + c.absUrl("src").trim());
				detectFreqText(c, freqTextDetector);
			}
		}
	}
	
	public static String checkThumbnailSize(String baseUrl, String thumbnail) throws IOException {
		long imageLen = PageFetcher.headContentLength(baseUrl, thumbnail);
		if (imageLen < 10240 && imageLen > -1) {
			thumbnail = null; 
		}
		return thumbnail;
	}
	
	public static String pattern(String text) {
		List<String> words = splitWords(text);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 5 && i < words.size(); i++) sb.append(words.get(i));
		for (int i = Math.max(5, words.size() - 5); i < words.size(); i++) sb.append(words.get(i));
		return sb.toString();
	}
	
	private static void cleanComment(Node node) {
		List<Node> nodes = node.childNodes();
		List<Node> cs = new ArrayList<Node>();
		for (Node n: nodes) {
			if (n instanceof Comment) cs.add(n);
			else cleanComment(n);
		}
		for (Node c: cs) c.remove();
	}

	private static void cleanJunkTags(Element node) {
		Elements es = node.getAllElements();
		List<Element> toRemoveTags = new ArrayList<Element>();
		for (Element e: es) {
			if (junkTags.contains(e.tagName())) toRemoveTags.add(e);
		}
		for (Element e: toRemoveTags) e.remove();
	}
	
	private static List<String> splitWords(String text) {
		List<String> words = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (TextUtility.isAlphaOrDigit(ch)) {
				sb.append(ch);
			} else if (Character.isLetter(ch)) {
				if (sb.length() > 0) {
					words.add(sb.toString());
					sb.setLength(0);
				}
				words.add(String.valueOf(ch));
			} else {
				if (sb.length() > 0) {
					words.add(sb.toString());
					sb.setLength(0);
				}
				String punc = String.valueOf(ch);
				words.add(punc);
			}
		}
		
		if (sb.length() > 0) words.add(sb.toString());
		
		for (int i = 0; i < words.size(); i++) {
			String word = words.get(i);
			boolean allDigit = true;
			for (int k = 0; k < word.length(); k++) {
				char ch = word.charAt(k); 
				if (ch < '0' || ch > '9') {
					allDigit = false;
					break;
				}
			}
			if (allDigit) words.set(i, "#");
		}
		
		return words;
	}
	
	// sentences
	public static List<String> splitSentences(Element node) {
		List<String> sentences = new ArrayList<String>();
		splitSentences(sentences, node);
		return sentences;
	}

	private static void splitSentences(List<String> sentences, Node node) {
		List<Node> childs = node.childNodes();
		for (Node child: childs) {
			if (child instanceof TextNode) {
				String text = ((TextNode)child).text().trim();
				if (text.length() > 0) sentences.addAll(TextUtility.splitSentences(text));
			}
			if (child instanceof Element) {
				Element e = (Element) child;
				if (e.hasClass("userdate")) return;
				splitSentences(sentences, child);
			}
		}
	}
}