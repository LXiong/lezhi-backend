package com.buzzinate.crawl.simhash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ArticleProfileDao;
import com.buzzinate.common.dao.mongo.ArticleDaoImpl;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.ArticleProfile;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.crawl.core.util.TextUtility;
import com.buzzinate.main.MyModule;
import com.buzzinate.util.MurmurHash;
import com.google.code.morphia.Datastore;
import com.google.common.collect.HashMultimap;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class SimHash {
	private static Logger log = Logger.getLogger(SimHash.class);
	private static final int NGRAM_SIZE = 3;
	
	private static MurmurHash hash = new MurmurHash();
	
	public static void main(String[] args) throws IOException {
//		System.out.println(createNgram("在“我行贿了网”成功备案前4天，另一家同类型网站“我行贿网站”也成功备案。2011, 更早些时候，“我贿赂了中文网”及另一家“我贿赂了“（www.wohuilule.com）于6月24日备案成功。", 2));
		checkDuplicate(1000);
	}
	
	public static void checkDuplicate(int max) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		ArticleDao articleDao = new ArticleDaoImpl(injector.getInstance(Datastore.class));
		
		List<Article> pops = articleDao.createQuery().filter("status", Status.OK).order("-score").limit(max).asList();
		HashMap<Long, Article> id2as = new HashMap<Long, Article>();
		HashMultimap<Integer, ArticleProfile> m2mh = HashMultimap.create();
		for (Article a: pops) {
			id2as.put(a.getId(), a);
			List<Integer> mh = calcMinHash(Jsoup.parse(a.getContent()).body().text());
			ArticleProfile ap = new ArticleProfile();
			ap.setId(a.getId());
			ap.setMinhashes(mh);
			m2mh.put(ap.getMinhash(), ap);
		}
		for (Map.Entry<Integer, Collection<ArticleProfile>> e: m2mh.asMap().entrySet()) {
			if (e.getValue().size() <= 1) continue;
			System.out.println(e.getKey() + " ====== ");
			List<ArticleProfile> mhs = new ArrayList<ArticleProfile>(e.getValue());
			for (int i = 0; i < mhs.size(); i++) {
				ArticleProfile mh = mhs.get(i);
				System.out.println(mh.getTitle() + " ===> " + mh.getMinhashes());
				for (int j = i+1; j < mhs.size(); j++) {
					ArticleProfile dmh = mhs.get(j);
					double sim = getJaccard(mh.getMinhashes(), dmh.getMinhashes());
					if (sim >= 0.6) {
						System.out.println("duplicate " + sim + ": " + dmh.getTitle() + " ===> " + dmh.getMinhashes());
					}
					else if (sim >= 0.3) {
						sim = getJaccard(mh.getTitle(), dmh.getTitle());
						if (sim >= 0.8) System.out.println("duplicate title " + sim + ": " + dmh.getTitle() + " ===> " + dmh.getMinhashes());
					}
				}
			}
			System.out.println("===========");
		}
	}
	
	/**
	 * 检查之前的内容重复的文章，如果有，把本文章标为重复
	 * 
	 * @param page
	 */
	public static boolean isDuplicate(ArticleProfileDao apDao, String title, List<Integer> minHashes) {
		if (minHashes.size() > 0) {
			List<ArticleProfile> aps = apDao.findByMinHash(minHashes.get(0));
			for (ArticleProfile oap: aps) {
				if (SimHash.getJaccard(minHashes, oap.getMinhashes()) >= 0.6) {
					log.info("!!! Duplicate: " + title + " <==> " + oap.getTitle() + "/" + oap.getId());
					return true;
				}
			}
		}
		return false;
	}
	
	public static float getJaccard(String text, String otherText) {
		HashSet<String> ngrams = createNgram(text, NGRAM_SIZE);
		HashSet<String> otherNgrams = createNgram(otherText, NGRAM_SIZE);
		int total = ngrams.size() + otherNgrams.size();
		otherNgrams.retainAll(ngrams);
		return otherNgrams.size() * 1f / (total - otherNgrams.size());
	}

	public static float getJaccard(List<Integer> mh1, List<Integer> mh2) {
		HashSet<Integer> common = new HashSet<Integer>();
		common.addAll(mh1);
		common.retainAll(mh2);
		
		return common.size() * 1f / Math.max(mh1.size(), mh2.size());
	}
	
	public static List<Integer> calcMinHash(String text) {
		HashSet<String> ngrams = createNgram(text, NGRAM_SIZE);
		return calcMinHash(ngrams);
	}

	public static List<Integer> calcMinHash(HashSet<String> set) {
		PriorityQueue<Integer, Integer> minpq = PriorityQueue.make(10, SortUtils.comp(Integer.class), SortUtils.comp(Integer.class));
		for (String s: set) {
			long h = hash.hash(s) & 0x7FFFFFFF;
			minpq.add((int)h, (int)h);
		}
		return minpq.values();
	}

	public static HashSet<String> createNgram(String text, int n) {
		HashSet<String> tokens = new HashSet<String>();
		List<List<String>> sentences = TextUtility.splitSentenceSet(text);
		for (List<String> sentence: sentences) {
			for (int i = 0; i < sentence.size(); i++) {
				if (isDigit(sentence.get(i))) sentence.set(i, "#");
			}
			for (int i = 0; i + n < sentence.size(); i++) {
				String token = StringUtils.join(sentence.subList(i, i+n), "");
				tokens.add(token);
			}
		}
		return tokens;
	}

	private static boolean isDigit(String word) {
		return Character.isDigit(word.charAt(0));
	}
}
