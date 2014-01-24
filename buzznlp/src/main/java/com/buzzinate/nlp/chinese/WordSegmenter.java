package com.buzzinate.nlp.chinese;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import org.ictclas4j.bean.DictLib;
import org.ictclas4j.bean.Pos;
import org.ictclas4j.bean.SegAtom;
import org.ictclas4j.bean.SegResult;
import org.ictclas4j.segment.Segment;

import com.buzzinate.nlp.util.TextUtility;

public class WordSegmenter {
	public static HashSet<String> usefulTags = new HashSet<String>(Arrays.asList("Ag", "a", "an", "i", "j", "l", "Ng", "n", "nz", "nr", "ns", "nt", "nx", "nz,Tg", "t", "Vg", "v", "vd", "vn", "un"));
	public static final HashSet<String> stopwords = loadStopwords(Thread.currentThread().getContextClassLoader().getResourceAsStream("stopwords.txt"));
	
	public static Segment seg;
	private static POSTaggerME tagger;
	
	private WordSegmenter() { }
	
	static {
		try {
			seg = new Segment(new DictLib(false));
			
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			POSModel model = new POSModel(cl.getResourceAsStream("en-pos-maxent.bin"));
			tagger = new POSTaggerME(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		System.out.println(WordSegmenter.segmentSentence("It starts with a fairly straightforward usage of MapReduce as a general purpose parallel execution framework, which can be applicable to many implementations requiring leveraging of large clusters for compute and data intensive calculations, including physical and engineering simulations, numerical analysis, performance testing, etc. The next group of algorithms, commonly used in Log Analysis, ETL and Data Querying, includes counting and summing, data collating (based on specific functions), filtering, parsing, validation and sorting."));
		System.out.println(WordSegmenter.segmentSentence("张新波在杭州, 馆内陈列周恩来与邓颖超生前使用过的物品"));
		System.out.println(WordSegmenter.segmentSentence("联邦调查局早在1991年就已经进行了这项调查，原因是时任美国总统的乔治·布什(George H.W. Bush)考虑提名乔布斯进入总统直属的出口委员会，这一职位无需经过参议院的确认，其具体职责是担任有关国际贸易的国家顾问。这项背景调查显示，乔布斯本来将可为出口委员会带来一种良好的、与众不同的远景观点。在联邦调查局公布的这份文件中，有关乔布斯在苹果供职时是否仍酗酒或服用毒品的问题曾多次出现。乔布斯曾承认他在年轻时吸过毒。来自于IBM的一位受访对象对此表示，他从来都没有“看到过被提名人有任何非法使用毒品或酗酒的行为”，并表示乔布斯“看起来生活在自己的财务模式中，从来都没看到过他有任何奢侈生活方式的例子”。一位匿名的女性消息人士也表示，乔布斯“饮酒很少，而且并未使用任何类型的非法药物”。"));
	}
	
	@SuppressWarnings("deprecation")
	public static List<Token> segmentSentence(String text) {
		List<Token> result = new ArrayList<Token>();
		boolean isEn = isEnglish(text);
		List<String> sentences = TextUtility.splitSentences(text);
		if (isEn) {
			int idx = 0;
			for (String sentence: sentences) {
				List<String> words = splitWords(sentence);
				List<String> tags = tagger.tag(words);
				for (int i = 0; i < tags.size(); i++) {
					String tag = null;
					if (tags.get(i).startsWith("NN")) tag = "N";
					if (tags.get(i).equals("NNP")) tag = "NR";
					if (tags.get(i).startsWith("VB")  || tags.get(i).startsWith("JJ")) tag = "O";
					if (tag != null && !stopwords.contains(words.get(i).toLowerCase()) && isAllLetter(words.get(i))) result.add(new Token(words.get(i), tag, idx));
					idx++;
				}
			}
		} else {
			int idx = 0;
			for (String sentence: sentences) {
				SegResult sr = seg.split(sentence);
				List<SegAtom> atoms = sr.getAtoms();
				for (int i = 0; i < atoms.size(); i++) {
					SegAtom atom = atoms.get(i);
					ArrayList<Pos> posList = atom.getPosList();
					if (posList != null) {
						for (Pos pos: posList) {
							String p = pos.toString();
							if (usefulTags.contains(p)) {
								String tag = "O";
								if (p.equals("nr")) tag = "NR";
								if (p.startsWith("n")) tag = "N";
								if (!stopwords.contains(atom.getWord().toLowerCase()) && isAllLetter(atom.getWord())) {
									result.add(new Token(atom.getWord(), tag, idx));
									break;
								}
							}
						}
					}
					idx++;
				}
			}
		}
		return result;
	}
	
	private static boolean isAllLetter(String word) {
		int nLetter = 0;
		for (int i = 0; i < word.length(); i++) {
			if (Character.isLetterOrDigit(word.charAt(i))) nLetter++;
		}
		return nLetter == word.length();
	}

	private static List<String> splitWords(String text) {
		List<String> words = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (TextUtility.isAlphaOrDigit(ch)) {
				sb.append(ch);
			} else if (Character.isLetter(ch)) {
				if (sb.length() > 0) {
					words.add(sb.toString());
					sb.setLength(0);
				}
				words.add(String.valueOf(ch));
			} else {
				if (sb.length() > 0) {
					words.add(sb.toString());
					sb.setLength(0);
				}
				
				if (!Character.isWhitespace(ch)) {
					String punc = String.valueOf(ch);
					words.add(punc);
				}
			}
		}
		
		if (sb.length() > 0) words.add(sb.toString());
		return words;
	}
	
	private static boolean isEnglish(String text) {
		int nLetter = 0;
		int nAscii = 0;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			int type = Character.getType(ch);
			if (type == Character.UPPERCASE_LETTER || type == Character.LOWERCASE_LETTER) nAscii++;
			if (Character.isLetter(ch)) nLetter++;
		}
		
		return nAscii >= nLetter * 0.8;
	}
	
	private static HashSet<String> loadStopwords(InputStream input) {
		HashSet<String> words = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() != 0) words.add(line.toLowerCase());
			}
			br.close();
		} catch (IOException e) {
			System.err.println("WARNING: cannot open stop words list!");
		}
		return words;
	}
}