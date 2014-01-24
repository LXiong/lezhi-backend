package com.buzzinate.jianghu.api;

import java.io.IOException;
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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.GZIP;

import redis.clients.jedis.ShardedJedisPool;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ItemMinhashDao;
import com.buzzinate.common.dao.Page;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.geo.GeoInfo;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.ArticleType;
import com.buzzinate.common.model.Category;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.model.RelatedArticle.RelatedItem;
import com.buzzinate.common.model.Trend;
import com.buzzinate.common.model.User;
import com.buzzinate.common.util.GeoUtil;
import com.buzzinate.jianghu.api.view.ArticleContent;
import com.buzzinate.jianghu.api.view.ArticleSummary;
import com.buzzinate.jianghu.api.view.ArticleVO;
import com.buzzinate.jianghu.api.view.ExtView;
import com.buzzinate.jianghu.api.view.UserVO;
import com.buzzinate.jianghu.dao.AreaArticleDao;
import com.buzzinate.jianghu.dao.ArticleTypeDao;
import com.buzzinate.jianghu.dao.CommentDao;
import com.buzzinate.jianghu.dao.FeedbackDao;
import com.buzzinate.jianghu.dao.FollowDao;
import com.buzzinate.jianghu.dao.LikeDao;
import com.buzzinate.jianghu.dao.ReadDao;
import com.buzzinate.jianghu.dao.TrendArticleDao;
import com.buzzinate.jianghu.dao.TrendDao;
import com.buzzinate.jianghu.dao.UserStatusDao;
import com.buzzinate.jianghu.model.Feedback;
import com.buzzinate.jianghu.model.UserStatus;
import com.buzzinate.jianghu.sr.ItemMinhashUtil;
import com.buzzinate.jianghu.sr.RecommendService;
import com.buzzinate.jianghu.sr.TopItem;
import com.buzzinate.jianghu.util.ApiException;
import com.buzzinate.jianghu.util.ContentCleaner;
import com.google.code.morphia.Datastore;
import com.google.inject.Inject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

@Path("articles")
@Produces({"application/json"})
public class ArticleResource {
	private static Logger log = Logger.getLogger(ArticleResource.class);
	
	@Inject private PreferenceDao prefDao;
	@Inject private UserDao userDao;
	@Inject private ArticleDao articleDao;
	@Inject private FollowDao followDao;
	@Inject private LikeDao likeDao;
	@Inject private ReadDao readDao;
	@Inject private CommentDao commentDao;
	@Inject private RecommendService recommendService;
	@Inject private UserStatusDao userStatusDao;
	@Inject private FeedbackDao feedbackDao;
	@Inject private ArticleTypeDao articleTypeDao;
	@Inject private TrendDao trendDao;
	@Inject private TrendArticleDao trendArticleDao;
	@Inject private AreaArticleDao areaArticleDao;
	private ItemMinhashDao imhDao;
	
	@Inject
	public ArticleResource(Datastore ds, ShardedJedisPool pool) {
		imhDao = new ItemMinhashDao(ds);
	}
	
	@GET @Path("recListView")
	@GZIP
	public ExtView<ArticleVO>[] getRecListView(@HeaderParam("userId") long loginUserId, @QueryParam("id") @DefaultValue("-1") long id,
			@QueryParam("algo") @DefaultValue("HeatDiffusion") String algo) throws ApiException {
		try {
			if (id == -1) id = loginUserId;
			List<TopItem> items = recommendService.recommend(algo, id);
			if (items.isEmpty()) return new ExtView[0];
			
			List<Long> ids = toIds(items);
			List<Article> articles = getArticleDatas(ids);
			return ArticleVO.makeDetail(items, articles, readDao.findReadIds(loginUserId, ids), likeDao.findLikeIds(loginUserId, ids));
		} catch (Exception e) {
			log.warn("Could not recommend for user: " + loginUserId, e);
			throw new ApiException(ApiException.RECOMMEND_ERROR, e);
		}
	}

	private List<Article> getArticleDatas(List<Long> ids) {
		return articleDao.get(ids, "id", "title", "summary", "thumbnail", "fromIcon", "from", "url", "category", "createAt");
	}
	
	@GET @Path("recList")
	@GZIP
	public ArticleVO[] getRecList(@HeaderParam("userId") long loginUserId, @QueryParam("id") @DefaultValue("-1") long id,
			@QueryParam("count") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page) throws ApiException {
		try {
			if (id == -1) id = loginUserId;
			UserStatus userStatus = userStatusDao.getOrNew(id);
			
			int start = page * count - count;
			List<TopItem> items = recommendService.recommend(id, userStatus.getLastRecommendArticleTime(), start, count);
			log.info("request range=[" + start + ", " + (start + count) + "), response len=" + items.size());
			if (items.isEmpty()) return new ArticleVO[0];
			
			List<Long> ids = toIds(items);
			List<Article> articles = getArticleDatas(ids);
			log.info("ids size=" + ids.size() + ", articles size=" + articles.size());
			
			long maxRecTime = recommendService.getMaxRecTime(id);
			if (maxRecTime > userStatus.getLastRecommendArticleTime()) {
				userStatus.setLastRecommendArticleTime(maxRecTime);
				userStatusDao.save(userStatus);
			}
			
			ArticleVO[] result = ArticleVO.make(items, articles, readDao.findReadIds(loginUserId, ids), likeDao.findLikeIds(loginUserId, ids));
			if (result.length > count) result = Arrays.copyOf(result, count);
			return result;
		} catch (Exception e) {
			log.warn("Could not recommend for user: " + loginUserId, e);
			throw new ApiException(ApiException.RECOMMEND_ERROR, e);
		}
	}
	
