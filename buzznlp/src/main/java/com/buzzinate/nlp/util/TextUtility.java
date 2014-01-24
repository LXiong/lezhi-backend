package com.buzzinate.nlp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TextUtility {
	public enum CharType { Alpha, Digit, Letter, Other}
		
	private static final HashSet<String> noizeTags = new HashSet<String>(Arrays.asList(
		"html", "head", "body", "frameset", "script", "noscript", "style", "meta", "link", "title", "frame", "ul", "tr", "td", "dl",
		"noframes", "section", "nav", "aside", "hgroup", "header", "footer", "pre", "div", "address", "figure", "figcaption",
		"form", "fieldset", "ins", "del", "table", "caption", "thead", "tfoot", "tbody", "colgroup", "col", "video",
		"audio", "canvas", "details", "menu", "plaintext"
	));
	
	public static final HashSet<Character> prePosChars = new HashSet<Character>(Arrays.asList(
		'一','不','与','也','了','于','仍','从','以','但','使','则','却','又','及','在','地','对','就','并','当','很','得','成','或','把','时',
		'是','比','的','着','等','给','而','虽','被','让','还', '和', '这', '那', '万', '亿', '十', '更', '为', '来', '都','将','你','我','他',
		'她','它','个', '太', '最', '之'
	));
	
	public static HashSet<String> keepPuncs = new HashSet<String>(Arrays.asList(",", ".", "，", "。"));	

	private TextUtility() {
	}

	public static boolean isNoizeTags(String tag) {
		return noizeTags.contains(tag);
	}
	
	public static String formatNum(String txt) {
		StringBuffer sb = new StringBuffer();
		boolean prevDigit = false;
		for (int i = 0; i < txt.length(); i++) {
			char ch = txt.charAt(i);
			if (Character.isDigit(ch)) {
				if (!prevDigit) sb.append("#");
				prevDigit = true;
			} else {
				sb.append(ch);
				prevDigit = false;
			}
		}
		return sb.toString();
	}
	
	public static int countLetters(String text) {
		int total = 0;
		boolean isLastAlpha = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (Character.isLetter(ch)) {
				if (!isLastAlpha || !isAlphaOrDigit(ch)) total++;
			}
			if (Character.isLetter(ch) && isAlphaOrDigit(ch)) isLastAlpha = true;
			else isLastAlpha = false;
		}
		return total;
	}
	
	public static List<String> splitWords(String text) {
		return splitWords(text, new HashSet<String>());
	}
	
	public static List<String> splitWords(String text, HashSet<String> keepPuncs) {
		List<String> words = new ArrayList<String>();
		CharType lastType = CharType.Other;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (isAlphaOrDigit(ch)) {
				if (ch >= '0' && ch <= '9') {
					if (lastType != CharType.Digit) sb.append("#");
					lastType = CharType.Digit;
				} else {
					sb.append(ch);
					lastType = CharType.Alpha;
				}
			} else if (Character.isLetter(ch)) {
				if (sb.length() > 0) {
					words.add(sb.toString());
					sb.setLength(0);
				}
				words.add(String.valueOf(ch));
				lastType = CharType.Letter;
			} else {
				if (sb.length() > 0) {
					words.add(sb.toString());
					sb.setLength(0);
				}
				String punc = String.valueOf(ch);
				if (keepPuncs.contains(punc)) words.add(punc);
				lastType = CharType.Other;
			}
		}
		
		if (sb.length() > 0) words.add(sb.toString());
		return words;
	}

	public static int countNumWords(String text) {
		int total = text.length();
		boolean isLastLetter = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (isAlphaOrDigit(ch)) {
				if (isLastLetter) total -= 1;
				isLastLetter = true;
			} else {
				if (!Character.isLetter(ch)) total -= 1;
				isLastLetter = false;
			}
		}
		return total;
	}
	
	public static int countPuncs(String text) {
		int nPuncs = 0;
		
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == ',' || ch == '.') {
				if (i + 1 == text.length()) nPuncs++;
				else if (Character.isWhitespace(text.charAt(i+1))) nPuncs++;
			}
			if (ch == '，' || ch == '。') nPuncs++;
		}
		return nPuncs;
	}
	
	public static int parseInt(String text, int d) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') sb.append(ch);
			else if (sb.length() > 0) return Integer.parseInt(sb.toString());
		}
		if (sb.length() > 0) return Integer.parseInt(sb.toString());
		return d;
	}

	public static boolean isAlphaOrDigit(char ch) {
		if (ch >= 'a' && ch <= 'z') return true;
		if (ch >= 'A' && ch <= 'Z') return true;
		if (ch >= '0' && ch <= '9') return true;
		return false;
	}
	
	public static boolean isFirstTwoUpperCase(String text) {
		boolean hasUpperCase = false;
		for (int i = 0; i < 2 && i < text.length(); i++) {
			char ch = text.charAt(i);
			int type = Character.getType(ch);
			if (type == Character.UPPERCASE_LETTER) {
				hasUpperCase = true;
				break;
			}
		}
		return hasUpperCase;
	}
	
	public static boolean isFirstAscii(String text) {
		if (text.length() == 0) return false;
		char ch = text.charAt(0);
		int type = Character.getType(ch);
		return type == Character.UPPERCASE_LETTER || type == Character.LOWERCASE_LETTER;
	}
	
	public static boolean isUsefulKeyword(String word) {
		if (TextUtility.isFirstAscii(word)) {
			if (word.contains(" ") || word.length() >= 3) return true;
		}
		else if (word.length() >= 3) return true;
		return false;
	}
	
	public static boolean isAllUpperCase(String text) {
		boolean allUpperCase = true;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			int type = Character.getType(ch);
			if (type != Character.UPPERCASE_LETTER && type != Character.DECIMAL_DIGIT_NUMBER && ch != '-') allUpperCase = false;
		}
		return allUpperCase;
	}
	
	public static boolean isNormalWord(String text) {
		boolean normal = true;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			int type = Character.getType(ch);
			if (i == 0 && type != Character.UPPERCASE_LETTER) normal = false;
			if (i > 0 && type != Character.LOWERCASE_LETTER) normal = false;
		}
		return normal;
	}
	
	public static void main(String[] args) {
		String text = "发表于 6 天, 前";
		System.out.println(splitWords(text, new HashSet<String>(Arrays.asList("，", "。"))));
		System.out.println(countNumWords(text));
		System.out.println(text.length());
		System.out.println(TextUtility.isUsefulKeyword("iPad"));
		System.out.println(TextUtility.isUsefulKeyword("Google"));
		System.out.println(TextUtility.isUsefulKeyword("手机"));
	}
	
	public static String deduplicateSentence(String text) {
		StringBuffer sb = new StringBuffer();
		List<String> sentences = splitSentences(text);
		HashSet<String> set = new HashSet<String>();
		for (String sentence: sentences) {
			if (set.contains(sentence)) {
				//System.out.println(sentence);
				continue;
			}
			sb.append(sentence);
			set.add(sentence);
		}
		return sb.toString();
	}
	
	public static List<String> splitSentences(String text) {
		List<String> sentences = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		boolean prevEnd = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			sb.append(ch);
			if (ch == '。' || (prevEnd && Character.isWhitespace(ch))) {
				String sentence = sb.toString().trim();
				if (sentence.length() > 1) {
					sentences.add(sentence);
					sb.setLength(0);
				}
			}
			if (ch == '.' || Character.isWhitespace(ch)) prevEnd = true;
			else prevEnd = false;
		}
		if (sb.length() > 0) {
			String sentence = sb.toString().trim();
			if (sentence.length() > 1) {
				sentences.add(sentence);
			}
		}
		
		return sentences;
	}

	public static List<List<String>> splitSentenceSet(String text) {
		StringBuffer sb = new StringBuffer();
		List<List<String>> sentences = new ArrayList<List<String>>();
		List<String> current = new ArrayList<String>();
		
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			int type = Character.getType(ch);
			
			if (type != Character.LOWERCASE_LETTER && type != Character.UPPERCASE_LETTER && ch != '’' && ch != '-' && type != Character.DECIMAL_DIGIT_NUMBER) {
				if (sb.length() > 1 && sb.charAt(0) != '-') current.add(sb.toString());
				sb.setLength(0);
			}
			if (type == Character.LOWERCASE_LETTER || type == Character.UPPERCASE_LETTER || ch == '’' || ch == '-' || type == Character.DECIMAL_DIGIT_NUMBER) {
				sb.append(ch);
			}
			if (type == Character.OTHER_LETTER && !prePosChars.contains(ch)) {
				current.add(String.valueOf(ch));
			}
			if (type >= Character.DASH_PUNCTUATION && type <= Character.OTHER_PUNCTUATION || prePosChars.contains(ch)) {
				if (current.size() > 1) sentences.add(current);
				if (current.size() == 1 && current.get(0).length() > 1) sentences.add(current);
				current = new ArrayList<String>();
			}
		}
		if (current.size() > 1) sentences.add(current);
		if (current.size() == 1 && current.get(0).length() > 1) sentences.add(current);
		return sentences;
	}
}
