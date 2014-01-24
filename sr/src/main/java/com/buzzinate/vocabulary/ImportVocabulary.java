package com.buzzinate.vocabulary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.buzzinate.common.dao.VocabularyDao;
import com.buzzinate.common.model.Vocabulary;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ImportVocabulary {
	
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		VocabularyDao vocDao = new VocabularyDao(ds);
		
		importVoc(vocDao);
	}

	public static void importVoc(VocabularyDao vocDao) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("vocs");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\t");
			if (parts.length < 3) continue;
			String word = parts[0].trim();
			if (word.length() < 2) continue;
			
			String category = parts[1].trim();
			
			Vocabulary voc = new Vocabulary(word, category, parts[2].equals("0"));
			vocDao.save(voc);
		}
		br.close();
		
		ExtWords[] extwords = new ExtWords[]{
			new ExtWords("星座", Arrays.asList("白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"))
		};
		for (ExtWords ew: extwords) {
			for (String word: ew.getWords()) {
				Vocabulary voc = new Vocabulary(word, ew.getCategory(), true);
				vocDao.save(voc);
			}
		}
	}
}

class ExtWords {
	private String category;
	private List<String> words;
	
	public ExtWords(String category, List<String> words) {
		this.category = category;
		this.words = words;
	}

	public String getCategory() {
		return category;
	}

	public List<String> getWords() {
		return words;
	}
}