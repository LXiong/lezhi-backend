package com.buzzinate.crawl.core.tpl;

import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.buzzinate.crawl.core.util.DomainNames;
import com.buzzinate.crawl.core.util.ExtractUtils;
import com.buzzinate.crawl.core.util.TextUtility;

public class TextNodeProcessor implements ExtractProcessor {

	@Override
	public void process(ExtractState extractState) {
		Element root = extractState.root;
		LongestSnippetState ls = new LongestSnippetState();
		selectSnippet(extractState, root, 0, ls);
		
		extractState.snippetNode = ls.getBest();
		
		Element textNode = extractState.snippetNode;
		if (textNode == null) return;
		
		int nMinPuncs = extractState.getStat(root).nPuncs * 3 / 11;
		
		Element current = extractState.snippetNode;
		NodeStat ns = extractState.getStat(current);
		while (current != root) {
			if (!current.tag().isInline() && ns.nPuncs >= nMinPuncs) {
				Element pn = current.parent();
				NodeStat pns = extractState.getStat(pn);
				if (pns.nPuncs <= ns.nPuncs + 5 || (pns.nPuncs <= ns.nPuncs + 15 && pns.nLinkWords > ns.nLinkWords * 2)) {
					textNode = current;
					break;
				}
			}
			current = current.parent();
			ns = extractState.getStat(current);
		}
		
		extractState.textNode = textNode;
	}
	
	private static void selectSnippet(ExtractState es, Element e, int depth, LongestSnippetState state) {
		if (e.tagName().equals("a")) {
			String href = e.absUrl("href").trim();
			if (href.isEmpty()) state.appendSiteLink();
			else {
				String site = DomainNames.safeGetPLD(href);
				if (es.site.equals(site)) state.appendSiteLink();
				else state.appendOtherLink();
			}
			return;
		}
		
		if (e.tagName().equals("img")) {
			state.appendWords(depth, e, 2, 0);
		}
		
		List<Node> childs = e.childNodes();
		for (Node child: childs) {
			if (child instanceof TextNode) {
				String text = ((TextNode) child).text().trim();
				if (text.isEmpty()) continue;
				String pattern = ExtractUtils.pattern(text);
				if (!es.freqTexts.contains(pattern)) state.appendWords(depth, e, TextUtility.countNumWords(text), TextUtility.countPuncs(text));
			}
			if (child instanceof Element) {
				selectSnippet(es, (Element) child, depth+1, state);
			}
		}
	}
	
	public static class LongestSnippetState {
		private Element best;
		private int nMaxTotalWords = 0;
		private int nMaxTotalPuncs = 0;
		
		private Element snippet;
		private int nOtherLinks = 0;
		private int nTotalWords = 0;
		private int nTotalPuncs = 0;
		private int nMaxWords = 0;

		public void appendWords(int depth, Element e, int nWords, int nPuncs) {
			this.nTotalWords += nWords;
			this.nTotalPuncs += nPuncs;
			if (nWords > nMaxWords) {
				nMaxWords = nWords;
				snippet = e;
			}
			nOtherLinks = 0;
		}
		
		public void appendSiteLink() {
			checkBest();
			clear();
		}
		
		public void appendOtherLink() {
			nOtherLinks++;
			if (nOtherLinks >= 3) {
				checkBest();
				clear();
			}
		}

		private void clear() {
			snippet = null;
			nTotalWords = 0;
			nTotalPuncs = 0;
			nMaxWords = 0;
			nOtherLinks = 0;
		}
		
		public Element getBest() {
			checkBest();
			return best;
		}
		
		private void checkBest() {
			if (nTotalPuncs * 20 + nTotalWords > nMaxTotalPuncs * 20 + nMaxTotalWords) {
				best = snippet;
				nMaxTotalWords = nTotalWords;
				nMaxTotalPuncs = nTotalPuncs;
			}
		}
	}
}