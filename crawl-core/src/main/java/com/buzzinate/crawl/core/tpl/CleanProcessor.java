package com.buzzinate.crawl.core.tpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.buzzinate.crawl.core.util.TextUtility;

public class CleanProcessor implements ExtractProcessor {
	private static Logger log = Logger.getLogger(CleanProcessor.class);

	@Override
	public void process(ExtractState extractState) {
		if (extractState.textNode == null) return;
		
		int nTotalWords = extractState.getStat(extractState.root).nWords;
		List<Element> noizeNodes =  new ArrayList<Element>();
		clean(extractState, extractState.textNode, nTotalWords, extractState.freqTexts, noizeNodes);
		for (Element n: noizeNodes) {
			if (n.parent() != null && !n.hasAttr("post.class")) {
				n.remove();
			}
		}
	}

	private int clean(ExtractState es, Element e, int nTotalWords, HashSet<String> freqTexts, List<Element> noizeNodes) {
		int nImgs = 0;
		
		if (e.tagName().equals("img")) {
			String src = "<img>" + e.absUrl("src").trim();
			if (!freqTexts.contains(src)) nImgs += 1;
		}
		
		if (e.hasAttr("nid")) {
			Elements cs = e.children();
			for (Element c: cs) {
				nImgs += clean(es, c, nTotalWords, freqTexts, noizeNodes);
			}
			NodeStat ns = es.getStat(e);
			if (TextUtility.isNoizeTags(e.tagName()) && ns.nWords * 11 < nTotalWords) {
				double txtDensity = (ns.nWords + nImgs * 3)/ (1f + ns.nFreqWords + ns.nWords + nImgs * 3 + ns.nLinkWords + ns.nSiteLinks * 5);
				if (txtDensity < 0.3) {
					if (txtDensity > 0.1) log.debug("remove node(" + txtDensity + "): " + e);
//					if (txtDensity > 0.1) System.out.println("remove node(" + txtDensity + "): " + e);
					noizeNodes.add(e);
				}
			}
		}
		
		return nImgs;
	}
}