	private static List<Long> toIds(List<TopItem> items) {
		List<Long> ids = new ArrayList<Long>();
		for (TopItem item: items) {
			ids.add(item.getItemId());
		}
		return ids;
	}
	
	@GET @Path("trend")
	@PermitAll
	public Trend[] getTrend(@QueryParam("count") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page) {
		List<Trend> trendList = trendDao.findPop(count, page);		
		Trend[] trends = new Trend[trendList.size()];
		for(int i = 0; i < trends.length; i++){
			trends[i] = trendList.get(i);
		}		                         
		return trends;
	}
	
	@GET @Path("trendArticles")
	@PermitAll
	public ArticleVO[] getTrendArticle(@QueryParam("trendid") long trendId,
			@QueryParam("count") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page, @QueryParam("userId") @DefaultValue("-1") long loginUserId){
		List<Long> articleIds = trendArticleDao.findItemsFromTrend(trendId, count, page);
		List<Article> articles = getArticleDatas(articleIds);
		return ArticleVO.make(articles);
	}
	
	@GET @Path("geo") 
	@PermitAll
	public ArticleContent[] getGeoArticle(@QueryParam("lon") double longitude, @QueryParam("lat") double latitude, @QueryParam("pageSize") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page, @QueryParam("category") @DefaultValue("-1") int cid){
		GeoInfo geoInfo = GeoUtil.getNearestGeoInfo(longitude, latitude);
		String province = geoInfo.getProvince();
		if(!province.isEmpty()){
			List<Long> articleIds = areaArticleDao.findProvinceItems(province, count, page, cid);	
			List<Article> articles = getArticleDatas(articleIds);
			return ArticleContent.make(articles);
		} else {
			return new ArticleContent[0];
		}
	}
	
	
	@GET @Path("articletype")
	@PermitAll
	public ArticleType[] getArticleType(@QueryParam("count") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page) {
		
		List<ArticleType> articleTypes = articleTypeDao.findPop(count, page);
		
		ArticleType[] results = new ArticleType[articleTypes.size()];
		for(int i = 0; i < results.length; i++){
			results[i] = articleTypes.get(i);
		}
		
		return results;
	}
	
	@GET @Path("likeList")
	public ArticleVO[] getLikeList(@HeaderParam("userId") long loginUserId, @QueryParam("id") @DefaultValue("-1") long id,
			@QueryParam("sinceId") @DefaultValue("-1") long sinceId,
			@QueryParam("maxId") @DefaultValue("-1") long maxId,
			@QueryParam("count") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page) {
		if (id == -1) id = loginUserId;
		List<Long> articleIds = likeDao.findLikeIds(id, new Page(sinceId, maxId, count, page));
		List<Article> articles = new ArrayList<Article>();
		for (Article a: articleDao.get(articleIds)) {
			if (a != null) articles.add(a);
		}
		return ArticleVO.make(articles, readDao.findReadIds(loginUserId, articleIds), likeDao.findLikeIds(loginUserId, articleIds));
	}
	
	@POST @Path("like")
	public Map<String, Object> like(@HeaderParam("userId") long loginUserId, @FormParam("id") long id, @FormParam("feedback") String feedback) throws IOException, ApiException {
		try {
			likeDao.addLike(loginUserId, id);
			Preference pref = new Preference();
			pref.setUserId(loginUserId);
			pref.setPageId(id);
			pref.setCreateAt(System.currentTimeMillis());
			prefDao.save(pref, WriteConcern.NORMAL);
			
			ItemMinhashUtil.updateItemMinhash(imhDao, userDao, loginUserId, id);
			
			if (!StringUtils.isBlank(feedback)) {
				Feedback fb = new Feedback();
				fb.setUserId(loginUserId);
				fb.setArticleId(id);
				fb.setFeedback(feedback);
				fb.setCreateAt(System.currentTimeMillis());
				feedbackDao.save(fb);
			}
			
			return ExtView.from("sourceId", id);
		} catch (MongoException.DuplicateKey e) {
			log.warn(loginUserId + " like already " + id, e);
			throw new ApiException(ApiException.DUPLICATE, "已经喜欢过" + id, e);
		}
	}
	
	@POST @Path("dislike")
	public Map<String, Object> dislike(@HeaderParam("userId") long loginUserId, @FormParam("id") long id) throws IOException {
		likeDao.removeLike(loginUserId, id);
		return ExtView.from("sourceId", id);
	}
	
