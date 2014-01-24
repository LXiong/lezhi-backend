package com.buzzinate.crawl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.SearchResult;
import org.buzzinate.lezhi.api.Client;
import org.buzzinate.lezhi.api.Doc;
import org.buzzinate.lezhi.util.LargestTitle;
import org.buzzinate.lezhi.util.SignatureUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.buzzinate.common.dao.AreaArticleDao;
import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ArticleProfileDao;
import com.buzzinate.common.dao.ItemMinhashDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.TrendArticleDao;
import com.buzzinate.common.dao.TrendDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.mongo.UserDaoImpl;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.geo.GeoInfo;
import com.buzzinate.common.message.LinkDetail;
import com.buzzinate.common.message.LinkMessage;
import com.buzzinate.common.message.Message;
import com.buzzinate.common.message.Message.MessageType;
import com.buzzinate.common.model.AreaArticle;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.ArticleProfile;
import com.buzzinate.common.model.ItemMinhash;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.Minhash;
import com.buzzinate.common.model.MinhashQueue;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.model.Trend;
import com.buzzinate.common.model.TrendArticle;
import com.buzzinate.common.queue.MessageWrapper;
import com.buzzinate.common.queue.Queue;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.MinhashUtil;
import com.buzzinate.common.util.StringUtil;
import com.buzzinate.crawl.core.extract.ArticleText;
import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;
import com.buzzinate.crawl.core.util.ExtractUtils;
import com.buzzinate.crawl.queues.CrawlTask;
import com.buzzinate.crawl.queues.WorkQueues;
import com.buzzinate.crawl.simhash.SimHash;
import com.buzzinate.dao.RawArticleDao;
import com.buzzinate.keyword.KeywordUtil;
import com.buzzinate.keywords.MobileKeywordsExtractor;
import com.buzzinate.link.BlackSiteDetector;
import com.buzzinate.model.RawArticle;
import com.buzzinate.nlp.keywords.KeywordExtractor;
import com.buzzinate.nlp.keywords.KeywordSummaryExtractor;
import com.buzzinate.util.DomainNames;
import com.buzzinate.util.IntHashMap;
import com.buzzinate.util.LanguageDetector;
import com.buzzinate.util.TimeStats;
import com.google.code.morphia.Datastore;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

public class CrawlThread extends Thread {
	private static Logger log = Logger.getLogger(CrawlThread.class);

	private WorkQueues<MessageWrapper<LinkMessage>> queue;

	private Queue crawlQueue;
	private Queue classifyQueue;
	private PreferenceDao prefDao;
	private ArticleDao articleDao;
	private BlackSiteDetector blackSiteDetector;
	private SiteTemplateService siteTpl;
	private RawArticleDao rawArticleDao;
	private ArticleProfileDao apDao;
	private UserDao userDao;
	private ItemMinhashDao imhDao;
	private TrendDao trendDao;
	private TrendArticleDao taDao;
	private AreaArticleDao aaDao;
	private Client client;
	private Dictionary dict;

	public CrawlThread(WorkQueues<MessageWrapper<LinkMessage>> queue, Queue crawlQueue, Queue classifyQueue, PreferenceDao prefDao, ArticleDao articleDao, BlackSiteDetector blackSiteDetector,
			Client client, SiteTemplateService siteTpl, Dictionary dict) {
		this.queue = queue;
		this.crawlQueue = crawlQueue;
		this.classifyQueue = classifyQueue;
		this.prefDao = prefDao;
		this.articleDao = articleDao;
		this.blackSiteDetector = blackSiteDetector;
		this.siteTpl = siteTpl;
		this.rawArticleDao = new RawArticleDao(articleDao.getDatastore());
		this.dict = dict;
		this.client = client;
		Datastore ds = articleDao.getDatastore();
		this.apDao = new ArticleProfileDao(ds);
		this.userDao = new UserDaoImpl(ds);
		this.imhDao = new ItemMinhashDao(ds);
		this.trendDao = new TrendDao(ds);
		this.taDao = new TrendArticleDao(ds);
		this.aaDao = new AreaArticleDao(ds);

		this.setName("crawler-" + getId());
		this.setDaemon(true);
	}

