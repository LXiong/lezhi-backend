package com.buzzinate.crawl.core.fetch;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;

import com.buzzinate.crawl.core.detect.CharsetDetector;
import com.buzzinate.crawl.core.detect.CharsetMatch;
import com.buzzinate.crawl.core.util.MultiCharsetDetector;

@SuppressWarnings("deprecation")
public class PageFetcher {
	private static final SimpleDateFormat LAST_MOD_DF = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
	private static HttpClient httpClient = null;
	
	static {
		httpClient = createHttpClient();		
	}
	
	public static void main(String[] args) throws IOException {
//		String url = "http://et.21cn.com/movie/xinwen/huayu/2011/11/24/9915003.shtml";
//		String url = "http://www.techfrom.com/19636.html";
//		String url = "http://www.tianya.cn/publicforum/content/develop/1/844680.shtml";
//		String url = "http://www.iteye.com/news/24617";
//		String url = "http://sh.house.sina.com.cn/news/2012-04-23/1751146984.shtml";
//		String url = "http://akb48blog.org/team-k/kikushi-ayaka/98135.html";
		String url = "http://blog.chinaunix.net/uid-25472972-id-3264452.html";
		Response resp = fetch(url);
		System.out.println(resp.getRealUrl());
		System.out.println(resp.getDoc().title());
		System.out.println(resp.getCharset());
		System.out.println(resp.getDoc().html());
	}
	
	public static Response fetch(String url) throws IOException {
		HttpResponse response = null;
		try {
			HttpGet get = new HttpGet(StringUtils.replace(url, "|", "%7c"));
			//get.addHeader("Accept-Encoding", "gzip");
			get.addHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
			RedirectContext ctx = new RedirectContext(get.getURI());
			response = httpClient.execute(get, ctx);
			HttpEntity entity = response.getEntity();
			
			int statusCode = response.getStatusLine().getStatusCode();			
			
			String realUrl = ctx.getRealUrl().toURL().toExternalForm();
			
			long lastMod = System.currentTimeMillis();
			Header lastModHeader = response.getFirstHeader("Last-Modified");
			if (lastModHeader != null) {
				try {
					lastMod = LAST_MOD_DF.parse(lastModHeader.getValue()).getTime();
				} catch (ParseException e) {
					// Ignore
				}
			}
			
			String contentType = EntityUtils.getContentMimeType(entity);
			Header ce = entity.getContentEncoding();
			InputStream is = entity.getContent();
			if (ce != null && ce.getValue().equalsIgnoreCase("gzip")) {
				is = new GZIPInputStream(is);
			}
			
			byte[] bs = toByteArray(is, entity.getContentLength());
			
			MultiCharsetDetector mcd = new MultiCharsetDetector();
			mcd.addCharset(EntityUtils.getContentCharSet(entity));
			mcd.addCharset(MozillaCharsetDetector.detect(bs));
	        mcd.addCharset(MetaCharsetDetector.detectMetaCharset(bs));
	        
	        CharsetDetector cd = new CharsetDetector();
	        cd.setText(bs);
	        for (CharsetMatch m  : cd.detectTop()) {
	          String coreCharset = m.getName();
	          Double weight = m.getConfidence() / 100.0;
	          
	          mcd.addCharset(coreCharset, weight);
	        }
			
			String charset = mcd.getCharset();
			
			Document doc = null;
			if (charset == null) {
				// Use jsoup to detect charset
				doc = DataUtil.load(new ByteArrayInputStream(bs), charset, realUrl);
				charset = doc.outputSettings().charset().name();
			} else {
				String html = new String(bs, charset);
				doc = Jsoup.parse(html, realUrl);
				doc.outputSettings().charset(charset);
			}
			
			String title = doc.title().replaceAll("<[^>]+>", "").replaceAll("[\\\r\\\n]", "").trim();
			doc.title(title);
			return new Response(statusCode, realUrl, contentType, lastMod, charset, doc);
		} catch (ClientProtocolException e) {
			throw new IOException(e);
		} finally {
			if (response != null) EntityUtils.consume(response.getEntity());
		}
	}
	
