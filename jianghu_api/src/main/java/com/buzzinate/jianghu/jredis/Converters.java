package com.buzzinate.jianghu.jredis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Converters {
	public static Converter<String> STRING = new Converter<String>() {
		@Override
		public String toString(String value) {
			return value;
		}

		@Override
		public String fromString(String str) {
			return str;
		}
	};

	public static Converter<Long> LONG = new Converter<Long>() {
		@Override
		public String toString(Long value) {
			return String.valueOf(value);
		}

		@Override
		public Long fromString(String str) {
			return Long.parseLong(str);
		}
	};
	
	public static Converter<Integer> INTEGER = new Converter<Integer>() {
		@Override
		public String toString(Integer value) {
			return String.valueOf(value);
		}

		@Override
		public Integer fromString(String str) {
			return Integer.parseInt(str);
		}
	};
	
	public static Converter<List<Long>> LONG_LIST = new ListConverter<Long>(LONG);
	
	public static class ListConverter<T> implements Converter<List<T>> {
		private Converter<T> unitConverter;
		
		public ListConverter(Converter<T> unitConverter) {
			this.unitConverter = unitConverter;
		}

		@Override
		public String toString(List<T> values) {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (T value: values) {
				if (first) first = false;
				else sb.append(",");
				sb.append(unitConverter.toString(value));
			}
			return sb.toString();
		}

		@Override
		public List<T> fromString(String str) {
			List<T> result = new ArrayList<T>();
			if (str.isEmpty()) return result;
			String[] es = str.split(",");
			for (String e: es) result.add(unitConverter.fromString(e));
			return result;
		}
	}
	
	public static Converter<PrefData> PREF_DATA = new Converter<PrefData>() {

		@Override
		public String toString(PrefData p) {
			return p.getId() + "," + StringUtils.join(p.getWords(), "&") + "," + p.getCreateAt() + "," + p.getSize();
		}

		@Override
		public PrefData fromString(String str) {
			String[] parts = str.split(",");
			long id = Long.parseLong(parts[0]);
			List<String> words = new ArrayList<String>();
			for (String word: parts[1].split("&")) words.add(word);
			long createAt = Long.parseLong(parts[2]);
			int size = Integer.parseInt(parts[3]);
			return new PrefData(id, words, createAt, size);
		}
		
	};
}