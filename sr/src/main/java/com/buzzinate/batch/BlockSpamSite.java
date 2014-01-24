package com.buzzinate.batch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.BlackSiteDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.BlackSite;
import com.buzzinate.main.MyModule;
import com.buzzinate.util.DomainNames;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.mapping.Mapper;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class BlockSpamSite {

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new MyModule());
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		BlackSiteDao blockSiteDao = new BlackSiteDao(injector.getInstance(Datastore.class));
		
		List<String> urlKeywords = Arrays.asList("tuan", "job", "zhaopin", "career", "mai", "buy", "pay", "quan", "product");
		List<String> titleKeywords = Arrays.asList("团购", "招聘", "诚聘", "商品", "购买", "买网", "商城", "网店", "淘宝", "优惠", "售", "包邮");
		
		HashMap<String, SpamStat> hostSpamStats = new HashMap<String, SpamStat>();
		HashMap<String, SpamStat> domainSpamStats = new HashMap<String, SpamStat>();
		
		List<Article> articles = articleDao.createQuery().retrievedFields(true, "url", "title").order("-" + Mapper.ID_KEY).limit(50000).asList();
		for (Article article: articles) {
			if (article.getUrl() == null) article.setUrl("");
			if (article.getTitle() == null) article.setTitle("");
			String host = DomainNames.safeGetHost(article.getUrl());
			String domain = DomainNames.getPLD(host);
			SpamStat hs = hostSpamStats.get(host);
			if (hs == null) {
				hs = new SpamStat(host);
				hostSpamStats.put(host, hs);
			}
			SpamStat ds = domainSpamStats.get(domain);
			if (ds == null) {
				ds = new SpamStat(domain);
				domainSpamStats.put(domain, ds);
			}
			hs.incTotal();
			ds.incTotal();
			
			int cnt = 0;
			for (String urlKeyword: urlKeywords) {
				if (article.getUrl().contains(urlKeyword) && !article.getUrl().contains("main")) {
					System.out.println(article.getTitle() + " ===> " + article.getUrl() + " | " + host);
					cnt += 1;
				}
			}
			for (String titleKeyword: titleKeywords) {
				if (article.getTitle().contains(titleKeyword)) {
					System.out.println(article.getTitle() + " ===> " + article.getUrl() + " | " + host);
					cnt += 2;
				}
			}
			if (cnt >= 2) {
				hs.incSpamCount();
				hs.setUrl(article.getUrl());
				hs.setTitle(article.getTitle());
				
				ds.incSpamCount();
				ds.setUrl(article.getUrl());
				ds.setTitle(article.getTitle());
			}
		}
		
		HashSet<String> verifiedSites = blockSiteDao.getVerifiedBlackSites();
		System.out.println("======================");
		
		HashSet<String> domains = new HashSet<String>();
		for (SpamStat ds: domainSpamStats.values()) {
			if (ds.getSpamCount() >= 4 && ds.getSpamRatio() >= 0.4) {
				domains.add(ds.getHost());
				System.out.println(ds.getTitle() + " (" + ds.getHost() + " | " + ds.getUrl() + ") => " + ds.getSpamRatio() + " | " + ds.getSpamCount() + "/" + ds.getTotal());
				if (verifiedSites.contains(ds.getHost())) continue;
				BlackSite blockSite = new BlackSite();
				blockSite.setSite(ds.getHost());
				blockSite.setUrl(ds.getUrl());
				blockSite.setTitle(ds.getTitle());
				blockSite.setScore(ds.getSpamCount());
				blockSite.setStatus(BlackSite.Status.UnVerified);
				blockSiteDao.save(blockSite);
			}
		}
		
		System.out.println("======================");
		
		for (SpamStat hs: hostSpamStats.values()) {
			String domain = DomainNames.getPLD(hs.getHost());
			if (hs.getSpamCount() >= 2 && hs.getSpamRatio() >= 0.6 && !domains.contains(domain)) {
				System.out.println(hs.getTitle() + " (" + hs.getHost() + " | " + hs.getUrl() + ") => " + hs.getSpamRatio() + " | " + hs.getSpamCount() + "/" + hs.getTotal());
				if (verifiedSites.contains(hs.getHost())) continue;
				BlackSite blockSite = new BlackSite();
				blockSite.setSite(hs.getHost());
				blockSite.setUrl(hs.getUrl());
				blockSite.setTitle(hs.getTitle());
				blockSite.setScore(hs.getSpamCount());
				blockSite.setStatus(BlackSite.Status.UnVerified);
				blockSiteDao.save(blockSite);
			}
		}
	}
}

class SpamStat implements Comparable<SpamStat> {
	private String host;
	private String url;
	private String title;
	private int total;
	private int spamCount;
	
	public SpamStat(String host) {
		this(host, 0, 0);
	}
	
	public SpamStat(String host, int total, int spamCount) {
		this.host = host;
		this.total = total;
		this.spamCount = spamCount;
	}
	
	public void incTotal() {
		total++;
	}
	
	public void incSpamCount() {
		spamCount++;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}

	public int getTotal() {
		return total;
	}

	public int getSpamCount() {
		return spamCount;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public float getSpamRatio() {
		return spamCount * 1f / total;
	}

	@Override
	public int compareTo(SpamStat other) {
		return -Float.compare(getSpamRatio(), other.getSpamRatio());
	}
}