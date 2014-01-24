package com.buzzinate.batch;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ArticleProfileDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.UserProfileDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.ArticleProfile;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.KeywordFeature;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.model.User;
import com.buzzinate.common.model.UserFeature;
import com.buzzinate.common.model.UserProfile;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ExportSampleData {
	
	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		UserDao userDao = injector.getInstance(UserDao.class);
		UserProfileDao upDao = new UserProfileDao(ds);
		ArticleProfileDao apDao = new ArticleProfileDao(ds);
		
		CSVWriter userCsv = new CSVWriter(new FileWriter("user-sample.csv"));
		List<String> fields = null;
		
		List<User> users = userDao.createQuery().field("detail").exists().filter("isLeziUser", true).asList();
		if (users.size() < 50) users.addAll(userDao.createQuery().field("detail").exists().limit(50 - users.size()).asList());
		if (users.size() > 50) users = users.subList(0, 50);
		for (User user: users) {
			Map<String, String> userData = extractUser(user.getId(), user, upDao.get(user.getId()));
			if (fields == null) {
				fields = new ArrayList<String>(userData.keySet());
				Collections.sort(fields);
				userCsv.write(fields);
			}
			List<String> values = fill(fields, userData);
			userCsv.write(values);
		}
//		System.out.println(userData);
		userCsv.close();
		
		
		CSVWriter articleCsv = new CSVWriter(new FileWriter("article-sample.csv"));
		fields = null;
		List<Article> as = articleDao.createQuery().filter("status", Status.OK).order("-_id").limit(100).asList();
		for (Article a: as) {
			Map<String, String> articleData = extractPage(a.getId(), a, apDao.get(a.getId()));
			if (fields == null) {
				fields = new ArrayList<String>(articleData.keySet());
				Collections.sort(fields);
				articleCsv.write(fields);
			}
			List<String> values = fill(fields, articleData);
			articleCsv.write(values);
		}
		articleCsv.close();
	}
	
	

	private static List<String> fill(List<String> fields, Map<String, String> map) {
		List<String> values = new ArrayList<String>();
		for (String field: fields) {
			String value = map.get(field);
			if (value == null) value = "";
			values.add(value);
		}
		return values;
	}



	private static Map<String, String> extractPage(long articleId, Article article, ArticleProfile ap) {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("id", String.valueOf(article.getId()));
		data.put("url", article.getUrl());
		data.put("title", article.getTitle());
		data.put("content", article.getContent());
		data.put("summary", article.getSummary());
		data.put("metakeywords", article.getKeywords());
		data.put("category", article.getCategory().toString());
		data.put("comments-size", String.valueOf(article.getCommentSize()));
		data.put("retweets-size", String.valueOf(article.getRetweetSize()));
		data.put("pref-size", String.valueOf(article.getPrefSize()));
		data.put("score", String.valueOf(article.getScore()));
		
		if (ap != null) {
			List<String> keywords = new ArrayList<String>();
			for (Keyword kw: ap.getKeywords()) keywords.add(kw.getWord());
			data.put("lezhi-keywords", StringUtils.join(keywords, ", "));
		}
		return data;
	}



	private static Map<String, String> extractUser(long userId, User user, UserProfile up) {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("id", String.valueOf(user.getId()));
		String detail = user.getDetail();
		if (StringUtils.isNotBlank(detail)) {
			detail = detail.substring("User [".length(), detail.length() - "]".length());
			String[] fields = StringUtils.split(detail, ",");
			for (String f: fields) {
//				System.out.println(f.trim());
				String fn = StringUtils.substringBefore(f, "=").trim();
				String fv = StringUtils.substringAfter(f, "=");
				data.put("sina-" + fn, fv);
			}
		}
		if (up != null) {
			List<Long> neighbors = new ArrayList<Long>();
			List<String> keywords = new ArrayList<String>();
			for (UserFeature uf: up.getUserFeatures()) {
				neighbors.add(uf.getUserId());
			}
			data.put("lezhi-neighbors", StringUtils.join(neighbors, ", "));
			
			for (KeywordFeature kf: up.getKeywordFeatures()) {
				keywords.add(kf.getKeyword());
			}
			for (Keyword kw: up.getKeywords()) {
				keywords.add(kw.getWord());
			}
			data.put("lezhi-keywords", StringUtils.join(keywords, ", "));
		}
		return data;
	}
}

class UserData {
	public long id;
	public String uid;
	public String screenName;
	public List<String> keywords;
	
	public static List<String> header() {
		return Arrays.asList("id", "uid", "screenName", "keywords");
	}
}