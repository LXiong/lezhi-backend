package com.buzzinate.crawl.core.tag;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.buzzinate.crawl.core.util.PorterStemmer;

public class WordUtil {
	private static Set<String> stopwords = new HashSet<String>();
	
	private static PorterStemmer stemmer = new PorterStemmer();
	
	static {
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("stopword.txt");
			String content = IOUtils.toString(is);
			String[] lines = StringUtils.split(content, "\n");
			for (String line: lines) {
				line = line.trim();
				if (line.length() > 0) {
					stopwords.add(stemmer.stem(line));
				}
			}
		} catch (Exception e) {
			IOUtils.closeQuietly(is);
		}
	}
	
	public static boolean isUseful(String word) {
		return !isStopword(word) && hasLetter(word);
	}
	
	public static boolean isStopword(String word) {
		return stopwords.contains(stemmer.stem(word));
	}
	
	public static boolean hasLetter(String word) {
		for (char ch: word.toCharArray()) if (Character.isLetter(ch)) return true;
		return false;
	}
	
	public static void main(String[] args) {
		System.out.println(isStopword("an"));
		System.out.println(isStopword("the"));
		System.out.println(isStopword("p2p"));
	}
}