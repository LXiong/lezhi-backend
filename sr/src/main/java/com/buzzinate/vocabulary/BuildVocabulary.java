package com.buzzinate.vocabulary;

import java.io.IOException;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.VocabularyDao;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class BuildVocabulary {
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		final VocabularyDao vocDao = new VocabularyDao(ds);
		
		ImportVocabulary.importVoc(vocDao);
		ExtractVocabulary.extractVoc(articleDao, vocDao);
		UpdateVocabularyFreq.updateVocFreq(articleDao, vocDao);
	}
}