package com.buzzinate.vocabulary;

import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.map.OpenObjectIntHashMap;
import org.arabidopsis.ahocorasick.WordFreqTree;
import org.ictclas4j.bean.DictLib;
import org.ictclas4j.bean.Pos;
import org.ictclas4j.segment.Segment;
import org.jsoup.Jsoup;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.VocabularyDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.model.Vocabulary;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.PriorityQueue.Entry;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.crawl.core.text.ExtractNNP;
import com.buzzinate.crawl.core.util.ExtractUtils;
import com.buzzinate.crawl.core.util.TextUtility;
import com.buzzinate.main.MyModule;
import com.buzzinate.util.DomainNames;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ExtractVocabulary {
	private static final String NEW = "网络新词";
	private static final int MAX_DOCS = 50000;
	private static final int MAX_WORDS = 25000;

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		VocabularyDao vocDao = new VocabularyDao(ds);
		
		extractVoc(articleDao, vocDao);
	}

	public static void extractVoc(ArticleDao articleDao, VocabularyDao vocDao) {
		final Segment seg = new Segment(new DictLib(false));
		TreeSet<String> wordsites = new TreeSet<String>();
		
		for (Category c: Category.values()) {
			List<Article> as = articleDao.createQuery().retrievedFields(true, "title", "category", "keywords", "content", "url").filter("category", c).filter("status", Status.OK).order("-score").limit(MAX_DOCS).asList();
			for (Article a: as) {
				String title = a.getTitle();
				List<String> texts = ExtractUtils.splitSentences(Jsoup.parse(a.getContent()).body());
				List<String> words = ExtractNNP.extractNewWords(title, texts);
				for (String kw: a.getKeywords().split(",")) {
					String w = ExtractNNP.stemAll(kw.trim());
					if (w.length() > 0 && ExtractNNP.isAllLetterOrSpace(w) && Character.isLetter(w.charAt(0))) {
					  if (TextUtility.countNumWords(w) <= 6) words.add(w);
					}
				}
				OpenObjectIntHashMap<String> wordCnt = new OpenObjectIntHashMap<String>();
				for (String word: words) {
					if (word.length() >= 3) wordCnt.adjustOrPutValue(word, 1, 1);
				}
				final PriorityQueue<Integer, String> twpq = PriorityQueue.make(wordCnt.size() / 2, SortUtils.reverse(Integer.class), SortUtils.reverse(String.class));
				wordCnt.forEachPair(new ObjectIntProcedure<String>(){
					@Override
					public boolean apply(String word, int freq) {
						boolean isNoun = true;
						for (Pos pos: seg.getPoses(word)) {
							if (!pos.toString().startsWith("n")) isNoun = false;
						}
						if (isNoun) twpq.add(freq * 10 + word.length(), word);
						return true;
					}
				});
				String site = DomainNames.safeGetPLD(a.getUrl());
				for (String word: twpq.values()) {
					if (word.length() < 20) wordsites.add(word.toLowerCase() + "#" + site);
				}
			}
		}
		
		PriorityQueue<Double, String> topq = PriorityQueue.make(MAX_WORDS * 6 / 5, SortUtils.reverse(Double.class), SortUtils.reverse(String.class));
		String prev = "";
		int cnt = 0;
		for (String ws: wordsites) {
			String word = StringUtils.substringBefore(ws, "#");
			if (!prev.equals(word)) {
				if (cnt > 0) {
					double score = cnt + Math.log(prev.length());
					topq.add(score, prev);
				}
				prev = word;
				cnt = 0;
			}
			cnt++;
		}
		
		WordFreqTree wft = new WordFreqTree();
		for (String word: topq.values()) {
			wft.add(word);
		}
		wft.build();
		
		OpenObjectIntHashMap<String> wordCnt = new OpenObjectIntHashMap<String>();
		for (Category c: Category.values()) {
			List<Article> as = articleDao.createQuery().retrievedFields(true, "title", "category", "keywords", "content", "url").filter("category", c).filter("status", Status.OK).order("-score").limit(MAX_DOCS).asList();
			for (Article a: as) {
				HashSet<String> words = new HashSet<String>();
				words.addAll(wft.search(a.getTitle().toLowerCase()));
				String content = Jsoup.parse(a.getContent()).body().text();
				words.addAll(wft.search(content.toLowerCase()));
				for (String word: words) wordCnt.adjustOrPutValue(word, 1, 1);
			}
		}
		
		final PriorityQueue<Double, String> ftopq = PriorityQueue.make(MAX_WORDS, SortUtils.reverse(Double.class), SortUtils.reverse(String.class));
		wordCnt.forEachPair(new ObjectIntProcedure<String>(){
			@Override
			public boolean apply(String word, int freq) {
				double idf = Math.log(MAX_DOCS / (1.0 + freq));
				ftopq.add(idf, word);
				return true;
			}
		});
		
		for (Entry<Double, String> e: ftopq.entries()) {
			System.out.println(e.getValue() + " ==> idf: " + e.getKey());
			Vocabulary voc = new Vocabulary(e.getValue(), NEW, false);
			vocDao.save(voc);
		}
	}
}