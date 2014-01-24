package com.buzzinate.crawl.core.tpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.buzzinate.crawl.core.util.TextUtility;
import com.google.common.collect.HashMultimap;

public class ForumProcessor implements ExtractProcessor {
	private static Logger log = Logger.getLogger(ForumProcessor.class);
	
	private static List<String> postNames = Arrays.asList("post", "message", "msg", "thread");

	@Override
	public void process(ExtractState extractState) {
		if (extractState.textNode == null) return;
		
		Element root = extractState.root;
		HashMap<String, Integer> tagAttCnt = new HashMap<String, Integer>();
		
		int snippetDepth = 0;
		Element current = extractState.snippetNode;
		while (current != root) {
			snippetDepth++;
			current = current.parent();
		}
		
		int depthDiff = 0;
		current = extractState.snippetNode;
		while (current != root) {
			int depth = snippetDepth - depthDiff;
			for (String key: postKeys(current, depth)) {
				tagAttCnt.put(key, 0);
			}
			depthDiff++;
			current = current.parent();
		}
		
		countTagAttr(extractState.root, 0, tagAttCnt);
		
		int nMaxPuncs = extractState.getStat(root).nPuncs * 5 / 11;
		
		int nMaxNodes = 2;
		int nPrevPuncs = 0;
		Element postNode = null;
		current = extractState.snippetNode;
		depthDiff = 0;
		while (current != root) {
			int depth = snippetDepth - depthDiff;
			int nPuncs = extractState.getStat(current).nPuncs;
			if (nPuncs >= nMaxPuncs) break;
			
			if (nPuncs <= nPrevPuncs + 3 && postNode != null) break;
			
			List<String> keys = postKeys(current, depth);
			if (keys.size() >= 2) {
				int minCnt = Integer.MAX_VALUE;
				int maxClassCnt = -1;
				for (String key: keys) {
					int cnt = tagAttCnt.get(key);
					if (key.startsWith(depth + "|c|")) {
						if(maxClassCnt < cnt) maxClassCnt = cnt;
					}
					else {
						if (cnt < minCnt) minCnt = cnt;
					}
				}
				if (maxClassCnt > -1 && maxClassCnt < minCnt) minCnt = maxClassCnt;
				if (minCnt == 1 && postNode != null) break;
				
				if (minCnt >= nMaxNodes) {
					nMaxNodes = minCnt;
					postNode = current;
				}
			}
			depthDiff++;
			nPrevPuncs = nPuncs;
			current = current.parent();
		}
		
		if (postNode == null) return;
		
		List<PathStep> txtPath = new ArrayList<PathStep>();
		current = postNode;
		while (current != root.parent()) {
			txtPath.add(PathStep.from(current));
			current = current.parent();
		}
		Collections.reverse(txtPath);
		
		List<PathStep> path = new ArrayList<PathStep>();
		List<Element> nodes = new ArrayList<Element>();
		collect(extractState, root, txtPath, path, 0, nodes);
		
		if (nodes.size() >= 2) {
			int len = extractState.getLen();
			NodeData[] nds = new NodeData[len];
			for (int i = 0; i < nds.length; i++) nds[i] = new NodeData();
			
			Element postRoot = null;
			for (Element node: nodes) {
				current = node;
				while (current != root.parent()) {
					NodeData nd = nds[extractState.getId(current)];
					nd.nPost++;
					if (nd.nPost == nodes.size() && postRoot == null) postRoot = current;
					current = current.parent();
				}
			}
			
			int nTotalPuncs = extractState.getStat(postRoot).nPuncs;
			int nMaxPostPuncs = 0;
			Elements cs = postRoot.children();
			for (Element c: cs) {
				int nPostPuncs = extractState.getStat(c).nPuncs;
				if (nPostPuncs > nMaxPostPuncs) nMaxPostPuncs = nPostPuncs;
			}
			
			if (nTotalPuncs < nMaxPostPuncs + 5) return;
			
			log.info("############" + extractState.root.baseUri() + " is forum, node: " + postNode.tagName() + "#" + postNode.id() + "." + postNode.className());
			
			HashMultimap<String, String> text2urls = HashMultimap.create();
			Elements links = postRoot.getElementsByTag("a");
			for (Element link: links) text2urls.put(link.ownText(), link.absUrl("href"));
			
			extractUsers(extractState, postRoot, nds, text2urls);
			extractDates(extractState, postRoot, nds);
			
			for (Element node: nodes) node.attr("post", "post");
			Document doc = root.ownerDocument();
			Element allPost = doc.createElement("div");
			for (Element node: nodes) {
				UsefullText user = new UsefullText(null, 0);
				UsefullText date = new UsefullText(null, 0);
				Element last = node;
				current = node;
				while (current != postRoot) {
					NodeData nd = nds[extractState.getId(current)];
					if (nd.user != null) user.max(nd.user);
					if (nd.date != null) date.max(nd.date);
					last = current;
					current = current.parent();
				}
				
				Element prev = last.previousElementSibling();
				if (prev != null) {
					NodeData pnd = nds[extractState.getId(prev)];
					if (user.getText() == null && pnd.user != null) user.max(pnd.user);
					if (date.getText() == null && pnd.date != null) date.max(pnd.date);
				}
				
				Element post = doc.createElement("div");
				post.attr("post.class", "post");
				
				Element ud = doc.createElement("div");
				ud.attr("post.class", "userdate");
				List<String> texts = new ArrayList<String>();
				if (user.getText() != null) texts.add(user.getText());
				if (date.getText() != null) texts.add(date.getText());
				ud.text(StringUtils.join(texts, ", "));
				
				node.attr("post.class", "content");
				
				post.appendChild(ud);
				post.appendChild(node);
				allPost.appendChild(post);
			}
			
			extractState.textNode = allPost;
		}
	}