	@Override
	public void run() {
		TimeStats.infoThreadStart();
		try {
			while (!Thread.interrupted()) {
				CrawlTask<MessageWrapper<LinkMessage>> task = null;
				try {
					task = queue.fetchTask();
					if (task == null) {
						try {
							Thread.sleep(100);
						} catch (Throwable t) {
						}
						continue;
					}
					LinkMessage msg = task.getInfo().getMessage();
					log.debug("Crawl " + msg.realUrl);
					processPage(msg);
					TimeStats.infoCrawled();
				} catch (MongoException.DuplicateKey e) {
					log.warn("Ignore duplicate entity: " + e.getMessage());
				} catch (Throwable t) {
					log.error("Crawl error, url=" + task.getInfo().getMessage().realUrl, t);
					try {
						LinkMessage msg = task.getInfo().getMessage();
						Article page = new Article();
						page.setUrl(msg.realUrl);
						page.setHash(StringUtil.hash(msg.realUrl));
						page.setSinaUrl(msg.sinaUrl);
						page.setError(t.getMessage());
						page.setStatus(Status.CRAWL_ERROR);
						articleDao.save(page, WriteConcern.NORMAL);
					} catch (Throwable rt) {
						log.error("Crawl error, url=" + task.getUrl(), rt);
					}
				} finally {
					if (task != null) {
						queue.finishTask(task);
						long tag = task.getInfo().getDeliveryTag();
						try {
							crawlQueue.commit(tag);
							log.info("Ack msg: " + tag);
						} catch (IOException e) {
							log.error("Could not ack msg: " + tag, e);
						}
					}
				}
			}
		} finally {
			TimeStats.infoThreadEnd();
		}
	}

	private boolean mayDuplicate(String url, LinkMessage msg) throws IOException {
		byte[] hash = StringUtil.hash(url);
		Article page = articleDao.findByHash(hash);
		if (page != null) {
			log.info("Already crawl, org=" + page.getUrl() + ", now=" + url);
			for (LinkDetail detail : msg.details) {
				// TODO: 需要添加 retweetSize和commentSize？
				page.setRetweetSize(Math.max(page.getRetweetSize(), detail.retweetSize));
				page.setCommentSize(Math.max(page.getCommentSize(), detail.commentSize));
			}
			page.updateScore();
			articleDao.save(page);

			if (page.getStatus() == Status.OK) {
				classifyQueue.send(new Message(-1, page.getId(), MessageType.Crawled));
			}

			for (LinkDetail detail : msg.details) {
				Preference pref = new Preference();
				pref.setUserId(detail.userId);
				pref.setPageId(page.getId());
				pref.setCreateAt(detail.tweetTime);
				prefDao.save(pref, WriteConcern.NORMAL);
			}

			updateItemMinhash(msg, page);

			if (page.getStatus() == Status.OK) {
				
				if (!aaDao.isExistInArea(page.getId())) {
					processGeoInfo(msg, page);
				}

				if (!taDao.isExistInTrend(page.getId())) {
					processTrendInfo(msg, page);
				}
			}
			return true;
		} else
			return false;
	}
	
	private void updateItemMinhash(LinkMessage msg, Article page) {
		ItemMinhash imh = imhDao.get(page.getId());
		if (imh == null) {
			imh = new ItemMinhash();
			imh.setId(page.getId());
			imh.setCreateAt(page.getCreateAt());
		}
		List<Long> userIds = new ArrayList<Long>();
		for (LinkDetail detail: msg.details) userIds.add(detail.userId);
		Map<Long, Integer> user2prefsize = userDao.mapField(userIds, "prefSize");
		MinhashQueue mq = new MinhashQueue(Constants.ITEM_MINHASH_NUM);
		for (Minhash mh: imh.getMinhashes()) mq.add(mh);
		for (LinkDetail detail: msg.details) {
			mq.add(new Minhash(MinhashUtil.hash(detail.userId), user2prefsize.get(detail.userId)));
		}
		imh.setMinhashes(mq.values());
		log.info(page.getId() + " itemcf minhash size: " + imh.getMinhashes().size());
		imhDao.save(imh);
	}

