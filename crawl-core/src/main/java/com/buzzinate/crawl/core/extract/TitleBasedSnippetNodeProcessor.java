package com.buzzinate.crawl.core.extract;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 基于文章标题的snippetNode处理器，找到跟title最相似的文本所在节点
 * 
 * @author brad
 *
 */
public class TitleBasedSnippetNodeProcessor implements ExtractionProcessor {
	private static Logger log = Logger.getLogger(TitleBasedSnippetNodeProcessor.class);
	private static Pattern titleSplitter = Pattern.compile("(_|-|·|\\|)");

	@Override
	public void process(ArticleStat articleStat) {
		StringComparator titleComparator = new StringComparator(articleStat.title);
		
		int minLen = articleStat.title.length() * 2;
		Elements es = articleStat.root.getAllElements();
		float maxSim = 0;
		Element best = null;
		for (Element e: es) {
			String ot = e.ownText();
			if (ot.length() > minLen) continue;
			float d = titleComparator.compare(ot);
			if (maxSim < d) {
				maxSim = d;
				best = e;
			}
		}
		
		log.debug("Best match title(" + maxSim + "): " + best);
		if (best != null) {
			StringComparator snippetComparator = new StringComparator(best.ownText());
			Matcher m = titleSplitter.matcher(articleStat.title);
			String bestTitle = articleStat.title;
			maxSim = snippetComparator.compare(bestTitle) * 0.9f;
			while (m.find()) {
				String pt = articleStat.title.substring(0, m.start()).trim();
				float s = snippetComparator.compare(pt);
				if (maxSim < s) {
					maxSim = s;
					bestTitle = pt;
				}
			}
			log.debug("Possible best title: " + bestTitle);
			articleStat.title = bestTitle;
		}
		
		articleStat.snippetNode = best;
	}
}