	private void countTagAttr(Element e, int depth, HashMap<String, Integer> tagAttCnt) {
		List<Integer> diffs = Arrays.asList(-1, 0, 1);
		for (int diff: diffs) {
			for (String key: postKeys(e, depth + diff)) {
				Integer cnt = tagAttCnt.get(key);
				if (cnt != null) tagAttCnt.put(key, cnt+1);
			}
		}
		
		Elements cs = e.children();
		for (Element c: cs) countTagAttr(c, depth+1, tagAttCnt);
	}

	private List<String> postKeys(Element e, int depth) {
		List<String> keys = new ArrayList<String>();
		if (!e.tagName().equals("p") && !e.tag().isInline()) {
			keys.add(depth + "|" + e.tagName());
			if (maybePost(e.id())) keys.add(depth + "|id|" + TextUtility.formatNum(e.id()));
			for (String c: e.classNames()) {
				if (maybePost(c)) keys.add(depth + "|c|" + c);
			}
		}
		return keys;
	}

	private boolean maybePost(String name) {
		for (String postName: postNames) {
			if (name.contains(postName)) return true;
		}
		return false;
	}

	private UsefullText extractDates(ExtractState es, Element e, NodeData[] nds) {
		UsefullText date = new UsefullText(null, 0);
		if (e.hasAttr("post")) return date;
		
		List<Node> childs = e.childNodes();
		for (Node child: childs) {
			if (child instanceof TextNode) {
				String txt = ((TextNode)child).text().trim();
				if (txt.length() > 25) continue;
				String p = TextUtility.formatNum(txt);
				if (p.contains("#-#-#")) date.max(new UsefullText(txt, p.length()+1-"#-#-#".length()));
				else if (p.contains("#:#")) date.max(new UsefullText(txt, p.length()+1-"#:#".length()));
			}
			if (child instanceof Element) {
				Element c = (Element) child;
				date.max(extractDates(es, c, nds));
			}
		}
		
		NodeData nd = nds[es.getId(e)];
		if (nd.nPost <= 1) nd.date = date;
		
		return date;
	}

	private UsefullText extractUsers(ExtractState es, Element e, NodeData[] nds, HashMultimap<String, String> text2urls) {
		UsefullText user = new UsefullText(null, 0);
		if (e.hasAttr("post")) return user;
		
		if (e.tagName().equals("a")) {
			String txt = e.ownText();
			String url = TextUtility.formatNum(e.absUrl("href"));
			if (text2urls.get(txt).size() == 1) {
				int score = 0;
				if (url.contains("space") || url.contains("uid")) score += 3;
				if (StringUtils.substringAfter(url, "/").contains("#")) score += 2;
				user.max(new UsefullText(txt, score));
			}
		} else {
			List<Node> childs = e.childNodes();
			for (Node child: childs) {
				if (child instanceof TextNode) {
					String txt = ((TextNode)child).text().trim();
					if (txt.isEmpty() || txt.length() > 25) continue;
					for (Attribute a: e.attributes()) {
						if (!a.getKey().equals("nid") && a.getValue().contains(txt)) user.max(new UsefullText(txt, 3));
					}
				}
				if (child instanceof Element) {
					Element c = (Element) child;
					user.max(extractUsers(es, c, nds, text2urls));
				}
			}
		}
		
		NodeData nd = nds[es.getId(e)];
		if (nd.nPost <= 1) nd.user = user;
		
		return user;
	}

	private void collect(ExtractState es, Element e, List<PathStep> txtPath, List<PathStep> path, int depth, List<Element> nodes) {
		path.add(PathStep.from(e));
		if (PathStep.countSame(txtPath, path) >= txtPath.size() * 0.85) nodes.add(e);
		else {
			for (Element c: e.children()) collect(es, c, txtPath, path, depth+1, nodes);
		}
		
		path.remove(path.size()-1);
	}
	
	public static class NodeData {
		public UsefullText user;
		public UsefullText date;
		public int nPost;
	}
	
	public static class UsefullText {
		private String text;
		private int score;
		
		public UsefullText(String text, int score) {
			this.text = text;
			this.score = score;
		}

		public void max(UsefullText o) {
			if (o.score > score) {
				text = o.text;
				score = o.score;
			}
		}

		public String getText() {
			return text;
		}

		public int getScore() {
			return score;
		}
	}
}