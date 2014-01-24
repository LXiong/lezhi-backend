package com.buzzinate.vocabulary;

import java.util.HashSet;
import java.util.List;

import org.apache.mahout.math.map.OpenObjectIntHashMap;
import org.arabidopsis.ahocorasick.WordFreqTree;
import org.ictclas4j.bean.DictLib;
import org.jsoup.Jsoup;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.VocabularyDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.model.Vocabulary;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class UpdateVocabularyFreq {
	private static final int MAX_ITE = 100;

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		final VocabularyDao vocDao = new VocabularyDao(ds);
		
		updateVocFreq(articleDao, vocDao);
	}

	public static void updateVocFreq(ArticleDao articleDao, final VocabularyDao vocDao) {
		List<Vocabulary> vocs = vocDao.createQuery().asList();
		WordFreqTree wft = new WordFreqTree();
		for (Vocabulary voc: vocs) {
			wft.add(voc.getWord());
		}
		wft.build();
		
		OpenObjectIntHashMap<String> docFreqs = new OpenObjectIntHashMap<String>();
		long endId = articleDao.findMaxId() + 1;
		for (int ite = 0; ite < MAX_ITE; ite++) {
			System.out.println("Iteration " + ite + " **********");
			List<Article> as = articleDao.createQuery().retrievedFields(true, "title", "content").filter("status", Status.OK).filter("_id <", endId).order("-_id").limit(10000).asList();
			for (Article a: as) {
				String title = a.getTitle();
				String content = Jsoup.parse(a.getContent()).body().text();
				HashSet<String> words =  new HashSet<String>();
				words.addAll(wft.search(title.toLowerCase()));
				words.addAll(wft.search(content.toLowerCase()));
				for (String word: words) {
					docFreqs.adjustOrPutValue(word, 1, 1);
				}
				if (endId > a.getId()) endId = a.getId();
			}
		}
				
		DictLib dictLib = new DictLib(false);
		System.out.println("Update voc freqes ...");
		for (Vocabulary voc: vocs) {
			int docFreq = docFreqs.get(voc.getWord());
			voc.setDocFreq(docFreq);
			voc.setPossibility(VocUtil.computePossibility(dictLib, voc.getWord()));
			System.out.println(voc.getWord() + " ==> " + docFreq + ", " + voc.getPossibility());
			vocDao.save(voc);
		}
	}
}