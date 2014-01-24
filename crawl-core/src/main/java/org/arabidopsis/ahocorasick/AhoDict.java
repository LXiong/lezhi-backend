package org.arabidopsis.ahocorasick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AhoDict<E> {
	private AhoCorasick<Element<E>> tree = new AhoCorasick<Element<E>>();
	
	public static void main(String[] args) {
		AhoDict<String> dict = new AhoDict<String>();
		dict.put("中国", "中国1");
		dict.put("中国", "中国2");
		dict.put("站起", "站起");
		dict.prepare();
		
		List<Match<String>> results = dict.search("从此中国人民站起来了");
		for (Match<String> m: results) {
			System.out.println(m.getOffset() + " ==> " + m.getValue());
		}
	}
	
	public void put(String key, E value) {
		tree.add(key.getBytes(), new Element<E>(key, value));
	}
	
	public void prepare() {
		tree.prepare();
	}
	
	public List<Match<E>> search(String text) {
		int len = text.length();
		int[] offset = new int[len];
		int off = 0;
		for (int i = 0; i < text.length(); i++) {
			offset[i] = off;
			byte[] cbs = text.substring(i, i + 1).getBytes();
			off += cbs.length;
		}

		List<Match<E>> result = new ArrayList<Match<E>>();
		Iterator<SearchResult<Element<E>>> searcher = tree.search(text.getBytes());
		while (searcher.hasNext()) {
			SearchResult<Element<E>> sr = searcher.next();
			int idx = Arrays.binarySearch(offset, sr.getLastIndex());
			for (Element<E> o : sr.getOutputs()) {
				result.add(new Match<E>(idx - o.getKey().length(), o.getValue()));
			}
		}
		return result;
	}
	
	private static class Element<E> {
		private String key;
		private E value;
		
		public Element(String key, E value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public E getValue() {
			return value;
		}
	}
	
	public static class Match<V> {
		private int offset;
		private V value;
		
		public Match(int offset, V value) {
			this.offset = offset;
			this.value = value;
		}

		public int getOffset() {
			return offset;
		}

		public V getValue() {
			return value;
		}
	}
}