package com.buzzinate.weibo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import weibo4j2.Timeline;
import weibo4j2.Weibo;
import weibo4j2.http.Response;
import weibo4j2.model.Paging;
import weibo4j2.model.PostParameter;
import weibo4j2.model.Status;
import weibo4j2.model.Trend;
import weibo4j2.model.Trends;
import weibo4j2.model.WeiboException;
import weibo4j2.org.json.JSONArray;
import weibo4j2.org.json.JSONObject;
import weibo4j2.util.WeiboConfig;

import com.buzzinate.common.util.Constants;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class WeiboExt extends Weibo {
	private static final long serialVersionUID = -1749888278410088077L;

	private static Logger log = Logger.getLogger(WeiboExt.class);

	private static String filtTrendStr = "＋";

	// 新浪话题搜索结果有限制，一次搜索最多50条，总的搜索结果最多200条
	private static final int MAX_TREND_PER_SEARCH = 50;
	private static final int MAX_TREND_PAGE = 4;

	private long expiredTime = 0;

	// 普通授权，每用户每应用150次/小时
	@Inject(optional = true)
	@Named(Constants.KEY_WEIBO_INTERVAL)
	private static int ACCESS_INTERVAL = 1000;

	private long lastAccessTime = System.currentTimeMillis();

	public WeiboExt(long expiredTime) {
		this.expiredTime = expiredTime;
	}

	public boolean isExpired() {
		return System.currentTimeMillis() >= expiredTime + Constants.ONE_DAY;
	}

	public List<Trends> getTrends() throws WeiboException {

		String baseUrl = WeiboConfig.getValue("baseURL") + "trends/";

		String hourlyQurl = baseUrl + "hourly.json";
		String dailyQurl = baseUrl + "daily.json";
		String weeklyQurl = baseUrl + "weekly.json";

		List<Trends> res = new ArrayList<Trends>();

		ensureAccessInterval();
		List<PostParameter> params = new ArrayList<PostParameter>();
		System.out.println(client.get(hourlyQurl, params.toArray(new PostParameter[0])));
		res.addAll(Trends.constructTrendsList(client.get(hourlyQurl, params.toArray(new PostParameter[0]))));
		res.addAll(Trends.constructTrendsList(client.get(dailyQurl, params.toArray(new PostParameter[0]))));
		res.addAll(Trends.constructTrendsList(client.get(weeklyQurl, params.toArray(new PostParameter[0]))));
		return res;
	}
	
	public Location getLocation(Double longitude, Double latitude) throws WeiboException {
		String baseUrl = WeiboConfig.getValue("baseURL") + "location/geo/geo_to_address.json";
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("coordinate", longitude+","+latitude));
		return Location.constructCounts(client.get(baseUrl, params.toArray(new PostParameter[0])));
	}

	/**
	 * 批量获取某条话题的微博 , 详细API参见 http://open.weibo.com/wiki/2/search/topics
	 * 
	 * @param trend
	 * @return
	 */
	public List<Status> getTrendTimeline(Trend trend) {

		List<Status> statuses = new ArrayList<Status>();
		String qUrl = WeiboConfig.getValue("baseURL") + "search/topics.json";
		for (int page = 1; page <= MAX_TREND_PAGE; page++) {
			List<PostParameter> params = new ArrayList<PostParameter>();
			String filtedQuery = trend.getQuery().replace(filtTrendStr, "");
			params.add(new PostParameter("count", MAX_TREND_PER_SEARCH));
			params.add(new PostParameter("q", filtedQuery));
			params.add(new PostParameter("page", page));
			try {
				ensureAccessInterval();
				Response res = client.get(qUrl, params.toArray(new PostParameter[0]));
				statuses.addAll(Status.constructWapperStatus(res).getStatuses());
			} catch (Exception e) {
				log.info("getTrendTimeline Exception: " + e.getMessage() + " trend.Query(): " + trend.getQuery());
				return new ArrayList<Status>();
			}
		}

		return statuses;
	}
	
	/**
	 * 批量获取某条话题的微博 , 详细API参见 http://open.weibo.com/wiki/2/search/topics
	 * 
	 * @param trend
	 * @return
	 */
	public List<Status> getTopicTimeline(String query) {

		List<Status> statuses = new ArrayList<Status>();
		String qUrl = WeiboConfig.getValue("baseURL") + "search/topics.json";
		for (int page = 1; page <= MAX_TREND_PAGE; page++) {
			List<PostParameter> params = new ArrayList<PostParameter>();
			String filtedQuery = query.replace(filtTrendStr, "");
			params.add(new PostParameter("count", MAX_TREND_PER_SEARCH));
			params.add(new PostParameter("q", filtedQuery));
			params.add(new PostParameter("page", page));
			try {
				ensureAccessInterval();
				Response res = client.get(qUrl, params.toArray(new PostParameter[0]));
				statuses.addAll(Status.constructWapperStatus(res).getStatuses());
			} catch (Exception e) {
				log.info("getTopicsTimeline Exception: " + e.getMessage() + " trend.Query(): " + query);
				return new ArrayList<Status>();
			}
		}

		return statuses;
	}

	public String shortUrlExpand(String url) throws WeiboException {
		List<String> urls = new ArrayList<String>();
		urls.add(url);
		return doShortUrlExpand(urls).get(urls);
	}

	public Map<String, String> shortUrlExpand(List<String> urls) throws WeiboException {
		int batchSize = 20, totalSize = urls.size();
		Map<String, String> urlMap = new HashMap<String, String>();
		for (int i = 0; batchSize * i < totalSize; i++) {
			int start = batchSize * i, end = batchSize * (i + 1);
			if (end > totalSize) {
				end = totalSize;
			}
			urlMap.putAll(doShortUrlExpand(urls.subList(start, end)));
		}
		return urlMap;
	}

	/**
	 * 批量获取n条微博消息的评论数和转发数。要获取评论数和转发数的微博消息ID列表，用逗号隔开 一次请求最多可以获取100条微博消息的评论数和转发数
	 * 
	 * @param ids
	 *            ids a string, separated by commas
	 * @return a list of counts objects
	 * @throws WeiboException
	 *             when Weibo service or network is unavailable
	 * @since Weibo4J 1.1220
	 * @see <a
	 *      href="http://open.t.sina.com.cn/wiki/index.php/Statuses/counts">Statuses/counts</a>
	 */
	public List<Count> getCountsExt(String ids) throws WeiboException {
		ensureAccessInterval();
		return Count.constructCounts(client.get(WeiboConfig.getValue("baseURL") + "statuses/counts.json", new PostParameter[] { new PostParameter("ids", ids) }));
	}

	public List<Status> getShortUrlStatuses(List<String> shortUrls, int count) throws WeiboException {
		ensureAccessInterval();
		String url = WeiboConfig.getValue("baseURL") + "short_url/share/statuses.json";
		List<PostParameter> params = new ArrayList<PostParameter>();
		for (String u : shortUrls) {
			params.add(new PostParameter("url_short", u));
		}
		params.add(new PostParameter("count", count));
		Response res = client.get(url, params.toArray(new PostParameter[0]));

		try {
			JSONArray statuses = res.asJSONObject().getJSONArray("share_statuses");
			List<Status> results = new ArrayList<Status>();
			for (int i = 0; i < statuses.length(); i++) {
				JSONObject status = statuses.getJSONObject(i);
				results.add(new Status(status));
			}
			return results;
		} catch (Exception e) {
			throw new WeiboException(e);
		}
	}

	private Map<String, String> doShortUrlExpand(List<String> urls) throws WeiboException {
		ensureAccessInterval();
		String url = WeiboConfig.getValue("baseURL") + "short_url/expand.json";
		List<PostParameter> params = new ArrayList<PostParameter>();
		for (String u : urls) {
			params.add(new PostParameter("url_short", u));
		}
		Response res = client.get(url, params.toArray(new PostParameter[0]));

		try {
			JSONArray list = res.asJSONObject().getJSONArray("urls");
			Map<String, String> urlMap = new HashMap<String, String>();
			for (int i = 0; i < list.length(); i++) {
				JSONObject obj = list.getJSONObject(i);
				String urlShort = obj.getString("url_short");
				String urlLong = obj.getString("url_long");
				int type = obj.getInt("type");
				boolean result = obj.getBoolean("result");
				if (type == 0 && result) {
					if (urlShort.startsWith("http://sinaurl.cn/")) {
						urlShort = urlShort.substring(18);
					} else if (urlShort.startsWith("http://t.cn/")) {
						urlShort = urlShort.substring(12);
					}
					urlMap.put(urlShort, urlLong);
				} else {
					log.warn("ignore " + urlShort + " ==> " + urlLong + ", type=" + type + ", result=" + result);
				}
			}
			return urlMap;
		} catch (Exception e) {
			throw new WeiboException(e);
		}
	}

	public List<Status> getUserTimeline(String uid, Paging paging) throws WeiboException {
		ensureAccessInterval();
		return new Timeline().getUserTimelineByUid(uid, paging, 0, 0).getStatuses();
	}

	public List<Status> getFriendsTimeline(Paging paging) throws WeiboException {
		ensureAccessInterval();
		return new Timeline().getFriendsTimeline(0, 0, paging).getStatuses();
	}

	public List<Status> getreposttimeline(String tweetId, Paging paging) throws WeiboException {
		ensureAccessInterval();
		try {
			return new Timeline().getRepostTimeline(tweetId, paging).getStatuses();
		} catch (Exception e) {
			return new ArrayList<Status>();
		}
	}

	private void ensureAccessInterval() {
		long d = lastAccessTime + ACCESS_INTERVAL - System.currentTimeMillis();
		if (d > 0) {
			log.info("#### sleep " + d);
			try {
				Thread.sleep(d);
			} catch (InterruptedException e) {
			} // Ignore
		}
		lastAccessTime = System.currentTimeMillis();
	}
}