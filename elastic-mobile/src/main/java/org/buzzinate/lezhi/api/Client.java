package org.buzzinate.lezhi.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.buzzinate.lezhi.util.SignatureUtil;
import org.buzzinate.lezhi.util.StringUtils;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IgnoreIndices;
import org.elasticsearch.common.UUID;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import com.alibaba.fastjson.JSON;

public class Client {
	private static final String CLUSTER_NAME = "mobile";
	private static final String INDEX_PREFIX = "content_mobile_";
	private static final int ONE_DAY = 1000 * 3600 * 24;
	private static final DateFormat mf = new SimpleDateFormat("yyyy-MM");
	private static final String TYPE = "doc";
	public static final String[] fields = new String[]{"id", "url", "title", "signature", "thumbnail", "lastModified"};
	
	private final org.elasticsearch.client.Client client;
	
	public Client(String host) {
		this(Arrays.asList(host));
	}
	
	public Client(List<String> hosts) {
		this(hosts, 9300);
	}
	
	public Client(List<String> hosts, int port) {
		 Settings settings = setNodename(ImmutableSettings.settingsBuilder())
					.put("http.enabled", "false")
					.put("transport.tcp.port", "9300-9400")
					.put("discovery.zen.ping.unicast.hosts", StringUtils.join(hosts, ",")).build();
		Node node = NodeBuilder.nodeBuilder().clusterName(CLUSTER_NAME).client(true).settings(settings).node();
		client = node.client();
	}
	
	public String state() {
		return client.admin().cluster().state(new ClusterStateRequest()).actionGet().state().toString();
	}
	
	private ImmutableSettings.Builder setNodename(ImmutableSettings.Builder builder) {
		String uuid = UUID.randomBase64UUID();
		try {
			return builder.put("node.name", InetAddress.getLocalHost().getHostName() + "-" + uuid);
		} catch(UnknownHostException e) {
			return builder.put("node.name", uuid);
		}
	}
	
	public void bulkAdd(List<Doc> docs) {
		if (docs.isEmpty()) return;
		
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (Doc doc: docs) {
			doc.title = StringUtils.escapeJson(doc.title);
			String index = INDEX_PREFIX + mf.format(new Date(doc.lastModified));
			String json = JSON.toJSONString(doc, false);
			bulkRequest.add(client.prepareIndex(index, TYPE, SignatureUtil.signature(doc.url)).setSource(json));
		}
		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
		if (bulkResponse.hasFailures()) {
			throw new RuntimeException(bulkResponse.buildFailureMessage());
		}
	}
	
	public Doc get(UrlTime ut) {
		String index = INDEX_PREFIX + mf.format(new Date(ut.lastModified));
		GetResponse resp = client.prepareGet(index, TYPE, SignatureUtil.signature(ut.url)).execute().actionGet();
		if (resp.exists()) return fromSource(resp.getSource());
		else return null;
	}
	
	public Map<String, Doc> get(List<UrlTime> uts) {
		HashMap<String, Doc> url2docs = new HashMap<String, Doc>();
		if (uts.size() == 0) return url2docs;
		if (uts.size() == 1) {
			Doc doc = get(uts.get(0));
			if (doc != null) url2docs.put(doc.url, doc);
			return url2docs;
		}
		
		MultiGetRequestBuilder mget = client.prepareMultiGet();
		for (UrlTime ut: uts) {
			String index = INDEX_PREFIX + mf.format(new Date(ut.lastModified));
			mget.add(index, TYPE, SignatureUtil.signature(ut.url));
		}
		MultiGetResponse resps = mget.execute().actionGet();
		for (MultiGetItemResponse resp : resps) {
			if (resp.getResponse() != null && resp.getResponse().isExists()) {
				Doc d = fromSource(resp.getResponse().getSource());
				url2docs.put(d.url, d);
			}
		}
		return url2docs;
	}
	
	public List<HitDoc> query(String signature, String keyword, long mintime, int maxWord, int maxDoc) {
		long now = System.currentTimeMillis();
		Query q = new Query(signature, keyword, now, maxWord);
		
		Map<String, Query> querymap = new HashMap<String, Query>();
		querymap.put("lezhi_query", q);
		Map<String, Filter> filtermap = new HashMap<String, Filter>();
		filtermap.put("lezhi_timerange", new Filter(mintime));
		
//		System.out.println(JSON.toJSONString(filtermap, false));
//		System.out.println(JSON.toJSONString(querymap, false));
		
		SearchResponse resp = client.prepareSearch(indecis4query(now)).setTypes(TYPE).setSearchType(SearchType.QUERY_AND_FETCH)
				.setQuery(JSON.toJSONString(querymap, false)).setFilter(JSON.toJSONString(filtermap, false))
				.setIgnoreIndices(IgnoreIndices.MISSING).addFields(fields).setFrom(0).setSize(maxDoc).execute().actionGet();
		
		List<HitDoc> docs = new ArrayList<HitDoc>();
//		System.out.println("total " + resp.getHits().getTotalHits() + " docs, time=" + resp.getTookInMillis());
		for (SearchHit sh: resp.hits()) {
//			System.out.println("id: " + sh.getId() + " score: " + sh.getScore());
			HitDoc doc = fromField(sh.fields());
			doc.score = sh.getScore();
			docs.add(doc);
		}
		return docs;
	}
	
	private String[] indecis4query(long lastModified) {
		String thismonth = mf.format(new Date(lastModified));
		String startmonth = mf.format(new Date(lastModified - ONE_DAY * 20));
		if (thismonth.equals(startmonth)) return new String[] { INDEX_PREFIX + thismonth};
		else return new String[] { INDEX_PREFIX + startmonth, INDEX_PREFIX + thismonth};
	}
	
	private HitDoc fromField(Map<String, SearchHitField> fields) {
		long id = ((Number)fields.get("id").getValue()).longValue();
		String url = fields.get("url").getValue();
		String title = fields.get("title").getValue();
		String signature = fields.get("signature").getValue();
		String thumbnail = "";
		if (fields.containsKey("thumbnail")) thumbnail = fields.get("thumbnail").getValue();
		long lastModified = fields.get("lastModified").getValue();
		return new HitDoc(id, url, title, signature, thumbnail, lastModified);
	}
	
	private Doc fromSource(Map<String, Object> source) {
		long id = ((Number)source.get("id")).longValue();
		String url = (String)source.get("url");
		String title = (String)source.get("title");
		String signature = (String)source.get("signature");
		String thumbnail = (String)source.get("thumbnail");
		String keyword = (String)source.get("keyword");
		long lastModified = ((Number)source.get("lastModified")).longValue();
		return new Doc(id, url, title, signature, thumbnail, keyword, lastModified);
	}
	
	public void close() {
		client.close();
	}
}