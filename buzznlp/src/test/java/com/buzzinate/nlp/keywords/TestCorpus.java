package com.buzzinate.nlp.keywords;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.buzzinate.nlp.util.PriorityQueue.Entry;
import com.buzzinate.nlp.util.TextUtility;

public class TestCorpus {
	
	public static void main(String[] args) throws IOException {
//		String[] urls = new String[] {
//				"http://www.infoq.com/cn/news/2011/11/chronon-20",
//				"http://www.infoq.com/news/2012/02/MapReducePatterns"
//		};
//		String sq = "div.box-content-5";
//		String rq = "div.vendor-content-box";
		String[] urls = new String[] {
				"http://tech.sina.com.cn/i/2012-02-10/08576707180.shtml",
				"http://tech.sina.com.cn/it/2012-02-10/00036703875.shtml",
				"http://tech.sina.com.cn/it/2012-02-10/00096703906.shtml",
				"http://tech.sina.com.cn/it/2012-02-10/01256704464.shtml",
				"http://tech.sina.com.cn/i/2012-02-10/16136708599.shtml"
		};
		String sq = "div.blkContainerSblkCon";
		String rq = "div.otherContent_01";
//		String[] urls = new String[] {
//				"http://www.tianya.cn/publicforum/content/travel/1/389524.shtml",
//				"http://www.tianya.cn/publicforum/content/travel/1/390300.shtml",
//				"http://www.tianya.cn/publicforum/content/travel/1/390951.shtml"
//		};
//		String sq = "div.allpost";
		
		for (String url: urls) {
			Document doc = Jsoup.connect(url).get();
			Element node = doc.select(sq).first();
			node.select(rq).first().remove();
			
			List<String> oldSentences = splitSentences(node);
			List<String> sentences = new ArrayList<String>();
			for (String s: oldSentences) {
				if (TextUtility.countNumWords(s) > 5) sentences.add(s);
			}
			
			KeywordSummaryExtractor kse = new KeywordSummaryExtractor(doc.title(), sentences);
			List<Entry<Double, String>> ks = kse.extractKeywords();
			for (Entry<Double, String> k: ks) System.out.println(k.getKey() + ": " + k.getValue());
			System.out.println(kse.extractSummary(100));
		}
	}
	
	private static List<String> splitSentences(Element node) throws IOException {
		List<String> sentences = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		splitSentences(sentences, node, sb);
		if (sb.length() > 0) {
			sentences.addAll(TextUtility.splitSentences(sb.toString()));
			sb.setLength(0);
		}
		return sentences;
	}

	private static void splitSentences(List<String> sentences, Node node, StringBuffer sb) throws IOException {
		List<Node> childs = node.childNodes();
		for (Node child: childs) {
			if (child instanceof TextNode) {
				sb.append(((TextNode)child).text());
			}
			if (child instanceof Element) {
				Element e = (Element) child;
				if (e.isBlock()) {
					sentences.addAll(TextUtility.splitSentences(sb.toString()));
					sb.setLength(0);
				}
//				if (e.tagName().equalsIgnoreCase("a")) sb.append("<a>");
				splitSentences(sentences, child, sb);
//				if (e.tagName().equalsIgnoreCase("a")) sb.append("</a>");
				if (e.isBlock()) {
					sentences.addAll(TextUtility.splitSentences(sb.toString()));
					sb.setLength(0);
				}
			}
		}
	}
}