package com.buzzinate.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.arabidopsis.ahocorasick.WordFreqTree;

import weibo4j2.model.Paging;
import weibo4j2.model.Status;
import weibo4j2.model.WeiboException;

import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dao.UserWeiboDao;
import com.buzzinate.common.model.UserWeibo;
import com.buzzinate.common.util.MentionUtil;
import com.buzzinate.crawl.core.util.Counter;
import com.buzzinate.keywords.Keyword;
import com.buzzinate.keywords.MobileKeywordsExtractor;
import com.buzzinate.main.MyModule;
import com.buzzinate.weibo.SinaWeiboClient;
import com.buzzinate.weibo.WeiboExt;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ExtractCookieKeywords {

	public static void main(String[] args) throws WeiboException, IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		UserDao userDao = injector.getInstance(UserDao.class);
		UserWeiboDao uwDao = new UserWeiboDao(ds);
		
//		String accessToken = userDao.findByUid(1720400850L).getAccessToken();
//		WeiboExt weibo = new SinaWeiboClient().getWeibo(accessToken);
//		
//		fetchWeibo(uwDao, weibo);
	
		WordFreqTree wft = build();
//		PrintWriter file = new PrintWriter(new FileWriter(new File("E:/data/car.ids.keywords")));
//		Map<String, String> uid2info = parseUid2info(IOUtils.toString(new FileReader("E:/data/car.ids")));
////		
		PrintWriter file = new PrintWriter(new FileWriter(new File("E:/data/fashion.ids.keywords")));
		Map<String, String> uid2info = parseUid2info(IOUtils.toString(new FileReader("E:/data/fashion.ids")));
		
		int total = uid2info.size();
		int cnt = 0;
		for (Map.Entry<String, String> e: uid2info.entrySet()) {
			long uid = Long.parseLong(e.getKey());
			List<UserWeibo> weibos = uwDao.findLatest(uid, 200);
			List<String> texts =  new ArrayList<String>();
			for (UserWeibo uw: weibos) {
				String text = MentionUtil.cleanStatusText(uw.getText());
				texts.add(text);
			}
			
			Counter<String> wordcnt = new Counter<String>();
			for (String text: texts) {
				for (String word: wft.search(text)) {
					wordcnt.add(word);
				}
			}
			Map<String, Integer> wordfreq = wordcnt.toMap();
			for (Keyword keyword: MobileKeywordsExtractor.extract(texts, 30)) {
				if (!wordfreq.containsKey(keyword.word())) wordfreq.put(keyword.word(), keyword.freq());
			}
			
			List<String> keywords = new ArrayList<String>();
			for (Map.Entry<String, Integer> wf: wordfreq.entrySet()) {
				keywords.add(wf.getKey() + "(" + wf.getValue() + ")");
			}
			file.println(e.getKey() + "," + e.getValue() + "," + StringUtils.join(keywords, " "));
			
			cnt ++;
			if (cnt % 100 == 0) {
				file.flush();
				System.out.println("now " + cnt + "/" + total);
			}
		}
		file.flush();
		file.close();
	}
	
	private static WordFreqTree build() throws IOException {
		WordFreqTree wft = new WordFreqTree();
		LineIterator lines = IOUtils.lineIterator(Thread.currentThread().getContextClassLoader().getResourceAsStream("carfashion.dict"), "UTF-8");
		while (lines.hasNext()) {
			String line = lines.next();
			String word = StringUtils.substringBefore(line, "\t");
			wft.add(word);
		}
		wft.build();
		return wft;
	}

	private static void fetchWeibo(UserWeiboDao uwDao, WeiboExt weibo) throws FileNotFoundException, WeiboException {
		LineIterator lines = IOUtils.lineIterator(new FileReader("E:/data/car.ids"));
		while (lines.hasNext()) {
			String line = lines.next();
			try {
				long uid = Long.parseLong(StringUtils.substringBefore(line, ",["));
				fetchUserWeibo(weibo, uwDao, uid);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		lines.close();
		
		lines = IOUtils.lineIterator(new FileReader("E:/data/fashion.ids"));
		while (lines.hasNext()) {
			String line = lines.next();
			try {
				long uid = Long.parseLong(StringUtils.substringBefore(line, ",["));
				fetchUserWeibo(weibo, uwDao, uid);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		lines.close();
		System.out.println("##############Done#################");
	}
	
	public static void fetchUserWeibo(WeiboExt weibo, UserWeiboDao uwDao, long uid) throws WeiboException {
		List<UserWeibo> weibos = uwDao.findLatest(uid, 200);
		if (weibos.isEmpty()) {
			Paging p = new Paging();
			p.setCount(200);
			List<Status> statuses = weibo.getUserTimeline(String.valueOf(uid), p);
			for (Status status: statuses) {
				System.out.println(status.getText());
				UserWeibo uw = new UserWeibo(uid, status.getIdstr(), status.getText(), status.getCreatedAt().getTime());
				uwDao.save(uw);
				weibos.add(uw);
				Status rt = status.getRetweetedStatus();
				if (rt != null && rt.getCreatedAt() != null) {
					System.out.println(rt.getText());
					UserWeibo ruw = new UserWeibo(uid, rt.getIdstr(), rt.getText(), rt.getCreatedAt().getTime());
					uwDao.save(ruw);
					weibos.add(ruw);
				}
			}
		}
	}
	
	public static Map<String, String> parseUid2info(String content) {
		Map<String, String> uid2info = new HashMap<String, String>();
		
		Pattern p = Pattern.compile("([0-9]+),\\[([^\\]]*)]");
		Matcher m =  p.matcher(content);
		int start = 0;
		while (m.find(start)) {
			String uid = m.group(1);
			String info = m.group(2);
			uid2info.put(uid, StringUtils.replace(info, "\r\n", " "));
			start = m.end();
		}
		
		return uid2info;
	}
}