package com.buzzinate.crawl.core.extract;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 基于标点的snippetNode处理器
 * 
 * @author brad
 *
 */
public class PuncBasedSnippetNodeProcessor implements ExtractionProcessor {
	private static Logger log = Logger.getLogger(PuncBasedSnippetNodeProcessor.class);

	@Override
	public void process(ArticleStat articleStat) {
		Element root = articleStat.root;
		Element prevSnippet = articleStat.snippetNode;
		NodeStat rootStat = articleStat.getStat(root);
		float puncThreshold = rootStat.totalPuncNum / 3f;
		
		Element snippetNode = root;
		if (prevSnippet != null) {
			snippetNode = prevSnippet.parent();
			NodeStat nodeStat = articleStat.getStat(snippetNode);
			while (nodeStat.totalPuncNum < puncThreshold && snippetNode != root) {
				snippetNode = snippetNode.parent();
				nodeStat = articleStat.getStat(snippetNode);
			}
		}
		
		StringComparator titleComparator = new StringComparator(articleStat.title);
		float minSim = 0.01f;
		if (prevSnippet != null) {
			minSim = titleComparator.compare(prevSnippet.ownText()) * 0.2f;
		}
		
		snippetNode = findMaxPuncNode(articleStat, snippetNode, titleComparator, minSim);
		log.debug("punc based best snippet node:" + snippetNode);
		if (snippetNode != null) articleStat.snippetNode = snippetNode;
	}

	private Element findMaxPuncNode(ArticleStat articleStat, Element node, StringComparator titleComparator, float minSim) {
		Element best = null;
		int maxPuncCnt = 5;
		
		Elements es = node.getAllElements();
		for (Element e: es) {
			float sim = titleComparator.compare(e.ownText());
			if (sim <= minSim) continue;
			
			int puncTotal = 0;
			for (Element c: e.children()) {
				NodeStat nodeStat = articleStat.getStat(c);
				puncTotal += nodeStat.puncNum;
			}
			
			NodeStat nodeStat = articleStat.getStat(e);
			puncTotal += nodeStat.puncNum;
			
			if (puncTotal > maxPuncCnt) {
				maxPuncCnt = puncTotal;
				best = e;
			}
		}
		
		return best;
	}
}
