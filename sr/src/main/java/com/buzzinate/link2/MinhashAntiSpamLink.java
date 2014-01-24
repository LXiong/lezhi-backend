package com.buzzinate.link2;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.mahout.math.MurmurHash3;

import com.buzzinate.common.util.MentionUtil;
import com.buzzinate.crawl.core.util.DomainNames;
import com.buzzinate.crawl.core.util.TextUtility;

public class MinhashAntiSpamLink {
	private TreeSet<Integer> mhes = new TreeSet<Integer>();
	private int max;
	
	private static HashSet<String> antiHosts = new HashSet<String>(Arrays.asList("item.taobao.com", "s.click.taobao.com"));
	
	public MinhashAntiSpamLink(int max) {
		this.max = max;
	}
	
	public boolean isAntiHost(String url) {
		String host = DomainNames.safeGetHost(url);
		return antiHosts.contains(host);
	}
	
	public boolean isFull() {
		return mhes.size() >= max;
	}
	
	public void add(String weiboText) throws UnsupportedEncodingException {
		String text = MentionUtil.cleanStatusText(weiboText);
		HashSet<String> ngrams = createNgram(text, 3);
		for (String ngram: ngrams) {
			int h = hash(ngram);
			mhes.add(h);
		}
		if (mhes.size() > max) mhes.pollLast();
	}
	
	public double checkSim(String weiboText) throws UnsupportedEncodingException {
		String text = MentionUtil.cleanStatusText(weiboText);
		HashSet<String> ngrams = createNgram(text, 3);
		HashSet<Integer> hashes = new HashSet<Integer>();
		for (String ngram: ngrams) {
			int h = hash(ngram);
			hashes.add(h);
		}
		double total = hashes.size();
		double cnt = 0;
		for (int h: hashes) {
			if (mhes.contains(h)) cnt += 1;
		}
		return cnt / total;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		String text = "[给力]【UGG澳洲代购雪地靴限时限量限价秒杀中】限时3小时，限量200个，限价1折[花心]100%正品保障，90>天无理由退换[礼物]错过机会，不会再有[来]这个冬天！为你的家人和爱人准备一双温暖的UGG雪地靴吧！[心]分享秒杀地址>>>http://t.cn/zl8skhQ";
		MinhashAntiSpamLink as = new MinhashAntiSpamLink(100);
		as.add(text);
		double sim = as.checkSim(text);
		System.out.println(sim);
	}
	
	public static int hash(String text) throws UnsupportedEncodingException {
		byte[] bs = text.getBytes("UTF-8");
		return MurmurHash3.murmurhash3_x86_32(bs, 0, bs.length, 0x3c074a61) & 0x7FFFFFFF;
	}
	
	public static HashSet<String> createNgram(String text, int n) {
		HashSet<String> tokens = new HashSet<String>();
		List<List<String>> sentences = TextUtility.splitSentenceSet(text);
		for (List<String> sentence: sentences) {
			for (int i = 0; i < sentence.size(); i++) {
				if (isDigit(sentence.get(i))) sentence.set(i, "#");
			}
			for (int i = 0; i + n < sentence.size(); i++) {
				String token = StringUtils.join(sentence.subList(i, i+n), "");
				tokens.add(token);
			}
		}
		return tokens;
	}
	
	private static boolean isDigit(String word) {
		return Character.isDigit(word.charAt(0));
	}
}