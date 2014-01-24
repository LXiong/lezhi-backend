package com.buzzinate.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.buzzinate.lezhi.api.Client;
import org.buzzinate.lezhi.api.Doc;
import org.buzzinate.lezhi.api.HitDoc;
import org.buzzinate.lezhi.api.UrlTime;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserWeiboDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.UserWeibo;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.MentionUtil;
import com.buzzinate.keywords.Keyword;
import com.buzzinate.keywords.MobileKeywordsExtractor;
import com.buzzinate.main.MyModule;
import com.buzzinate.nlp.util.DictUtil;
import com.buzzinate.nlp.util.DoublePriorityQueue;
import com.google.code.morphia.Datastore;
import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestSearch {

	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Client client = injector.getInstance(Client.class);
		Datastore ds = injector.getInstance(Datastore.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		PreferenceDao prefDao = new PreferenceDao(ds);
		UserWeiboDao uwDao = new UserWeiboDao(ds);
		
		int[] fieldboost = new int[] {1, 2, 6, 5};
	
		List<Long> leziUserIds = Arrays.asList(11604L);
//		List<Long> leziUserIds = userDao.findLeziUserIds();
//		List<Long> leziUserIds = Arrays.asList(11604L, 12377L, 49449L, 89342L, 56719L);
//		List<Long> leziUserIds = Arrays.asList(12071L, 77926L, 188835L);
		long since = System.currentTimeMillis() - Constants.ONE_DAY * 200;
		for (long userId: leziUserIds) {
			ArrayListMultimap<Long, HitDoc> id2docs = ArrayListMultimap.create();
			
			System.out.println("Processing user " + userId);
			List<UserWeibo> weibos = uwDao.findLatest(userId, 200);
			List<String> texts =  new ArrayList<String>();
			for (UserWeibo weibo: weibos) {
				String text = MentionUtil.cleanStatusText(weibo.getText());
				texts.add(text);
			}
			
			List<Keyword> keywords = MobileKeywordsExtractor.extract(texts, 30);
			System.out.println(keywords);
			List<String> kws = new ArrayList<String>();
			for (Keyword keyword: keywords) {
				double idf = DictUtil.splitIdf(keyword.word());
				kws.add(String.format("%s|%s,%s,%s", StringUtils.replace(keyword.word(), " ", "_"), keyword.freq(), keyword.field(), idf));
			}
			List<HitDoc> docs = client.query(null, StringUtils.join(kws, " "), since, 20, 100);
			normalize(docs);
			for (HitDoc doc: docs) {
				id2docs.put(doc.id, doc);
//				System.out.println("\t" + doc);
			}
			
			List<Long> myprefs = prefDao.findLatestItems(userId, 100);
			List<UrlTime> uts = new ArrayList<UrlTime>();
			for (Article a: articleDao.createQuery().retrievedFields(true, "url", "createAt").filter("_id in", myprefs).asList()) {
				uts.add(new UrlTime(a.getUrl(), a.getCreateAt()));
			}
			
			for (Doc doc: client.get(uts).values()) {
				List<String> hkws = new ArrayList<String>();
				for (String part: StringUtils.split(doc.keyword, " ")) {
					String word = StringUtils.substringBefore(part, "|");
					double idf = DictUtil.splitIdf(StringUtils.replace(word, "_", " "));
					hkws.add(part + "," + idf);
				}
				List<HitDoc> hdocs = client.query(doc.signature, StringUtils.join(hkws, " "), since, 15, 100);
				normalize(hdocs);
				for (HitDoc hd: hdocs) {
					id2docs.put(hd.id, hd);
//					System.out.println("\t" + doc);
				}	
			}
						
			DoublePriorityQueue<HitDoc> pq = new DoublePriorityQueue<HitDoc>(200);
			for (Map.Entry<Long, Collection<HitDoc>> e: id2docs.asMap().entrySet()) {
				HitDoc doc = e.getValue().iterator().next();
				List<Float> scores = new ArrayList<Float>();
				for (HitDoc hd: e.getValue()) scores.add(hd.score);
				Collections.sort(scores);
				Collections.reverse(scores);
				float total = 0;
				for (int i = 0; i < scores.size() && i < 4; i++) total += scores.get(i);
				doc.score = total;
				pq.add(total, doc);
			}
			
			for (HitDoc hd: pq.values()) System.out.println(hd);
		}
		client.close();
	}
	
	private static void normalize(List<HitDoc> docs) {
//		float maxscore = 0;
//		for (HitDoc hd: docs) {
//			if (maxscore < hd.score) maxscore = hd.score;
//		}
//		for (HitDoc hd: docs) hd.score = hd.score / maxscore;
	}
}