	private void processPage(LinkMessage msg) throws IOException {
		String url = msg.realUrl;

		if (url == null) {
			log.warn("url is null for " + msg.sinaUrl);
			url = "http://t.cn/" + msg.sinaUrl;
		}

		if (mayDuplicate(url, msg))
			return;

		Response resp = PageFetcher.fetch(url);
		if (resp.getStatusCode() >= 400)
			throw new IOException("status=" + resp.getStatusCode());

		Boolean isTargetLanguage = LanguageDetector.isTargetLanguage(url, resp.getDoc().title());

		log.info(resp.getRealUrl() + ", isTargetLanguage=" + isTargetLanguage);

		if (!isTargetLanguage)
			return;

		if (resp.getRealUrl() != null) {
			if (!resp.getRealUrl().equals(url) && mayDuplicate(resp.getRealUrl(), msg))
				return;
			url = resp.getRealUrl();
			msg.realUrl = url;
		}

		if (blackSiteDetector.isInBlackList(url)) {
			log.info("Ignore link:" + url);
			return;
		}

		String contentType = resp.getContentType();
		if (contentType == null || !(contentType.startsWith("text/") || contentType.startsWith("application/xml") || contentType.startsWith("application/xhtml+xml"))) {
			log.warn("Ignore content type:" + resp.getContentType());
			return;
		}

		rawArticleDao.save(new RawArticle(url, resp.getDoc().title(), resp.getDoc().outerHtml()));

		Article page = new Article();
		page.setSinaUrl(msg.sinaUrl);

		page.setUrl(url);
		page.setHash(StringUtil.hash(url));

		Document doc = resp.getDoc();
		String html = resp.getDoc().outerHtml();
		if (contentType.equalsIgnoreCase("text/vnd.wap.wml")) {
			String title = doc.select("card[title]").attr("title");
			doc.title(title);
		}

		String title = doc.title();
		ArticleText articleText = siteTpl.extract(url, doc);

		String summary = doc.select("meta[name=description]").attr("content");
		try {
			Element body = Jsoup.parse(articleText.getContent()).body();
			List<String> sentences = ExtractUtils.splitSentences(body);
			KeywordSummaryExtractor kse = new KeywordSummaryExtractor(title, sentences);
			summary = kse.extractSummary(100);
		} catch (Exception e) {
			log.warn("Could not use nlp to extract summary, reason" + e.getMessage());
		}

		log.info("Url: " + url + ", title: " + title + " ==> " + articleText.getTitle());
		page.setTitle(articleText.getTitle());
		page.setSummary(summary);
		page.setKeywords(doc.select("meta[name=keywords]").attr("content") + ", " + doc.title());
		page.setContent(articleText.getContent());
		page.setThumbnail(ThumbnailUtil.makeThumbnail(url, articleText.getThumbnail()));
		if (articleText.getThumbnail() != null)
			log.info("Thumbnail: " + articleText.getThumbnail() + " ==> " + page.getThumbnail());
		page.setCharset(resp.getCharset());
		page.setFrom(DomainNames.safeGetHost(page.getUrl()));
		page.setFromIcon("http://" + page.getFrom() + "/favicon.ico");
		page.setCreateAt(msg.getMinTime(resp.getLastMod()));

		page.setNumWords(articleText.getNumWords());
		page.setTextDensity(articleText.getTextDensity());
		page.setStatus(Status.OK);

		if (articleText.hasError() || resp.getCharset() == null)
			page.setStatus(Status.EXTRACT_ERROR);

		for (LinkDetail detail : msg.details) {
			// TODO: 需要添加 retweetSize和commentSize？
			page.setRetweetSize(Math.max(page.getRetweetSize(), detail.retweetSize));
			page.setCommentSize(Math.max(page.getCommentSize(), detail.commentSize));
		}

		List<Integer> minHashes = null;
		if (page.getStatus() == Status.OK) {
			// 检查之前的内容重复的文章，如果有，把本文章标为重复
			minHashes = SimHash.calcMinHash(Jsoup.parse(page.getContent()).body().text());
			if (SimHash.isDuplicate(apDao, page.getTitle(), minHashes))
				page.setStatus(Status.DUPLICATE);
		}
		
		page.updateScore();
		articleDao.save(page);
		
		if (page.getStatus() == Status.OK) {
			List<com.buzzinate.keywords.Keyword> keywords = MobileKeywordsExtractor.extract(url, html);
			String signature = SignatureUtil.signature(LargestTitle.parseLargest(page.getTitle()));
			List<String> kws = new ArrayList<String>();
			for (com.buzzinate.keywords.Keyword keyword: keywords) {
				if (StringUtils.contains(keyword.word(), "|")) continue;
				kws.add(String.format("%s|%s,%s", StringUtils.replace(keyword.word(), " ", "_"), keyword.freq(), keyword.field()));
			}
			client.bulkAdd(Arrays.asList(new Doc(page.getId(), page.getUrl(), page.getTitle(), signature, page.getThumbnail(), StringUtils.join(kws, " "), page.getCreateAt())));
		}

		if (page.getStatus() == Status.OK) {
			apDao.updateMinhash(page.getId(), page.getTitle(), minHashes, page.getCreateAt());

			List<String> sentences = ExtractUtils.splitSentences(Jsoup.parse(page.getContent()).body());
			sentences.add(page.getKeywords());
			List<Keyword> kws = KeywordUtil.extract(dict, page.getTitle(), sentences);
			apDao.updateKeywords(page.getId(), kws, page.getCreateAt());

			processGeoInfo(msg, page);
			processTrendInfo(msg, page);
		}

		if (page.getStatus() == Status.OK) {
			classifyQueue.send(new Message(-1, page.getId(), MessageType.Crawled));
		}

		for (LinkDetail detail : msg.details) {
			Preference pref = new Preference();
			pref.setUserId(detail.userId);
			pref.setPageId(page.getId());
			pref.setCreateAt(detail.tweetTime);
			prefDao.save(pref, WriteConcern.NORMAL);
		}

		updateItemMinhash(msg, page);
	}

