package com.buzzinate.crawl.core.extract;

import java.util.Arrays;
import java.util.List;

/**
 * 抽取出来的正文
 * 
 * @author Brad Luo
 *
 */
public class ArticleText {
	private static List<String> noizeWords = Arrays.asList("文件大小", "MB", "文件", "下载", "地址", "文件下载", "上传", "上传声明", "严禁上传", "反动、暴力、色情、违法及侵权", "Copyright", "ICP备", "ICP证", "版权", "版权所有", "版权说明", "本公司", "关于我们", "联系我们", "诚聘英才", "客户服务热线");
	
	private String url;
	private String title;
	private String content;
	private String thumbnail;
	// 文字数量，比较小，可以认为抽取失败，或者是视频图片类型网页
	private int numWords;
	// 文本稠密度，比较小的话，可以认为抽取失败，或者是视频图片类型网页
	private float textDensity;
	
	public ArticleText(String url, String title, String content, String thumbnail, int numWords, float textDensity) {
		this.url = url;
		this.title = title;
		this.content = content;
		this.thumbnail = thumbnail;
		this.numWords = numWords;
		this.textDensity = textDensity;
	}

	public String getUrl() {
		return url;
	}
	
	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}
	
	public String getThumbnail() {
		return thumbnail;
	}

	public int getNumWords() {
		return numWords;
	}

	public float getTextDensity() {
		return textDensity;
	}
	
	public boolean hasError() {
		if (numWords < 300 && countNoizeWords(content) >= 5) return true;
		return numWords < 100 || (numWords < 200 && textDensity < 0.9f) || (numWords < 500 && textDensity < 0.5f);
	}

	private static int countNoizeWords(String c) {
		int cnt = 0;
		for (String k: noizeWords) {
			if (c.contains(k)) cnt++;
		}
		return cnt;
	}
}
