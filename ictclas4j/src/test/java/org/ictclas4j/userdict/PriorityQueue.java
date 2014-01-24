package org.ictclas4j.userdict;

import java.util.TreeMap;

@SuppressWarnings("serial")
public class PriorityQueue extends TreeMap<Double, String> {
	private int max = 0;
	
	public PriorityQueue(int max) {
		this.max = max;
	}

	@Override
	public String put(Double key, String value) {
		if (size() < max) return super.put(key, value);
		else {
			if (key < lastKey()) {
				pollLastEntry();
				return super.put(key, value);
			} else return null;
		}
	}
	
	public static void main(String[] args) {
		PriorityQueue pq = new PriorityQueue(2);
		pq.put(1.0, "1.0");
		pq.put(10.0, "10.0");
		pq.put(2.0, "2.0");
		pq.put(5.0, "5.0");
		System.out.println(pq);
	}
}