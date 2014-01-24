package com.buzzinate.jianghu.api;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.apache.mahout.math.MurmurHash3;
import org.jboss.resteasy.annotations.GZIP;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.model.RelatedArticle.RelatedItem;
import com.buzzinate.common.model.User;
import com.buzzinate.common.util.StringUtil;
import com.buzzinate.jianghu.api.view.ArticleSummary;
import com.buzzinate.jianghu.api.view.ArticleVO;
import com.buzzinate.jianghu.api.view.ExtView;
import com.buzzinate.jianghu.dao.ReadDao;
import com.buzzinate.jianghu.sr.RecommendService;
import com.buzzinate.jianghu.sr.TopItem;
import com.buzzinate.jianghu.util.ContentCleaner;
import com.google.inject.Inject;

@Path("engine")
@Produces({"application/json"})
public class EngineResource {
	private static Logger log = Logger.getLogger(EngineResource.class);
	
	@Inject private RecommendService recommendService;
	@Inject private UserDao userDao;
	@Inject private ArticleDao articleDao;
	@Inject private ReadDao readDao;
	
	@POST
	@Path("track")
	@PermitAll
	public void track(@FormParam("socialId") String socialId, @FormParam("cookieId") String cookieId, @FormParam("url") String url) {
		long userId = getUserId(socialId, cookieId);
		
		byte[] hash = StringUtil.hash(url);
		Article page = articleDao.findByHash(hash);
		if (page != null) readDao.addRead(userId, page.getId());
	}
	
	@GET @Path("recommend")
	@PermitAll
	@GZIP
	public ArticleVO[] recommend(
			@QueryParam("socialId") String socialId, 
			@QueryParam("cookieId") String cookieId,
			@QueryParam("offset") @DefaultValue("0") int offset, 
			@QueryParam("length") @DefaultValue("10") int length, 
			@QueryParam("refresh") @DefaultValue("0") int refresh
		) {
		long userId = getUserId(socialId, cookieId);
		List<TopItem> items = recommendService.recommend(userId, System.currentTimeMillis(), offset, length);
		log.info("request range=[" + offset + ", " + (offset + length) + "), response len=" + items.size());
		if (items.isEmpty()) return new ArticleVO[0];
		
		List<Long> ids = toIds(items);
		List<Article> articles = articleDao.get(ids, "id", "title", "summary", "thumbnail", "fromIcon", "from", "url", "category", "createAt");
		ArticleVO[] result = ArticleVO.make(items, articles);
		if (result.length > length) result = Arrays.copyOf(result, length);
		return result;
	}
	
	@GET @Path("top")
	@PermitAll
	@GZIP
	public ArticleVO[] top(
			@QueryParam("category") @DefaultValue("-1") int category, 
			@QueryParam("offset") @DefaultValue("0") int offset, 
			@QueryParam("length") @DefaultValue("10") int length, 
			@QueryParam("refresh") @DefaultValue("0") int refresh
		) {
		List<Article> articles = null;
		if (category > 0) {
			articles = articleDao.top(Category.getCategory(category), offset, length);
		} else {
			articles = articleDao.top(offset, length);
		}
		List<Long> ids = new ArrayList<Long>();
		for (Article a: articles) ids.add(a.getId());
		return ArticleVO.make(articles);
	}
	
	@GET @Path("article")
	@PermitAll
	@GZIP
	public Map<String, Object> getArticle(@QueryParam("url") String url) {
		byte[] hash = StringUtil.hash(url);
		Article article = articleDao.findByHash(hash);
		String cleanContent = ContentCleaner.clean(article.getContent());
		return ExtView.from("url", url, "title", article.getTitle(), "content", cleanContent);
	}
	
	@GET @Path("related")
	@PermitAll
	@GZIP
	public List<ArticleSummary> related(
			@QueryParam("url") String url,
			@QueryParam("offset") @DefaultValue("0") int offset, 
			@QueryParam("length") @DefaultValue("10") int length
		) {
		byte[] hash = StringUtil.hash(url);
		Article article = articleDao.findByHash(hash);
		List<RelatedItem> items = recommendService.recommendRelated(article.getId(), 5);
		
		HashMap<Long, RelatedItem> id2item = new HashMap<Long, RelatedItem>();
		List<Long> ids = new ArrayList<Long>();
		for (RelatedItem item: items) {
			ids.add(item.articleId);
			id2item.put(item.articleId, item);
		}
		ids = articleDao.filterByStatus(ids);
		List<Article> articles = articleDao.get(ids, "id", "title", "summary", "thumbnail", "fromIcon", "from", "url", "category", "createAt");
		List<ArticleSummary> related = new ArrayList<ArticleSummary>();
		// deduplicate the articles with same title
		HashSet<String> showedTitles = new HashSet<String>();
		for (Article a: articles) {
			if(null != a.getTitle() && !showedTitles.contains(a.getTitle())){
				related.add(new ArticleSummary(a.getId(), a.getTitle(), a.getSummary(), a.getUrl(), "previous=" + article.getId()));
				showedTitles.add(a.getTitle());
			}
		}
		return related;
	}
	
	private static List<Long> toIds(List<TopItem> items) {
		List<Long> ids = new ArrayList<Long>();
		for (TopItem item: items) {
			ids.add(item.getItemId());
		}
		return ids;
	}

	private long getUserId(String socialId, String cookieId) {
		long userId = -1L;
		if (socialId != null && socialId.startsWith("sinaminiblog_")) {
			long uid = Long.parseLong(socialId.substring("sinaminiblog_".length()));
			User user = userDao.findByUid(uid);
			if (user == null) {
				user = new User();
				user.setUid(uid);
				user.setCookieId(cookieId);
				userDao.save(user);
			}
			userId = user.getId();
		}
		if (userId == -1L) {
			User user = userDao.findByCookieId(cookieId);
			if (user == null) {
				user = new User();
				user.setUid(-hashCookie(cookieId));
				user.setCookieId(cookieId);
				userDao.save(user);
			}
			userId = user.getId();
		}
		return userId;
	}
	
	private Charset utf8 = Charset.forName("UTF-8");
	private long hashCookie(String cookieId) {
		byte[] bs = cookieId.getBytes(utf8);
		return MurmurHash3.murmurhash3_x86_32(bs, 0, bs.length, 0x3c074a61) & 0x7FFFFFFF;
	}
}