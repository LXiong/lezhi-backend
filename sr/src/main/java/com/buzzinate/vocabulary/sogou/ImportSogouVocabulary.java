package com.buzzinate.vocabulary.sogou;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class ImportSogouVocabulary {
	private static List<String> importantCategories = Arrays.asList("电影", "节日", "IMDB", "篮球", "足球", "军事");
	
	public static void main(String[] args) throws IOException {	
		File dictDir = new File("extDict");
		BufferedWriter bw = new BufferedWriter(new FileWriter("src/main/resources/vocs"));
		for (File dictFile: dictDir.listFiles()) {
			String category = dictFile.getName();
			category = StringUtils.substringBefore(category, ".scel");
			category = StringUtils.substringBefore(category, "【官方推荐】");
			category = StringUtils.substringBefore(category, "大全");
			category = StringUtils.substringBefore(category, "词汇");
			category = StringUtils.substringBefore(category, "Top");
			category = category.trim();
			System.out.println(category);
			
			int check = 1;
			for (String c: importantCategories) {
				if (category.contains(c)) check = 0;
			}
			List<String> words = SogouScelReader.readWords(dictFile);
			for (String word: words) {
				word = StringUtils.substringBefore(word, ",");
				bw.append(word + " \t" + category + "\t" + check + "\n");
			}
			bw.flush();
		}
		bw.close();
	}
}