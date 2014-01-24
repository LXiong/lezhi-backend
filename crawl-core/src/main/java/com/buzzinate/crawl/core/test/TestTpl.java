package com.buzzinate.crawl.core.test;

import java.io.IOException;
import java.util.HashSet;

import org.jsoup.nodes.Document;

import com.buzzinate.crawl.core.extract.ArticleText;
import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.tpl.TplExtractor;
import com.buzzinate.crawl.core.util.ExtractUtils;
import com.buzzinate.crawl.core.util.FreqTextDetector;

public class TestTpl {
	public static void main(String[] args) throws IOException {
		String[] urls = new String[] {
				"http://www.infoq.com/cn/news/2011/12/ska-ibm",
				"http://www.infoq.com/cn/news/2011/12/F-sharp-numeric",
				"http://www.infoq.com/cn/news/2011/12/date4j-vs-joda-time",
				"http://www.infoq.com/cn/news/2012/02/worddocgenerator"
//				"http://blog.csdn.net/bitfan/article/details/7005005",
//				"http://blog.csdn.net/zhongguoren666/article/details/6999420",
//				"http://blog.csdn.net/ydj9931/article/details/7028400"
//				"http://www.tianya.cn/publicforum/content/develop/1/567388.shtml",
//				"http://www.tianya.cn/publicforum/content/develop/1/844680.shtml",
//				"http://www.tianya.cn/publicforum/content/develop/1/416826.shtml"
//				"http://tech.sina.com.cn/i/2011-12-06/01036430542.shtml",
//				"http://tech.sina.com.cn/it/2011-12-06/07116432380.shtml",
//				"http://tech.sina.com.cn/t/2011-12-06/08296433125.shtml"
//				"http://bbs.hqjr.cn/forum.php?mod=viewthread&tid=437717&extra=page%3D1",
//				"http://bbs.hqjr.cn/forum.php?mod=viewthread&tid=437716&extra=page%3D1",
//				"http://bbs.hqjr.cn/forum.php?mod=viewthread&tid=437710&extra=page%3D1"
//				"http://www.socialbeta.cn/articles/lean-finance-model-venture-capital.html",
//				"http://www.socialbeta.cn/articles/8-small-business-social-media-tips.html",
//				"http://www.socialbeta.cn/articles/niche-social-marketing-2011.html"
//				"http://bbs.taobao.com/catalog/thread/14725016-255391787.htm",
//				"http://bbs.taobao.com/catalog/thread/454001-255102747.htm",
//				"http://bbs.taobao.com/catalog/thread/10394505-255193162.htm"
//				"http://www.cuba.com.cn/news_show.php?NewsID=16882",
//				"http://www.cuba.com.cn/news_show.php?NewsID=16703",
//				"http://www.cuba.com.cn/news_show.php?NewsID=16153"
//				"http://n.easou.com/show.m?doc=61616377&pm=1_2&wver=c&esid=E6HaHlaUM4M",
//				"http://n.easou.com/show.m?doc=61595859&pm=1_2&wver=c&esid=E6HaHlaUM4M",
//				"http://n.easou.com/show.m?doc=61573755&pm=1_2&wver=c&esid=E6HaHlaUM4M"
//				"http://news.dayoo.com/guangzhou/201112/23/73437_21131669.htm",
//				"http://news.dayoo.com/guangzhou/201112/09/73437_20875972.htm",
//				"http://news.dayoo.com/guangzhou/201112/23/73437_21131555.htm"
//				"http://www.sketchupbar.com/forum.php?extra=page%3D1&mod=viewthread&tid=31980&bsh_bid=35461786",
//				"http://www.sketchupbar.com/thread-6920-1-1.html",
//				"http://www.sketchupbar.com/thread-22196-1-1.html"
//				"http://www.jiaju.com/shop101287/"
//				"http://www.36kr.com/p/70573.html"
//				"http://www.dcci.com.cn/report/view/id/692/cid/6"
//				"http://bbs.21ccom.net/thread-13277-1-1.html"
//				"http://blog.sina.com.cn/s/blog_4701280b01017ijd.html"
//				"http://www.rmfans.cn/news/2011-12-12/10784.html"
//				"http://bbs2.mowo.cn/viewthread.php?tid=74192&fromuid=50224"
//				"http://comment.news.163.com/news_shehui7_bbs/7L6GSRL100011229.html"
//				"http://www.36kr.com/p/71376.html"
//				"http://www.hxpai.com/Photo/view/id/353370"
//				"http://www.yangtse.com/system/2011/08/30/003828757.shtml"
//				"http://fashion.tom.com/1112/1910/AAAX9SRP01AUAAAB.html"
//				"http://www.sarft.net/a/37160.aspx"
//				"http://bbs.pal-club.com.cn/forum.php?mod=viewthread&tid=28061&page=10612#pid1751212"
//				"http://10juhua.com/2952"
//				"http://www.ahhouse.com/topic/2011/ahlszk10/"
//				"http://bbs.d2d.cn/thread-77802-1-1.html"
//				"http://www.pmcaff.com//forum.php?mod=viewthread&tid=4942&extra="
//				"http://bbs.gufengquan.com/thread-183-1-1.html"
//				"http://wap.vivame.cn/fenxiang/reader/?articleid=71a627c2-dbde-4737-9244-fb442620c684&magid=2038&pagenum=1"
//				"http://bbs.bingchengwang.com/thread-12900436-1-1.html"
//				"http://group.9sky.com/Forum/Topic/76/2463017/1"
//				"http://bbs.hqjr.cn/forum.php?mod=viewthread&tid=561657"
//				"http://market.360buy.com/hd/smarthcjc/index.html"
//				"http://wallstreetcn.com/node/9561"
//				"http://www.share5156.com/i/89921"
//				"http://bbs.hiapk.com/thread-3221612-1-1.html"
//				"http://article.yeeyan.org/view/243359/253855"
//				"http://blog.jobbole.com/14164/"
//				"http://www.leggetter.co.uk/real-time-web-technologies-guide"
//				"http://tech110.blog.51cto.com/438717/737865"
//				"http://blogread.cn/it/article.php?id=4948&f=sinat",
//				"http://blogread.cn/it/article.php?id=5019&f=sinat",
//				"http://blogread.cn/it/article.php?id=5076&f=sinat"
//				"http://www.williamlong.info/archives/2397.html",
//				"http://www.williamlong.info/archives/394.html"
//				"http://en.wikibooks.org/wiki/Haskell/Applicative_Functors"
//				"http://www.ifanr.com/78052"
//				"http://sports.sina.com.cn/cba/2012-03-24/03355993838.shtml"
//				"http://sports.163.com/photoview/00MK0005/79060.html"
//				"http://book.ifeng.com/shuhua/detail_2012_03/26/13443949_0.shtml"
//				"http://www.chinaxwcb.com/2012-05/17/content_243112.htm"
		};
		
		Document[] docs = new Document[urls.length];
		
		FreqTextDetector freqTextDetector = new FreqTextDetector(); 
		for (int i = 0; i < docs.length; i++) {
			String url = urls[i];
			docs[i] = PageFetcher.fetch(url).getDoc();
			ExtractUtils.preClean(docs[i].body(), url);
			ExtractUtils.detectFreqText(docs[i].body(), freqTextDetector);
			freqTextDetector.shift();
		}
		
		HashSet<String> freqTexts = new HashSet<String>(freqTextDetector.freqSiteText(docs.length * 8 / 11));
		for (int i = 0; i < docs.length; i++) {
			ArticleText text = TplExtractor.extract(urls[i], docs[i].body(), freqTexts);
			
			System.out.println("---------------------");
			System.out.println(text.hasError());
			System.out.println(text.getTitle());
			System.out.println(text.getThumbnail());
//			System.out.println(text.getContent());
			System.out.println("---------------------");
//			String content = text.getContent();
//			if (content != null && content.length() > 2048) content = content.substring(0, 2048) + "...";
//			System.out.println(content);
		}
	}

	
}