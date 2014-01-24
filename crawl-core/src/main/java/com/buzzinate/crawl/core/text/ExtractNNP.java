package com.buzzinate.crawl.core.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Response;
import com.buzzinate.crawl.core.util.Counter;
import com.buzzinate.crawl.core.util.ExtractUtils;
import com.buzzinate.crawl.core.util.PorterStemmer;
import com.buzzinate.crawl.core.util.TextSnippet;
import com.buzzinate.crawl.core.util.TextUtility;

public class ExtractNNP {
	private static Pattern bp = Pattern.compile("《(.+?)》");
	private static PorterStemmer stemmer = new PorterStemmer();
	private static HashSet<String> enwords = loadEnWords();

	public static void main(String[] args) throws IOException {
//		String title = "MapReduce Patterns, Algorithms, and Use Cases, MapReduce";
//		String content = "The Horoscope, The Road, and Its Travelers, (Bantam Books). 美航天局拟把好莱坞大片《复仇者联盟》送入空间站, 《复仇者3》. MapReduce Patterns, Algorithms, and Use Cases, MapReduce";
//		List<String> texts = TextUtility.splitSentences(content);
		Response resp = PageFetcher.fetch("http://www.infoq.com/articles/graph-nosql-neo4j");
		String title = resp.getDoc().title();
		List<String> texts = ExtractUtils.splitSentences(resp.getDoc().body());
		List<String> words = extractNewWords(title, texts);
		System.out.println(words);
	}
	
	public static HashSet<String> loadEnWords() {
		HashSet<String> words = new HashSet<String>();
		BufferedReader br = null;
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("en-words.txt");
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				words.add(stemAll(line));
			}
			return words;
		} catch (IOException e) {
			e.printStackTrace();
			return words;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public static List<String> extractNewWords(String title, List<String> texts) {
		List<String> words = new ArrayList<String>();
		
		List<List<String>> sentences = new ArrayList<List<String>>();
		sentences.add(TextSnippet.split(title));
		for (String text: texts) {
			Matcher m = bp.matcher(text);
			while (m.find()) {
				String word = m.group(1).trim();
				if (isAllLetterOrSpace(word) && TextUtility.countNumWords(word) <= 6) words.add(stemAll(word.toLowerCase()));
			}
			sentences.add(TextSnippet.split(text));
		}
		
		Counter<String> wordCnt = new Counter<String>();
		for (List<String> sentence: sentences) {
			if (sentence.size() <= 5) continue;
			String prevWord = "";
			int nnpCnt = 0;
			int enCnt = 0;
			for (int i = 0; i < sentence.size(); i++) {
				String word = sentence.get(i); 
				if (isNNP(word)) nnpCnt++;
				if (TextUtility.isFirstAscii(word)) enCnt++;
			}
			if (nnpCnt >= enCnt * 7 / 10) continue;
			
			for (int i = 1; i < sentence.size(); i++) {
				String word = sentence.get(i).trim();
				if (word.length() == 0 || TextUtility.isWhitespace(word.charAt(0))) continue;
				if (isNNP(word)) {
					wordCnt.add(stemAll(word.toLowerCase()));
					if (isNNP(prevWord)) wordCnt.add(stemAll((prevWord + " " + word).toLowerCase()));
				}
				prevWord = word;
			}
		}
		
		for (String word: wordCnt.freqItems(2)) {
			String lastWord = StringUtils.substringAfter(word, " ");
			if (lastWord.isEmpty()) lastWord = word;
			if (!enwords.contains(lastWord)) words.add(word);
		}
		
		return words;
	}
	
	public static boolean isKnownWord(String word) {
		String w = stemAll(word.toLowerCase());
		return enwords.contains(w);
	}
	
	public static boolean isAllLetterOrSpace(String word) {
		for (char ch: word.toCharArray()) {
			if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch)) return false;
		}
		return true;
	}

	private static boolean isNNP(String word) {
		return word.length() >= 3 && TextUtility.isFirstAscii(word) && TextUtility.isFirstTwoUpperCase(word);
	}
	
	public static String stemAll(String text) {
		StringBuffer sb = new StringBuffer();
		int last = 0;
		boolean prevAscii = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			boolean ascii = isAscii(ch);
			if (prevAscii && !ascii) {
				sb.append(stemmer.stem(text.substring(last, i)));
				last = i;
			}
			prevAscii = ascii;
		}
		if (last < text.length()) sb.append(stemmer.stem(text.substring(last, text.length())));
		return sb.toString();
	}
	
	public static boolean isAscii(char ch) {
		int type = Character.getType(ch);
		return type == Character.LOWERCASE_LETTER || type == Character.UPPERCASE_LETTER || ch == '-';
	}
}