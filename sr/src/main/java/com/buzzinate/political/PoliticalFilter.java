package com.buzzinate.political;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.arabidopsis.ahocorasick.WordFreqTree;

public class PoliticalFilter {
	private static Logger log = Logger.getLogger(PoliticalFilter.class);
	
	private static WordFreqTree pks = getPoliticalKeywords();
	
	public static boolean hasPoliticalKeywords(String text) {
		if (text == null) text = "";
		String word = pks.searchFirst(text);
		if (word != null) log.info("political word: " + word);
		return word != null;
	}
	
	public static void main(String[] args) {
		System.out.println(hasPoliticalKeywords("asdfa宝来利科技sfsdf"));
	}

	private static WordFreqTree getPoliticalKeywords() {
		List<String> keywords = loadPoliticalKeywords();
		WordFreqTree wft = new WordFreqTree();
		for (String keyword: keywords) wft.add(keyword);
		wft.build();
		return wft;
	}

	public static List<String> loadPoliticalKeywords() {
		List<String> keywords = new ArrayList<String>();
		BufferedReader br = null;
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("political.txt");
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				keywords.add(line);
			}
			return keywords;
		} catch (IOException e) {
			e.printStackTrace();
			return keywords;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
			}
		}
	}
}