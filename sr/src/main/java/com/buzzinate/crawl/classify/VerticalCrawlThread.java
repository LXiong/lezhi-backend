package com.buzzinate.crawl.classify;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.model.Status;
import com.buzzinate.common.util.StringUtil;
import com.buzzinate.crawl.SiteTemplateService;
import com.buzzinate.crawl.core.extract.ArticleText;
import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;
import com.buzzinate.crawl.core.util.URLCanonicalizer;
import com.buzzinate.crawl.queues.CrawlTask;
import com.buzzinate.crawl.queues.WorkQueues;
import com.buzzinate.util.DomainNames;
import com.buzzinate.util.TimeStats;
import com.mongodb.MongoException;

public class VerticalCrawlThread extends Thread {

	private static Logger log = Logger.getLogger(VerticalCrawlThread.class);

	private WorkQueues<String> queue;
	private ArticleDao articleDao;
	private SiteTemplateService siteTpl;
	private Set<String> crawledUrlSets;
	private int maxQueueSize;
	private String siteSuffix;
	private Category category;

	public VerticalCrawlThread(WorkQueues<String> queue, ArticleDao articleDao,
			SiteTemplateService siteTpl, Set<String> crawledUrlSets,
			int maxQueueSize, String siteSuffix, Category category) {
		this.queue = queue;
		this.articleDao = articleDao;
		this.siteTpl = siteTpl;
		this.setName("crawler-" + getId());
		this.setDaemon(true);
		this.crawledUrlSets = crawledUrlSets;
		this.maxQueueSize = maxQueueSize;
		this.siteSuffix = siteSuffix;
		this.category = category;
		log.info(this.getName());
	}

	@Override
	public void run() {
		TimeStats.infoThreadStart();
		try {
			while (!Thread.interrupted()) {
				CrawlTask<String> task = null;
				log.info("queue-size => " + queue.getSize());
				try {
					task = queue.fetchTask();
					if (task == null) {
						try {
							Thread.sleep(100);
						} catch (Throwable t) {
						}
						continue;
					}
					String msg = task.getInfo();
					log.debug("Crawl " + msg);
					processPage(msg);
					TimeStats.infoCrawled();
				} catch (MongoException.DuplicateKey e) {
					log.warn("Ignore duplicate entity: " + e.getMessage());
				} catch (Throwable t) {
					log.error("Crawl error, url=" + task.getInfo(), t);
				} finally {
					if (task != null) {
						queue.finishTask(task);
					}
				}
			}
		} finally {
			TimeStats.infoThreadEnd();
		}
	}

	private void processPage(String msg) throws IOException,
			InterruptedException {
		String url = msg;
		if (url == null) {
			log.warn("the url is null");
			return;
		}

		Response resp = PageFetcher.fetch(url);
		if (resp.getStatusCode() >= 400)
			throw new IOException("status=" + resp.getStatusCode());

		if (resp.getRealUrl() != null) {
			if (!resp.getRealUrl().equals(url)
					&& mayDuplicate(resp.getRealUrl()))
				return;
			url = resp.getRealUrl();
			msg = url;
		}

		String contentType = resp.getContentType();
		if (contentType == null
				|| !(contentType.startsWith("text/")
						|| contentType.startsWith("application/xml") || contentType
						.startsWith("application/xhtml+xml"))) {
			log.warn("Ignore content type:" + resp.getContentType());
			return;
		}

		Document doc = resp.getDoc();
		if (contentType.equalsIgnoreCase("text/vnd.wap.wml")) {
			String title = doc.select("card[title]").attr("title");
			doc.title(title);
		}

		ArticleText articleText = siteTpl.extract(url, doc);

		Element body = Jsoup.parse(resp.getDoc().html()).body();
		Elements es = body.getElementsByTag("a");
		
		for (int i = 0; i < es.size(); i++) {
			Element e = es.get(i);
			String href = URLCanonicalizer.getCanonicalURL(e.attr("href"), url);	
			if (href.startsWith(siteSuffix)) {
				if (!mayDuplicate(href) && !crawledUrlSets.contains(href)) {
					if (queue.getSize() <= maxQueueSize * 0.8) {
						queue.submit(href, href);
						synchronized (this) {
							crawledUrlSets.add(href);
						}
					} else {
						log.info("remove cururl => " + url);
						synchronized (this) {
							// 这个url的子链接没有被完全抓取到，故从crawledUrlSets中删除掉，将来碰到这个url，会重新抓取他的子链接
							crawledUrlSets.remove(url);
						}
						break;
					}
				}
			}
		}
		
		System.out.println("queueSize => " + queue.getSize());
		
		if (articleText.hasError() || resp.getCharset() == null){
			System.out.println("this is not article => " + url );
			return;
		} else {
			System.out.println("this is article => " + url );
		}
		/****************/
		Article page = new Article();
		page.setSinaUrl("");

		page.setUrl(url);
		page.setHash(StringUtil.hash(url));
		page.setTitle(articleText.getTitle());
		page.setKeywords(doc.select("meta[name=keywords]").attr("content") + ", " + doc.title());
		page.setContent(articleText.getContent());
		
		page.setCharset(resp.getCharset());
		page.setFrom(DomainNames.safeGetHost(page.getUrl()));
		page.setFromIcon("http://" + page.getFrom() + "/favicon.ico");
		page.setCreateAt(System.currentTimeMillis());

		page.setNumWords(articleText.getNumWords());
		page.setTextDensity(articleText.getTextDensity());
		page.setIsTraining(true);
		page.setCategory(category);
		page.setStatus(Status.OK);
		articleDao.save(page);

	}
	private boolean mayDuplicate(String url) throws IOException {
		byte[] hash = StringUtil.hash(url);
		Article page = articleDao.findByHash(hash);
		if (page != null) {
			return true;
		} else
			return false;
	}
}