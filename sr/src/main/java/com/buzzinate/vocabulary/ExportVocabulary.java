package com.buzzinate.vocabulary;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.buzzinate.common.dao.VocabularyDao;
import com.buzzinate.common.model.Vocabulary;
import com.buzzinate.crawl.core.util.TextUtility;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ExportVocabulary {
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		VocabularyDao vocDao = new VocabularyDao(ds);
		
		PrintWriter pw = new PrintWriter(new FileWriter("userDict.txt"));
		
		for (Vocabulary voc: vocDao.createQuery().asList()) {
			if (!TextUtility.isFirstAscii(voc.getWord()) && !allDigit(voc.getWord())) pw.println(voc.getWord());
		}
		pw.flush();
		pw.close();
	}
	
	private static boolean allDigit(String word) {
		boolean ad = true;
		for (int i = 0; i < word.length(); i++) {
			char ch = word.charAt(i);
			if (ch < '0' || ch > '9') ad = false;
		}
		return ad;
	}
}