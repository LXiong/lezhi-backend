package com.buzzinate.crawl.core.extract;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;
import com.buzzinate.crawl.core.util.ExtractUtils;
import com.buzzinate.crawl.core.util.TextUtility;

public class TextExtractor {
	private static Logger log = Logger.getLogger(TextExtractor.class);
	
	private static List<ExtractionProcessor> ExtractionProcessors = Arrays.asList(
			new TitleBasedSnippetNodeProcessor(), 
			new PuncBasedSnippetNodeProcessor(),
			new PuncBasedTextNodeProcessor(),
			new TitleNodeCleanProcessor(),
			new NoizeNodeCleanProcessor(),
			new ThumbnailProcessor()
		);
		
	public static void main(String[] args) throws IOException {
//		String url = "http://video.sina.com.cn/v/b/54334102-1243629662.html";
//		String url = "http://www.infoq.com/cn/news/2011/07/forrester-data-virtualization";
//		String url = "http://www.infoq.com/cn/news/2011/07/Google-Plus";
//		String url = "http://www.infoq.com/cn/articles/cyw-evaluate-seachengine-result-quality";
//		String url = "http://www.infoq.com/cn/interviews/sheehy-riak-cn";
//		String url = "http://news.hsw.cn/system/2011/06/10/050985618.shtml";
//		String url = "http://www.ifanr.com/41613";
//		String url = "http://blog.sina.com.cn/s/blog_3f7968920100rwwv.html";
//		String url = "http://news.ifeng.com/photo/hdnews/detail_2011_05/26/6652621_0.shtml";
//		String url = "http://www.linkchic.com/item/49483.html";
//		String url = "http://www.199it.com/archives/2011060811193.html";
//		String url = "http://tk.mop.com/s/4dde447362f1b9c3fa256367.htm";
		
//		String url = "http://luo.bo/10414/";
//		String url = "http://tv.sohu.com/s2011/wodekangzhan/";
//		String url = "http://v.youku.com/v_show/id_XMjM1MTQzOTQ0.html";
//		String url = "http://learning.sohu.com/20110609/n309706735_5.shtml";
//		String url = "http://tt.mop.com/read_4767955_1_0.html";
//		String url = "http://www.infzm.com/content/61704";
//		String url = "http://tieba.baidu.com/f?kz=1139163449";
//		String url = "http://news.cheshi.com/20110802/380368.shtml";
//		String url = "http://cn.wsj.com/gb/20110802/tec113600.asp?source=whatnews2";
//		String url = "http://tieba.baidu.com/p/620836135?pn=83";
//		String url = "http://pinyin.sogou.com/skins/sv_390020.html?cate9";
//		String url = "http://blog.sina.com.cn/s/blog_467a66b00102drld.html?tj=1";
//		String url = "http://www.techweb.com.cn/internet/2011-08-03/1075938.shtml";
//		String url = "http://www.730700.com/article-20827-1.html";
//		String url = "http://www.eoeandroid.com/forum.php?mod=viewthread&tid=75625&page=1&extra=#pid799669";
//		String url = "http://www.ccb.com/sh/20110729_1311935803.html";
//		String url = "http://www.gzbbs.com/thread-423174-1-1.html";
//		String url = "http://bbs.wzdsb.net/thread-151158-1-1.html";
//		String url = "http://www.anzhuo.cn/thread-120483-1-1.html";
//		String url = "http://www.zoomkobe.cn/thread-73695-1-1.html";
//		String url = "http://bbs.yoka.com/thread-2711124-1-1.html";
//		String url = "http://www.lestalk.org/bbs/forum.php?mod=viewthread&tid=3990&extra=";
//		String url = "http://www.xiaot.com/vbbs/viewthread.php?tid=101873";
//		String url = "http://www.tianya.cn/publicforum/content/develop/1/801027.shtml";
//		String url = "http://wzcs.wzdsb.net/thread-174866-1-1.html";
//		String url = "http://epaper.bjnews.com.cn/html/2011-08/16/content_265330.htm?div=-1";
//		String url = "http://www.iteye.com/topic/816306";
//		String url = "http://news.ifeng.com/gundong/detail_2011_08/19/8536390_0.shtml";
		String url = "http://www.iteye.com/news/23234";
//		String url = "http://www.alibuybuy.com/posts/66175.html";
		
		
		Response resp = PageFetcher.fetch(url);
		Document doc = resp.getDoc();
		System.out.println("status: " + resp.getStatusCode());
		ArticleText text = extract(doc.body(), doc.title(), url);
		System.out.println("extracted: " + text.getContent());
		System.out.println("title: " + text.getTitle());
		System.out.println("image: " + text.getThumbnail());
		System.out.println("word num: " + text.getNumWords());
		System.out.println("text density: " + text.getTextDensity());
		System.out.println("error: " + text.hasError());
	}

	public static ArticleText extract(Element root, String title, String baseUrl) throws IOException {
		ExtractUtils.preClean(root, baseUrl);
		
		title = title.replaceAll("<[^>]+>", "").replaceAll("[\\\r\\\n]", "").trim();
		
		ArticleStat articleStat = new ArticleStat(root, title);
		
		for (ExtractionProcessor p: ExtractionProcessors) p.process(articleStat);
		
		if (articleStat.textNode == null) return new ArticleText(baseUrl, title, "", null, 0, 0);
		
		if (articleStat.thumbnail != null) {
			articleStat.thumbnail = ExtractUtils.checkThumbnailSize(baseUrl, articleStat.thumbnail);
		}
		NodeStat stat = articleStat.getStat(articleStat.textNode);
		int textWordCnt = stat.wordNum;
		
		ExtractUtils.postClean(articleStat.textNode);
		
		String content = ExtractUtils.format(articleStat.textNode);
		int totalLength = TextUtility.countNumWords(content);
		
		return new ArticleText(baseUrl, articleStat.title, content, articleStat.thumbnail, textWordCnt, textWordCnt / (1f + totalLength));
	}
}
