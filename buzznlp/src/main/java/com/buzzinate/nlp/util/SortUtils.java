package com.buzzinate.nlp.util;

import java.util.Comparator;

public class SortUtils {
	private SortUtils() {}
	
	public static <T extends Comparable<T>> Comparator<T> comp(Class<T> clazz) {
		return new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				return o1.compareTo(o2);
			}
		};
	}
	
	public static <T extends Comparable<T>> Comparator<T> reverse(Class<T> clazz) {
		return new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				return -o1.compareTo(o2);
			}
		};
	}
	
	public static class Pair<K, V> {
		private K key;
		private V value;
		
		public Pair(K k, V v) {
			this.key = k;
			this.value = v;
		}
		
		public static <K, V> Pair<K, V> make(K k, V v) {
			return new Pair<K, V>(k, v);
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		@Override
		public String toString() {
			return key + "," + value;
		}
	}
}