package com.buzzinate.crawl.core.tpl;

import java.util.HashSet;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.buzzinate.crawl.core.util.DomainNames;
import com.buzzinate.crawl.core.util.ExtractUtils;

public class ExtractState {
	private NodeStat[] nodeStats;
	
	public Element root;
	public HashSet<String> freqTexts;
	public String title;
	public String site;
	
	public Element snippetNode;
	public Element textNode;
	public String thumbnail;
	
	public ExtractState(Element root, String site, HashSet<String> freqTexts) {
		this.root = root;
		this.site = site;
		this.freqTexts = freqTexts;

		int total = index(root, 0);
		nodeStats = new NodeStat[total];
		stat(root, site);
	}
	
	public NodeStat getStat(Element e) {
		int nid = Integer.parseInt(e.attr("nid"));
		return nodeStats[nid];
	}
	
	public int getId(Element e) {
		return Integer.parseInt(e.attr("nid"));
	}
	
	public int getLen() {
		return nodeStats.length;
	}
	
	public void setStat(Element e, NodeStat ns) {
		int nid = Integer.parseInt(e.attr("nid"));
		nodeStats[nid] = ns;
	}
	
	private NodeStat stat(Element node, String site) {
		NodeStat ns = new NodeStat();
		for (Node child : node.childNodes()) {
			if (child instanceof TextNode) {
				String text = ((TextNode) child).text().trim();
				NodeStat sub = NodeStat.from(text);
				String pattern = "<" + node.tagName() + ">" + ExtractUtils.pattern(text);
				if (freqTexts.contains(pattern)) sub = new NodeStat(sub.nFreqWords, sub.nWords, sub.nLinkWords, sub.nSiteLinks, sub.nPuncs);
				ns.merge(sub);
			}
			if (child instanceof Element) {
				Element c = (Element) child;
				ns.merge(stat(c, site));
			}
		}
		
		if (node.tagName().equalsIgnoreCase("a")) {
			int nSiteLink = 0;
			String href = node.absUrl("href");
			if (DomainNames.safeGetPLD(href).equals(site) || href.isEmpty()) nSiteLink = 1; 
			ns = new NodeStat(0, ns.nFreqWords, ns.nLinkWords + ns.nWords + ns.nFreqWords, ns.nSiteLinks+nSiteLink, ns.nPuncs);
		}
		
		setStat(node, ns);
		
		return ns;
	}

	private int index(Element node, int offset) {
		for (Element e: node.children()) {
			offset = index(e, offset);
		}
		
		node.attr("nid", String.valueOf(offset));
		return offset + 1;
	}
}