	@GET @Path("popList")
	@PermitAll
	@GZIP
	public ArticleVO[] getPopList(@HeaderParam("userId") long loginUserId,	
			@QueryParam("cid") @DefaultValue("-1") int cid,
			@QueryParam("count") @DefaultValue("20") int count,
			@QueryParam("page") @DefaultValue("1") int page) {
		List<Article> articles = null;
		if (cid > 0) {
			articles = articleDao.findPop(Category.getCategory(cid), count * 2, page);
		} else {
			articles = articleDao.findPop(count * 2, page);
		}
		List<Long> ids = new ArrayList<Long>();
		for (Article a: articles) ids.add(a.getId());
		return ArticleVO.make(articles, readDao.findReadIds(loginUserId, ids), likeDao.findLikeIds(loginUserId, ids), count, System.currentTimeMillis());
	}
	
	@POST @Path("markasread")
	public Map<String, Object>[] markRead(@HeaderParam("userId") long loginUserId, @FormParam("id") List<Long> ids, @FormParam("feedback") String feedback) throws ApiException {
		Map<String, Object>[] results = new Map[ids.size()]; 
		for (int i = 0; i < ids.size(); i++) {
			long id = ids.get(i);
			try {
				readDao.addRead(loginUserId, id);
				results[i] = ExtView.from("id", id, "read", true);
				
				if (!StringUtils.isBlank(feedback)) {
					Feedback fb = new Feedback();
					fb.setUserId(loginUserId);
					fb.setArticleId(id);
					fb.setFeedback(feedback);
					fb.setCreateAt(System.currentTimeMillis());
					feedbackDao.save(fb);
				}
			} catch (MongoException.DuplicateKey e) {
				log.warn(loginUserId + " read already " + id, e);
				results[i] = ExtView.from("id", id, "read", true);
			}
		}
		return results;
	}
	
	@POST @Path("markasunread")
	public Map<String, Object>[] markUnread(@HeaderParam("userId") long loginUserId, @FormParam("id") List<Long> ids) {
		Map<String, Object>[] results = new Map[ids.size()]; 
		for (int i = 0; i < ids.size(); i++) {
			long id = ids.get(i);
			readDao.removeRead(loginUserId, id);
			results[i] = ExtView.from("id", id, "read", false);
		}
		return results;
	}
	
	@GET @Path("article")
	@PermitAll
	@GZIP
	public ExtView<ArticleVO> getArticle(@HeaderParam("userId") long loginUserId, @QueryParam("id") long id) {
		Article article = articleDao.get(id);
		boolean read = readDao.findReadIds(loginUserId, Arrays.asList(id)).contains(id);
		boolean like = likeDao.findLikeIds(loginUserId, Arrays.asList(id)).contains(id);
		String cleanContent = ContentCleaner.clean(article.getContent());
		return ExtView.combine(new ArticleVO(article, likeDao.countLikePerArticle(id), commentDao.countComment(id), read, like),
				"related", recommendRelated(article),
				"content", cleanContent);
	}
	
	private List<ArticleSummary> recommendRelated(Article article) {
		List<RelatedItem> items = recommendService.recommendRelated(article.getId(), 5);
		
		HashMap<Long, RelatedItem> id2item = new HashMap<Long, RelatedItem>();
		List<Long> ids = new ArrayList<Long>();
		for (RelatedItem item: items) {
			ids.add(item.articleId);
			id2item.put(item.articleId, item);
		}
		ids = articleDao.filterByStatus(ids);
		List<Article> articles = getArticleDatas(ids);
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
	
	@GET @Path("articleList")
	@GZIP
	public ExtView<ArticleVO>[] getArticleList(@HeaderParam("userId") long loginUserId, @QueryParam("id") List<Long> ids) {
		List<Long> reads = readDao.findReadIds(loginUserId, ids);
		List<Long> likes = likeDao.findLikeIds(loginUserId, ids);
		List<Article> articles = articleDao.get(ids);
		ExtView<ArticleVO>[] results = new ExtView[articles.size()];
		for (int i = 0; i < articles.size(); i++) {
			Article article = articles.get(i);
			long id = article.getId();
			boolean read = reads.contains(id);
			boolean like = likes.contains(id);
			String cleanContent = ContentCleaner.clean(article.getContent());
			results[i] = ExtView.combine(new ArticleVO(article, likeDao.countLikePerArticle(id), commentDao.countComment(id), read, like),
					"related", recommendRelated(article),
					"content", cleanContent);
		}
		return results;
	}
	
	@GET @Path("likeUsers")
	public ExtView<UserVO>[] getLikeUsers(@HeaderParam("userId") long loginUserId, @QueryParam("id") long id) {
		List<Long> userIds = likeDao.findLikeUserIds(id);
		List<User> users = userDao.get(userIds);
		return UserVO.make(followDao, likeDao, users, loginUserId);
	}
}
