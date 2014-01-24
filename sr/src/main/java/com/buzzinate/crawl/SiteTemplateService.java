package com.buzzinate.crawl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.buzzinate.common.util.SortUtils;
import com.buzzinate.crawl.core.extract.ArticleText;
import com.buzzinate.crawl.core.extract.TextExtractor;
import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;
import com.buzzinate.crawl.core.tpl.SiteTemplate;
import com.buzzinate.crawl.core.util.DomainNames;
import com.buzzinate.crawl.core.util.ExtractUtils;
import com.buzzinate.crawl.core.util.FreqTextDetector;
import com.buzzinate.crawl.core.util.TextUtility;
import com.buzzinate.dao.RawArticleDao;
import com.buzzinate.main.MyModule;
import com.buzzinate.model.RawArticle;
import com.buzzinate.nlp.keywords.KeywordSummaryExtractor;
import com.google.code.morphia.Datastore;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.common.collect.TreeMultimap;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class SiteTemplateService {
	private RawArticleDao rawArticleDao;
	
	private ConcurrentMap<String, List<String>> site2rawIdsCache = new MapMaker()
		.expiration(2, TimeUnit.HOURS)
		.makeComputingMap(new Function<String, List<String>>() {
			@Override
			public List<String> apply(String site) {
				return rawArticleDao.findBySite(site);
			}
		});
	
	private ConcurrentMap<String, SiteTemplate> tplCache = new MapMaker()
		.expiration(20, TimeUnit.HOURS)
		.makeMap();
	
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		SiteTemplateService sts = new SiteTemplateService(ds);
//		String url = "http://p.pomoho.com/play/dF90ZXQxODgvMTc2MjMyMTE$";
//		String url = "http://www.tianya.cn/publicforum/content/news/1/232242.shtml";
//		String url = "http://news.jcrb.com/jxsw/201112/t20111211_770151.html";
//		String url = "http://tech.hexun.com/2011-12-20/136470955.html";
//		String url = "http://game.donews.com/news/201112/1212467.html";
//		String url = "http://www.infoq.com/cn/news/2011/12/ska-ibm";
//		String url = "http://www.022net.com/2011/12-11/492017213314120.html";
//		String url = "http://sns.cjzg.cn/cqk868/blog/view?blog_id=826133";
//		String url = "http://domestic.kankanews.com/gangaotai/2011-12-22/920156.shtml";
//		String url = "http://www.unnoo.com/tradenews/241.html";
//		String url = "http://www.yzwb.net/epaper/html/2011-12/22/content_393052.htm?div=-1";
//		String url = "http://sports.3g.cn/nba/2012preview/lakers.aspx";
//		String url = "http://www.36kr.com/p/70573.html";
//		String url = "http://www.chinadigitalmarket.com/?p=149";
//		String url = "http://hangzhou.tuan26.com/#item_id=3273349&no=sina";
		String url = "http://www.neonan.com/article/view.aspx?id=5142989869541799257";
		Response resp = PageFetcher.fetch(url);
		ArticleText text = sts.extract(url, resp.getDoc());
		System.out.println(text.getTitle());
		
		List<String> sentences = ExtractUtils.splitSentences(Jsoup.parse(text.getContent()).body());
		KeywordSummaryExtractor kse = new KeywordSummaryExtractor(text.getTitle(), sentences);
		System.out.println(kse.extractSummary(100));
		System.out.println(text.getContent());
	}
	
	public SiteTemplateService(Datastore ds) {
		this.rawArticleDao = new RawArticleDao(ds);
	}
	
	public ArticleText extract(String url, Document doc) throws IOException {
		String tplId = StringUtils.substringBeforeLast(RawArticle.makeId(url), "#");
		SiteTemplate st = tplCache.get(tplId);
		if (st == null) {
			st = build(url);
			if (st != null) tplCache.put(tplId, st);
		}
		if (st == null) return TextExtractor.extract(doc.body(), doc.title(), url);
		else return st.extract(url, doc.body());
	}
	
	private SiteTemplate build(String url) {
		TreeMultimap<Integer, String> diff2id = TreeMultimap.create(SortUtils.comp(Integer.class), SortUtils.comp(String.class));
		String id = RawArticle.makeId(url);
		List<String> rawIds = site2rawIdsCache.get(DomainNames.safeGetPLD(url));
		for (String rawId: rawIds) diff2id.put(TextUtility.diff(id, rawId), rawId);
		
		List<String> topRawIds = new ArrayList<String>();
		for (Map.Entry<Integer, Collection<String>> e: diff2id.asMap().entrySet()) {
			for (String rawId: e.getValue()) {
				topRawIds.add(rawId);
				if (topRawIds.size() >= 5) break;
			}
			if (topRawIds.size() >= 5) break;
		}
		
		FreqTextDetector ftd = new FreqTextDetector();
		for (String rawId: topRawIds) {
			RawArticle raw = rawArticleDao.get(rawId);
			Document doc = Jsoup.parse(raw.content);
			ExtractUtils.detectFreqText(doc.body(), ftd);
			ftd.shift();
		}
		return new SiteTemplate(new HashSet<String>(ftd.freqSiteText(topRawIds.size() * 8 / 11)));
	}
}