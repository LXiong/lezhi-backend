package com.buzzinate.batch;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.model.User;
import com.buzzinate.crawl.core.util.Counter;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class StatData {
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		UserDao userDao = injector.getInstance(UserDao.class);
		PreferenceDao prefDao = new PreferenceDao(injector.getInstance(Datastore.class));
		
		long maxId = articleDao.findMaxId();
		long start = 0;
		Counter<String> articleCnt = new Counter<String>();
		while (start <= maxId) {
			List<Article> as = articleDao.createQuery().retrievedFields(true, "createAt").filter("_id >=", start).filter("_id <", start + nBatch).asList();
			for(Article a: as) {
				articleCnt.add(formatTime(a.getCreateAt()));
			}
			start += nBatch;
		}
		
		maxId = userDao.findMaxId();
		start = 0;
		Counter<String> userCnt = new Counter<String>();
		HashMap<Long, Long> minTimes = new HashMap<Long, Long>();
		while (start <= maxId) {
			List<Preference> prefs = prefDao.createQuery().retrievedFields(true, "createAt").filter("userId >=", start).filter("userId <", start + nBatch).asList();
			for(Preference p: prefs) {
				Long mt = minTimes.get(p.getUserId());
				if (mt == null || mt > p.getCreateAt()) mt = p.getCreateAt();
				minTimes.put(p.getUserId(), mt);
			}
			for (Map.Entry<Long, Long> e: minTimes.entrySet()) userCnt.add(formatTime(e.getValue()));
			start += nBatch;
		}
		
		TreeMap<String, Stat> stats = new TreeMap<String, Stat>(); 
		for (Map.Entry<String, Integer> e: articleCnt.toMap().entrySet()) {
			Stat s = stats.get(e.getKey());
			if (s == null) s = new Stat();
			s.nArticles = e.getValue();
			stats.put(e.getKey(), s);
		}
		
		for (Map.Entry<String, Integer> e: userCnt.toMap().entrySet()) {
			Stat s = stats.get(e.getKey());
			if (s == null) s = new Stat();
			s.nUsers = e.getValue();
			stats.put(e.getKey(), s);
		}
		
		PrintWriter pw = new PrintWriter(new FileWriter("statdata.csv"));
		pw.println("日期, 文章数, 用户数");
		for (Map.Entry<String, Stat> e: stats.entrySet()) {
			pw.println(e.getKey() + ", " + e.getValue().nArticles + ", " + e.getValue().nUsers);
			pw.flush();
		}
		pw.close();
	}
	
	private static final int nBatch = 100;
	private static final DateFormat mf = new SimpleDateFormat("yyyy-MM");
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static final long thisMonthStart = getMonthStart();
	private static long getMonthStart() {
		try {
			return mf.parse(mf.format(new Date())).getTime();
		} catch (ParseException e) {
			return -1;
		}
	}
	
	private static String formatTime(long t) {
		if (t > thisMonthStart) return df.format(new Date(t));
		else return mf.format(new Date(t));
	}
	
	private static class Stat {
		public int nArticles;
		public int nUsers;
	}
}