	public static long headContentLength(String referer, String url) throws IOException {
		HttpResponse response = null;
		try {
			HttpHead head = new HttpHead(StringUtils.replace(url, "|", "%7c"));
			head.addHeader("Referer", referer);
			RedirectContext ctx = new RedirectContext(head.getURI());
			response = httpClient.execute(head, ctx);
			String contentLength = response.getFirstHeader("Content-Length").getValue();
			if (response.getStatusLine().getStatusCode() >= 400) return 0;
			if (contentLength != null) return Long.parseLong(contentLength);
			return -1;
		} finally {
			if (response != null) EntityUtils.consume(response.getEntity());
		}
	}
	
	public static Thumbnail fetchImage(String referer, String imgsrc) throws IOException {
		HttpResponse response = null;
		try {
			HttpGet get = new HttpGet(StringUtils.replace(imgsrc, "|", "%7c"));
			get.addHeader("Referer", referer);
			response = httpClient.execute(get);
			String contentType = response.getFirstHeader("Content-Type").getValue();
			String format = StringUtils.substringAfter(contentType, "image/");
			BufferedImage img = ImageIO.read(response.getEntity().getContent());
			String contentLength = response.getFirstHeader("Content-Length").getValue();
			return new Thumbnail(img, format, Long.parseLong(contentLength));
		} catch (Exception e) {
			return new Thumbnail(null, null, 0L);
		} finally {
			if (response != null) EntityUtils.consume(response.getEntity());
		}
	}

	private static HttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
		paramsBean.setVersion(HttpVersion.HTTP_1_1);
		paramsBean.setContentCharset("UTF-8");
		paramsBean.setUseExpectContinue(false);
		params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

		params.setParameter("http.useragent", "Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)");
		params.setIntParameter("http.socket.timeout", 20000);

		params.setIntParameter("http.connection.timeout", 30000);

		ConnPerRouteBean connPerRouteBean = new ConnPerRouteBean();
		connPerRouteBean.setDefaultMaxPerRoute(100);
		ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRouteBean);
		ConnManagerParams.setMaxTotalConnections(params, 100);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
		DefaultHttpClient c = new DefaultHttpClient(connectionManager, params);
		RedirectStrategy redirectStrategy = new RedirectStrategy();
		
		c.setRedirectStrategy(redirectStrategy);
		
		return c;
	}
	
	public static byte[] toByteArray(final InputStream instream, long contentLength) throws IOException {
		if (instream == null) return null;

		if (contentLength > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
		}
		int i = (int) contentLength;
		if (i < 0) i = 4096;
		
		ByteArrayBuffer buffer = new ByteArrayBuffer(i);
		byte[] tmp = new byte[4096];
		int l;
		while ((l = instream.read(tmp)) != -1) {
			buffer.append(tmp, 0, l);
		}
		return buffer.toByteArray();
	}
}

class RedirectContext extends BasicHttpContext {
	
	public RedirectContext(URI uri) {
		setRealUrl(uri);
	}

	@Override
	public void setAttribute(String id, Object obj) {
		super.setAttribute(id, obj);
		if (id.equals("RealUrl")) setRealUrl((URI) obj);
	}

	private URI realUrl;
	
	public void setRealUrl(URI realUrl) {
		this.realUrl = realUrl;
	}
	
	public URI getRealUrl() {
		return realUrl;
	}
}

class RedirectStrategy extends DefaultRedirectStrategy {
	@Override
	public URI getLocationURI(final HttpRequest request, final HttpResponse response, final HttpContext context) throws ProtocolException {
		URI uri = super.getLocationURI(request, response, context);
		context.setAttribute("RealUrl", uri);
		return uri;
	}
	
	@Override
	protected URI createLocationURI(final String location) throws ProtocolException {
        try {
            return new URI(StringUtils.replace(location, "|", "%7c"));
        } catch (URISyntaxException ex) {
            throw new ProtocolException("Invalid redirect URI: " + location, ex);
        }
    }
}
