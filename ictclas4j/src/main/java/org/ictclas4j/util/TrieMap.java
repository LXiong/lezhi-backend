package org.ictclas4j.util;

public class TrieMap<E> {
	private Node<E> root = new Node<E>('$', null);
	
	public static void main(String[] args) {
		TrieMap<String> m = new TrieMap<String>();
		m.put("a", "abc - a");
		m.put("ab", "abc - ab");
		m.put("abc", "abc - abc");
		
		String key = "abcd";
		Node<String> r = m.getRoot();
		for (int i = 0; i < key.length(); i++) {
			r = m.search(r, String.valueOf(key.charAt(i)));
			if (r == null) break;
			System.out.println(key.substring(0, i+1) + " ==> " + r.value);
		}
		System.out.println("get ab: " + m.get("ab"));
	}
	
	public E get(String key) {
		Node<E> n = root;
		for (int i = 0; i < key.length(); i++) {
			n = n.searchChild(key.charAt(i));
			if (n == null) break;
		}
		if (n != null) return n.value;
		else return null;
	}
	
	public Node<E> search(String key) {
		return search(root, key);
	}
	
	public Node<E> search(Node<E> node, String nextStr) {
		Node<E> result = node;
		for (int i = 0; i < nextStr.length(); i++) {
			result = result.searchChild(nextStr.charAt(i));
			if (result == null) break;
		}
		return result;
	}
	
	public Node<E> getRoot() {
		return root;
	}

	public void put(String key, E value) {
		putR(root, key, 0, value);
	}

	private void putR(Node<E> node, String key, int i, E value) {
		char ch = key.charAt(i);
		Node<E> next = node.searchChild(ch);
		if (next == null) {
			next = new Node<E>(ch, null);
			node.addChild(next);
		}
		if (i == key.length()-1) next.value = value;
		if (i < key.length()-1) putR(next, key, i+1, value);
	}

	public static class Node<E> {
		private char ch;
		private E value;
		private CharHashMap<Node<E>> childen = new CharHashMap<Node<E>>();
		
		public Node(char ch, E value) {
			this.ch = ch;
			this.value = value;
		}
		
		public char getChar() {
			return ch;
		}
		
		public E getValue() {
			return value;
		}
		
		public Node<E> searchChild(char nextChar) {
			return childen.get(nextChar);
		}
		
		public void addChild(Node<E> child) {
			childen.put(child.ch, child);
		}
	}
}