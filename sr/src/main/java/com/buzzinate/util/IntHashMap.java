package com.buzzinate.util;

import java.util.HashMap;

public class IntHashMap<K> extends HashMap<K, Integer> {

	
	public void adjustOrPut(K key, Integer incr, Integer defValue){
		      Integer value = get(key);
		      if (value == null) {
		    	put(key, defValue);
		      } else {
		        put(key, value + incr);
		      }
		    }
	
	public static void main(String[] args){
		IntHashMap<String> test = new IntHashMap<String>();
		test.adjustOrPut("apple", 1, 5);
		test.adjustOrPut("apple", 1, 5);
		test.adjustOrPut("apple", 1, 5);
		System.out.println(test.get("apple"));
	}
}