	private void processGeoInfo(LinkMessage msg, Article page) {
		
		GeoInfo geoInfo = msg.geoInfo;
		if(geoInfo == null || geoInfo.getProvince().isEmpty()){
			return;
		}
		
		AreaArticle areaArticle = new AreaArticle();
		areaArticle.setPageId(page.getId());
		areaArticle.setProvinceName(geoInfo.getProvince());
		areaArticle.setProvinceHash(StringUtil.hash(geoInfo.getProvince()));
		if(!geoInfo.getCity().isEmpty()){
			areaArticle.setCityName(geoInfo.getCity());
			areaArticle.setCityHash(StringUtil.hash(geoInfo.getCity()));
		}
		if(!geoInfo.getDistrict().isEmpty()){
			areaArticle.setDistrictName(geoInfo.getDistrict());
			areaArticle.setDistrictHash(StringUtil.hash(geoInfo.getDistrict()));
		}
		
		aaDao.save(areaArticle);
		log.info("geoInfo => pageTitle: " + page.getTitle() + " geoProvince : " + geoInfo.getProvince());
	}

	private void processTrendInfo(LinkMessage msg, Article page) {

		IntHashMap<String> trendCounter = new IntHashMap<String>();
		Long retweetCounter = 0l;

		for (LinkDetail detail : msg.details) {
			if (detail.isTrendMsg) {
				retweetCounter += detail.retweetSize;
				trendCounter.adjustOrPut(detail.trendName, 1, 1);
			}
		}

		Integer maxTrendCount = 0;
		String maxTrendName = "";
		if (trendCounter.size() > 0) {
			for (Entry<String, Integer> e : trendCounter.entrySet()) {
				if (e.getValue() > maxTrendCount) {
					maxTrendName = e.getKey();
					maxTrendCount = e.getValue();
				}
			}
			if (!maxTrendName.isEmpty()) {

				List<String> trendGrams = KeywordExtractor.extractTrendGram(maxTrendName, Constants.TREND_MIN_LENGTH);
				if (trendGrams.size() <= 0) {
					return;
				}
				AhoCorasick<String> tree = new AhoCorasick<String>();
				for (String trendGram : trendGrams) {
					tree.add(trendGram.getBytes(), trendGram);
				}
				tree.prepare();

				StringBuilder sb = new StringBuilder();
				if (page.getTitle() != null) {
					sb.append(page.getTitle());
				}
				if (page.getSummary() != null) {
					sb.append(page.getSummary());
				}

				// 针对page的title和summary，如果title和summary中包含TREND_INCLUDED_GRAM_NUMS个不同的trendGram，则认为这篇文章属于这个话题
				Iterator<SearchResult<String>> searcher = tree.search(sb.toString().getBytes());
				HashSet<String> containsSet = new HashSet<String>();
				while (searcher.hasNext()) {
					containsSet.addAll(searcher.next().getOutputs());
				}

				Integer minTrendSize = 1;

				if (trendGrams.size() > 1) {
					minTrendSize = Constants.TREND_INCLUDED_GRAM_NUMS;
				}

				log.info("trendname : " + maxTrendName + " pageTitle: " + page.getTitle() + " pageUrl: " + page.getUrl() + " trendGrams: " + containsSet);
				
				if (containsSet.size() >= 0) {
					if (containsSet.size() >= minTrendSize) {
						TrendInfo ti = getAndUpdateTrend(maxTrendName, trendDao, 1);
						TrendArticle ta = new TrendArticle();
						ta.setPageId(page.getId());
						ta.setTrendId(ti.trendId);
						ta.setCreateAt(page.getCreateAt());
						taDao.save(ta);

						long apSearchTime = 0l;
						if (ti.isNewTrend) {
							log.info("first meet trend => " + maxTrendName);
							apSearchTime = Constants.ONE_DAY * 30;
						} else {
							apSearchTime = Constants.ONE_HOUR / 4;
						}
						// 将包含这个话题的其他文章插入话题文章列表
						List<String> keywords = new ArrayList<String>();
						keywords.add(maxTrendName);
						List<ArticleProfile> aps = apDao.findByKeywords(keywords, System.currentTimeMillis() - apSearchTime);
						int insertSize = 0;
						for (ArticleProfile ap : aps) {
							if (!taDao.isExistInTrend(ap.getId())) {
								TrendArticle rta = new TrendArticle();
								rta.setPageId(ap.getId());
								rta.setTrendId(ti.trendId);
								rta.setCreateAt(ap.getCreateAt());
								taDao.save(rta);
								insertSize += 1;
							}
						}
						if (insertSize > 0) {
							getAndUpdateTrend(maxTrendName, trendDao, insertSize);
						}
					}
				}
			}
		}
	}

	private static synchronized TrendInfo getAndUpdateTrend(String trendName, TrendDao trendDao, int insertSize) {
		byte[] hash = StringUtil.hash(trendName);
		Boolean isNewTrend = false;
		Trend trend = trendDao.findByHash(hash);
		if (null == trend) {
			trend = new Trend();
			trend.setHash(hash);
			trend.setName(trendName);
			trend.setCreateAt(System.currentTimeMillis());
			trend.setCount(1l);
			isNewTrend = true;
		} else {
			if (trend.getCount() == 0) {
				isNewTrend = true;
			}
			trend.setCount(trend.getCount() + insertSize);
		}
		trendDao.save(trend);
		return new TrendInfo(isNewTrend, trend.getId());
	}

}

class TrendInfo {
	Boolean isNewTrend;
	Long trendId;

	public TrendInfo(Boolean isNewTrend, Long trendId) {
		this.isNewTrend = isNewTrend;
		this.trendId = trendId;
	}
}