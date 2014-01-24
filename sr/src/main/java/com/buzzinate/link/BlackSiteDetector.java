package com.buzzinate.link;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import com.buzzinate.common.dao.BlackSiteDao;
import com.buzzinate.util.DomainNames;
import com.google.inject.Inject;

public class BlackSiteDetector {
	private BlackSiteDao blackSiteDao;
	private AtomicReference<HashSet<String>> blackSites = new AtomicReference<HashSet<String>>();
	private long lastLoadTime;
	
	@Inject
	public BlackSiteDetector(BlackSiteDao blackSiteDao) {
		this.blackSiteDao = blackSiteDao;
		loadBlackSites();
	}
	
	private void loadBlackSites() {
		HashSet<String> sites = blackSiteDao.getVerifiedBlackSites();
		blackSites.set(sites);
		lastLoadTime = System.currentTimeMillis();
	}

	public boolean isInBlackList(String url) {
		if (url.contains("http%3A%2F%2F")) return true;
		if (lastLoadTime + 1000L * 3600 < System.currentTimeMillis()) loadBlackSites();
		
		String site = DomainNames.safeGetPLD(url).toLowerCase();
		if (blackSites.get().contains(site)) return true;
		if (site.contains("taobao.com") || site.contains("tmall.com") || site.contains("360buy.com")) return true;
		return false;
	}
}
