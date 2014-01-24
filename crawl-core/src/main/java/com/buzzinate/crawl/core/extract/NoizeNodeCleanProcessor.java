package com.buzzinate.crawl.core.extract;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

import com.buzzinate.crawl.core.util.TextUtility;

/**
 * 删除噪音节点
 * 
 * @author brad
 *
 */
public class NoizeNodeCleanProcessor implements ExtractionProcessor {
	private static Logger log = Logger.getLogger(NoizeNodeCleanProcessor.class);

	@Override
	public void process(ArticleStat articleStat) {
		if (articleStat.textNode == null) return;
		int totalWordCnt = articleStat.getStat(articleStat.root).wordNum;
		removeNoize(articleStat, articleStat.textNode, totalWordCnt);
	}

	public void removeNoize(ArticleStat articleStat, Element node, int totalWordCnt) {
		NodeStat nodeStat = articleStat.getStat(node);
		float linkDensity = nodeStat.linkWordNum / (1f + nodeStat.linkWordNum + nodeStat.wordNum);
		if (TextUtility.isNoizeTags(node.tagName()) && nodeStat.wordNum * 20 < totalWordCnt && linkDensity > 0.8f) {
			if (log.isDebugEnabled()) log.debug("remove high linkdensity node: " + node);
			articleStat.remove(node);
		} else if(TextUtility.isNoizeTags(node.tagName()) && nodeStat.wordNum * 20 < totalWordCnt && nodeStat.wordNum * 3 < nodeStat.emptyNodeNum * 2) {
			if (log.isDebugEnabled()) log.debug("remove mainly empty node: " + node);
			articleStat.remove(node);
		} else {
			for (Element e: node.children()) {
				removeNoize(articleStat, e, totalWordCnt);
			}
		}
	}
}
