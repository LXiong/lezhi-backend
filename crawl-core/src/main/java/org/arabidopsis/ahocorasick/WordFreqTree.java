package org.arabidopsis.ahocorasick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.buzzinate.crawl.core.util.PorterStemmer;

public class WordFreqTree {
	private AhoCorasick<String> tree;
	private PorterStemmer stemmer = new PorterStemmer();
	
	public WordFreqTree() {
		this.tree = new AhoCorasick<String>();
	}
	
	public void add(String word) {
		tree.add(fill(word).getBytes(), word);
	}
	
	public void build() {
		tree.prepare();
	}
	
	private String fill(String word) {
		word = stemAll(word.trim());
		int type = Character.getType(word.charAt(0));
		if (type == Character.LOWERCASE_LETTER || type == Character.UPPERCASE_LETTER) word = " " + word;
		type = Character.getType(word.charAt(word.length() - 1));
		if (type == Character.LOWERCASE_LETTER || type == Character.UPPERCASE_LETTER) word = word + " ";
		return word;
	}
	
	public String searchFirst(String text) {
		text = fillContent(text);
		Iterator<SearchResult<String>> s = tree.search(text.getBytes());
		if (s.hasNext()) {
			SearchResult<String> sr = s.next();
			Set<String> outputs = sr.getOutputs();
			if (outputs.size() > 0) return outputs.toArray(new String[0])[0];
		}
		return null;
	}
	
	public String searchMax(String text) {
		text = fillContent(text);
		Iterator<SearchResult<String>> s = tree.search(text.getBytes());
		String r = null;
		int maxLen = 0;
		while (s.hasNext()) {
			SearchResult<String> sr = s.next();
			Set<String> words = sr.getOutputs();
			for (String word:words) {
				if (word.length() > maxLen) {
					maxLen = word.length();
					r = word;
				}
			}
		}
		return r;
	}
	
	private String stemAll(String text) {
		StringBuffer sb = new StringBuffer();
		int last = 0;
		boolean prevAscii = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			boolean ascii = isAscii(ch);
			if (prevAscii && !ascii) {
				sb.append(stemmer.stem(text.substring(last, i)));
				last = i;
			}
			prevAscii = ascii;
		}
		if (last < text.length()) sb.append(stemmer.stem(text.substring(last, text.length())));
		return sb.toString();
	}
	
	private String fillContent(String text) {
		text = stemAll(text);
		StringBuffer sb = new StringBuffer();
		boolean prevLetter = false;
		for (int i = 0; i < text.length(); i++) {
			boolean isletter = isLetter(text.charAt(i));
			if (prevLetter != isletter) sb.append(" ");
			sb.append(text.charAt(i));
			prevLetter = isletter;
		}
		sb.append(" ");
		return sb.toString().toLowerCase();
	}
	
	private static boolean isAscii(char ch) {
		int type = Character.getType(ch);
		return type == Character.LOWERCASE_LETTER || type == Character.UPPERCASE_LETTER || ch == '-';
	}
	
	private static boolean isLetter(char ch) {
		int type = Character.getType(ch);
		return type == Character.LOWERCASE_LETTER || type == Character.UPPERCASE_LETTER || Character.isWhitespace(ch) || ch == '-';
	}
	
	public List<String> search(String text) {
		text = fillContent(text);
		List<String> words = new ArrayList<String>();
		Iterator<SearchResult<String>> s = tree.search(text.getBytes());
		while (s.hasNext()) {
			SearchResult<String> sr = s.next();
			words.addAll(sr.getOutputs());
		}
		
		return words;
	}
	
	public static void main(String[] args) {
		WordFreqTree wft = new WordFreqTree();
		wft.add("算法");
		wft.add("程序");
		wft.build();
		List<String> ws = wft.search("预算法去年首次修订的重点是，将游离于预算法之外的政府收支尽可能纳入预算，对预算公开和透明提出法定要求，规范超收收入的使用，明确地方政府发行债券的权限，提升人大的审查监督以及对预算违法违规的问责等。现行预算法于1994年3月22日在八届全国人大二次会议通过，次年1月1日起正式施行。去年11月16日，国务院常务会讨论通过预算法修正案（草案）。当年12月，全国人大常委会会议“一审”国务院提请审议的预算法修正案草案。对具体项目的预算收支，会议期间按程序提出的修正案，大会应在整体表决前，先对预算修正案逐项表决，最后再整体表决。");
		System.out.println(ws);
	}
}
