package com.buzzinate.jianghu.api.servlet;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;
import com.buzzinate.jianghu.config.InjectorSingleton;
import com.buzzinate.jianghu.util.ContentCleaner;
import com.google.inject.Injector;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedServlet extends HttpServlet {

	static final long serialVersionUID = 940196742313994740L;

	private static final String DEFAULT_FEED_TYPE = "default.feed.type";
	private static final String DEFAULT_FEED_ADDR = "default.feed.address";

	private static final String FEED_PAGE = "page";
	private static final String FEED_COUNT = "count";
	private static final String FEED_CID = "cid";

	private static final String FEED_TYPE = "type";
	private static final String MIME_TYPE = "application/xml; charset=UTF-8";
	private static final String COULD_NOT_GENERATE_FEED_ERROR = "Could not generate feed";

	Injector injector = InjectorSingleton.getInjector();
	private ArticleDao articleDao = injector.getInstance(ArticleDao.class);

	private String _defaultFeedType;
	private String _defaultFeedAddr;

	private Integer feedPage = 1;
	private Integer feedCount = 20;
	private Integer feedCid = -1;

	public void init() {
		_defaultFeedType = getServletConfig().getInitParameter(
				DEFAULT_FEED_TYPE);
		_defaultFeedType = (_defaultFeedType != null) ? _defaultFeedType
				: "rss_2.0";

		_defaultFeedAddr = getServletConfig().getInitParameter(
				DEFAULT_FEED_ADDR);
		_defaultFeedAddr = (_defaultFeedAddr != null) ? _defaultFeedAddr
				: "http://api.lezhi.me/rss/";

	}

	private void initParams(HttpServletRequest req) {
		try {
			String feedPageStr = req.getParameter(FEED_PAGE);
			feedPage = (feedPageStr != null) ? Integer.valueOf(feedPageStr) : 1;
			String feedCountStr = req.getParameter(FEED_COUNT);
			feedCount = (feedCountStr != null) ? Integer.valueOf(feedCountStr): 20;
			String feedCidStr = req.getParameter(FEED_CID);
			feedCid = (feedCidStr != null) ? Integer.valueOf(feedCidStr) : -1;
		} catch (NumberFormatException e) {
			feedPage = 1;
			feedCount = 20;
			feedCid = -1;
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		try {
			initParams(req);
			SyndFeed feed = getFeed(req);

			String feedType = req.getParameter(FEED_TYPE);
			feedType = (feedType != null) ? feedType : _defaultFeedType;
			feed.setFeedType(feedType);

			res.setContentType(MIME_TYPE);
			SyndFeedOutput output = new SyndFeedOutput();
			output.output(feed, res.getWriter());
		} catch (FeedException ex) {
			String msg = COULD_NOT_GENERATE_FEED_ERROR;
			log(msg, ex);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
		}
	}

	protected SyndFeed getFeed(HttpServletRequest req) throws IOException,
			FeedException {

		List<Article> articles = null;
		String feedLink = _defaultFeedAddr;
		if (feedCid > 0) {
			
			articles = articleDao.findPop(Category.getCategory(feedCid),
					feedCount * 2, feedPage);
			feedLink = feedLink + "?cid=" + feedCid;
		} else {
			articles = articleDao.findPop(feedCount * 2, feedPage);
		}

		SyndFeed feed = new SyndFeedImpl();

		feed.setTitle(Category.getCnCategoryName(feedCid));
		feed.setLink(feedLink);
		feed.setDescription("热门乐知分类文章：科技，体育，文化，新闻，财经，生活，娱乐，女性");

		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		long now = System.currentTimeMillis();
		for (Article article : articles) {
			if(article.getCreateAt() > now) continue;
			Date date = new Date(article.getCreateAt());
			SyndContent description;
			description = new SyndContentImpl();
			description.setType("text/plain");
			String cleanContent = ContentCleaner.clean(article.getContent());
			description.setValue(cleanContent);

			SyndEntry entry = new SyndEntryImpl();
			entry.setTitle(article.getTitle());
			entry.setLink(article.getUrl());
			entry.setPublishedDate(date);
			entry.setDescription(description);

			entries.add(entry);
			if(entries.size() >= feedCount) break;
		}

		feed.setEntries(entries);
		return feed;
	}

}