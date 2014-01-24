package com.buzzinate.crawl.core.extract;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

/**
 * 基于标点的正文节点处理器
 * 
 * @author brad
 *
 */
public class PuncBasedTextNodeProcessor implements ExtractionProcessor {
	private static Logger log = Logger.getLogger(PuncBasedTextNodeProcessor.class);

	@Override
	public void process(ArticleStat articleStat) {
		if (articleStat.snippetNode == null) return;
		
		Element root = articleStat.root;
		NodeStat rootStat = articleStat.getStat(root);
		float puncThreshold = rootStat.totalPuncNum / 2f;
		float wordCntThreshold = rootStat.wordNum / 2f;
		Element txtNode = articleStat.snippetNode;
		double maxRatio = 0f;
		int depth = 1;
		
		Element current = articleStat.snippetNode;
		NodeStat nodeStat = articleStat.getStat(current);
		while (current != root && nodeStat.wordNum < wordCntThreshold && nodeStat.totalPuncNum < puncThreshold) {
			Element pnode = current.parent();
			NodeStat pNodeStat = articleStat.getStat(pnode);
			
			double ratio = (pNodeStat.totalPuncNum - nodeStat.totalPuncNum) / (1f + pNodeStat.totalPuncNum + nodeStat.totalPuncNum);
			ratio = ratio * 2f / (1 + Math.sqrt(depth));
			log.debug(nodeStat.totalPuncNum + " ==> " + pNodeStat.totalPuncNum + ", ratio=" + ratio);
			if (ratio >= maxRatio) {
				maxRatio = ratio;
				txtNode = pnode;
			}
			depth++;
			current = pnode;
			nodeStat = pNodeStat;
		}
		
		articleStat.textNode = txtNode;
	}
}
