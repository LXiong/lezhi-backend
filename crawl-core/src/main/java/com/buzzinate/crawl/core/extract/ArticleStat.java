package com.buzzinate.crawl.core.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.buzzinate.crawl.core.util.Counter;
import com.buzzinate.crawl.core.util.TextUtility;

/**
 * 统计html中各节点的中文字数，链接字数，标点数等，以方便其他正文抽取程序去抽取 正文，摘要，图片等
 * 
 * @author brad
 *
 */
public class ArticleStat {
	private static Logger log = Logger.getLogger(ArticleStat.class);
	
	private static final HashSet<Character> puncs = new HashSet<Character>(Arrays.asList(',', '.', '，', '。'));
	private static final HashSet<String> globalFreqWords = new HashSet<String>(
			Arrays.asList("招呼","串个","勋章","身卡","举报","消息","用道","帖子","论坛","威望","显身","使用","#点","打招","好友","发表",
					"显示","道具","加好","表于","登录","##","发消","八品","于#","楼层","积分","粉丝","穷人","知道","回复","位粉","#楼",
					"在线","金钱","贡献","注册","相册","帖子","精华","分享","首页","正文","评论","字号","#顶","#踩")
		);
	
	private NodeStat[] nodeStats;
	
	public Element root;
	public Element snippetNode;
	public Element textNode;
	public String title;
	public String thumbnail;
	
	public int cleanWordNum = 0;
	public int cleanLinkWordNum = 0;
	
	public ArticleStat(Element root, String title) {
		this.root = root;
		this.title = title;
		
		int total = index(root, 0);
		nodeStats = new NodeStat[total];
		for (int i = 0; i < nodeStats.length; i++) {
			nodeStats[i] = new NodeStat();
		}
		Counter<String> counter = new Counter<String>();
		findFreqWords(root, counter);
		HashSet<String> freqWords = new HashSet<String>(counter.freqItems(0f, 5));
		freqWords.addAll(globalFreqWords);
		log.debug("freq words: " + freqWords);
		stat(root, freqWords);
	}
	
	public void remove(Element e) {
		NodeStat nodeStat = getStat(e);
		
		Element p = e.parent();
		while (p != root) {
			NodeStat cs = getStat(p);
			cs.substract(nodeStat);
			p = p.parent();
		}
		getStat(root).substract(nodeStat);
		e.remove();
	}
	
	public NodeStat getStat(Element e) {
		int nid = Integer.parseInt(e.attr("nid"));
		return nodeStats[nid];
	}
	
	private int stat(Element node, HashSet<String> freqWords) {
		String ot = node.ownText().trim();
		ot = ot.replaceAll("[0-9]+", " ").trim();
		int freqLen = 0;
		int total = 0;
		for (Node child : node.childNodes()) {
			if (child instanceof TextNode) {
				String text = ((TextNode) child).text();
				text = text.trim();
				total += text.length();
				if (text.length() < 25 && text.length() > 0) {
					List<String> words = getCandidateFreqWords(text);
					for (String word: words) {
						if (freqWords.contains(word)) freqLen += word.length();
					}
				}
			}
		}
		
		int totalPuncCnt = 0;
		int emptyCnt = node.children().isEmpty()? 1: 0;
		int wordCnt = TextUtility.countNumWords(ot.trim());
		int linkWordCnt = 0;
		for (Element e: node.children()) {
			totalPuncCnt += stat(e, freqWords);
			NodeStat nodeStat = getStat(e);
			emptyCnt += nodeStat.emptyNodeNum;
			wordCnt += nodeStat.wordNum;
			linkWordCnt += nodeStat.linkWordNum;
		}
		if (node.tagName().equalsIgnoreCase("a")) {
			linkWordCnt = linkWordCnt + wordCnt;
			wordCnt = 0;
		}
		if (freqLen * 8 > total) wordCnt = 0;
		
		NodeStat nodeStat = getStat(node);
		int puncCnt = 0;
		for (int i = 0; i < ot.length(); i++) {
			if (puncs.contains(ot.charAt(i))) puncCnt++;
		}
		nodeStat.puncNum = puncCnt;
		
		totalPuncCnt += puncCnt;
		
		nodeStat.totalPuncNum = totalPuncCnt;
		nodeStat.emptyNodeNum = emptyCnt;
		nodeStat.wordNum = wordCnt;
		nodeStat.linkWordNum = linkWordCnt;
		
		return totalPuncCnt;
	}

	private int index(Element node, int offset) {
		for (Element e: node.children()) {
			offset = index(e, offset);
		}
		
		node.attr("nid", String.valueOf(offset));
		return offset + 1;
	}
	
	private void findFreqWords(Element node, Counter<String> counter) {
		List<Node> childs = node.childNodes();
		for (Node child: childs) {
			if (child instanceof TextNode) {
				String text = ((TextNode) child).text().trim();
				if (text.length() < 25 && text.length() > 0) {
					text = text.trim();
					List<String> words = getCandidateFreqWords(text);
					for (String w: words) counter.add(w);
				}
			}
			if (child instanceof Element) {
				findFreqWords((Element)child, counter);
			}
		}
	}
	
	private static List<String> getCandidateFreqWords(String text) {
		List<String> freqWords = new ArrayList<String>();
		List<String> words = TextUtility.splitWords(text);
		if (words.size() >= 2) freqWords.add(StringUtils.join(words.subList(0, 2), ""));
		if (words.size() >= 3) freqWords.add(StringUtils.join(words.subList(1, 3), ""));
		if (words.size() >= 4) freqWords.add(StringUtils.join(words.subList(2, 4), ""));
		if (words.size() >= 5) freqWords.add(StringUtils.join(words.subList(words.size() - 2, words.size()), ""));
		return freqWords;
	}
}