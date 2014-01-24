package com.buzzinate.crawl.core.extract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;

import com.buzzinate.crawl.core.fetch.PageFetcher;

public class TextExtractorTest {

	@Test
	public void test() {
		Data[] datas = new Data[] {
			new Data( "http://video.sina.com.cn/v/b/54334102-1243629662.html", true),
			new Data("http://news.hsw.cn/system/2011/06/10/050985618.shtml", "西安晚报讯", "连续几位乘客要求女孩让座", "素质问题啊。"),
			new Data("http://www.infoq.com/cn/articles/cyw-evaluate-seachengine-result-quality", "搜索引擎结果的好坏与否，体现在业界所称的在相关性", "张凯峰</a>对 本文的审校"),
			new Data("http://www.ifanr.com/41613", "家里的一台 Compaq 电脑主要是用来玩红星大战", "完整版文档请点击"),
			new Data("http://blog.sina.com.cn/s/blog_3f7968920100rwwv.html", "穆迪投资者服务公司(Moody's)周四宣布", "黄金操盘手</a>.黄不定"),
			new Data("http://news.ifeng.com/photo/hdnews/detail_2011_05/26/6652621_0.shtml", true, "江西省新建县南矶乡红卫村", "[详细]"),
			new Data("http://www.linkchic.com/item/49483.html", true, "复古亮橘色柔软垂坠感长款工字背心"),
			new Data("http://www.199it.com/archives/2011060811193.html", "公司5月份在线游戏的销售额增长了22.7%", "并稳定的发展"),
			new Data("http://tk.mop.com/s/4dde447362f1b9c3fa256367.htm", "微语录：有一种借口叫年轻", "用他的生..."),
			new Data("http://luo.bo/10414/", true, "超暴力测试全球最坚固防弹Marauder越野车", "Hammond 企图摧毁 Marauder 的计划又能否得逞呢？"),
			new Data("http://tv.sohu.com/s2011/wodekangzhan/", "我们做口述历史是给后人留下一个千百年以后还和先人内心对话的机会", "这是最真实的。"),
			new Data("http://learning.sohu.com/20110609/n309706735_5.shtml", true, "来源：", "搜狐教育", "责任编辑：刘丽虹"),
			new Data("http://cn.wsj.com/gb/20110802/tec113600.asp?source=whatnews2", "博服务Twitter Inc.说", "及为什么不能错过这个更多参与其未来的机会。"),
			new Data("http://tieba.baidu.com/p/620836135?pn=83", "我终於等到你这句", "铉民是啥"),
			new Data("http://www.gzbbs.com/thread-423174-1-1.html", "导语：每年因为医生误诊", "保护好自己的肠道")
		};
		for (Data data: datas) {
			try {
				Document doc = PageFetcher.fetch(data.url).getDoc();
				ArticleText text = TextExtractor.extract(doc.body(), doc.title(), data.url);
				ensure(data.url, text.getContent(), data.keyTexts);
				Assert.assertEquals("crawl " + data.url, data.hasError, text.hasError());
			} catch (IOException e) {
				// Ignore
			}
		}
	}
	
	private void ensure(String url, String text, List<String> keyTxts) {
		Assert.assertTrue(url + " text should not null", text != null);
		for (String keyTxt: keyTxts) {
			Assert.assertTrue(url + " should contain " + keyTxt, text.contains(keyTxt));
		}
	}
	
	private static class Data {
		public String url;
		public boolean hasError = false;
		public List<String> keyTexts = new ArrayList<String>();
		
		public Data(String url, String... keyTxts) {
			this(url, false, keyTxts);
		}
		
		public Data(String url, boolean hasError, String... keyTxts) {
			this.url = url;
			this.hasError = hasError;
			for (String keyTxt: keyTxts) keyTexts.add(keyTxt);
		}
	}
}
