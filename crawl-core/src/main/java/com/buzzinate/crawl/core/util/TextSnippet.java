package com.buzzinate.crawl.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class TextSnippet {
	private static final HashSet<Character> keepPuncs = new HashSet<Character>(Arrays.asList('[', ']', '【', '】', '(', ')', '\'', '"', '“', '”', '（', '）', '，', '.'));
	
	private List<String> words;
    private final Phrase[] suffixes;
    private final int[] lcs;
    private int maxLen;

    public TextSnippet(String pattern) {
    	this.words = split(pattern);
        this.maxLen = words.size();
        this.suffixes = new Phrase[words.size()];
        this.lcs = new int[words.size()];
        for (int i = 0; i < suffixes.length; i++) suffixes[i] = new Phrase(words, i);
        Arrays.sort(suffixes, new Phrase.Comp(maxLen));
        if (lcs.length > 0) lcs[0] = 0;
        for (int i = 1; i < lcs.length; i++) {
        	lcs[i] = suffixes[i].maxLen(words, suffixes[i-1].offset);
        }
    }
    
    public String get(int id) {
    	return suffixes[id].toString();
    }
    
    public int getMaxLen() {
    	return maxLen;
    }
    
    public String searchBest(String content) {
    	List<String> text = split(content);
    	List<Point> ps = new ArrayList<Point>();
    	int[] sum = new int[words.size() + 1];
//    	HashSet<Integer> matchSet = new HashSet<Integer>();
    	for (int i = 0; i < text.size(); ) {
    		int next = i + 1;
    		char ch = text.get(i).charAt(0);
    		if (Character.isLetterOrDigit(ch)) {
	        	List<Match> ms = prefix(text, i);
	        	
	        	for (Match m: ms) {
	        		next = Math.max(next, i + m.end - m.start);
//	        		int mv = m.start * words.size() + m.end;
//	        		if (matchSet.contains(mv)) continue;
//	        		matchSet.add(mv);
	        		sum[m.end]++;
//	        		System.out.println(i + "/" + m + " ==> " + words.subList(m.start, m.end));
	        		ps.add(new Point(m.start, Point.START));
	        		ps.add(new Point(m.end - 1, Point.END));
	        	}
    		}
    		i = next;
        }
    	Collections.sort(ps);
    	for (int i = 1; i < sum.length; i++) sum[i] = sum[i-1] + sum[i];
    	int pos = -1;
    	int cnt = 0;
    	int start = 0;
    	int maxCnt = 0;
    	Match best = null;
       	int maxLargeCnt = 0;
    	Match largeBest = null;
    	for (Point p: ps) {
    		pos = p.pos;
    		if (p.type == Point.START) {
    			if (cnt == 0) start = pos;
    			cnt++;
    		}
    		else {
    			cnt--;
    			if (cnt == 0) {
    				int s = sum[pos + 1] - sum[start];
//    				System.out.println("found: " + words.subList(start, pos+1) + " ==> " + s);
    				if (maxCnt < s) { // TODO: tricky
    					best = new Match(start, pos + 1);
    					maxCnt = s;
    				}
    				if (pos + 1 - start > 4) {
    					if (maxLargeCnt < s) {
    						largeBest = new Match(start, pos+1);
    						maxLargeCnt = s;
    					}
    				}
    			}
    		}
    	}
    	
    	if (largeBest != null) best = largeBest;
    	if (best == null) return null;
    	
    	start = best.start;
    	int end = best.end;
    	while (start >= 0 && !isSplit(words.get(start).charAt(0))) start --;
    	while (end < words.size() && !isSplit(words.get(end).charAt(0))) end++;
    	
    	return StringUtils.join(words.subList(start+1, end), "").trim();
    }
    
    private static class Point implements Comparable<Point> {
    	public static final int START = 0;
    	public static final int END = 1;
    	int pos;
    	int type;
    	
    	public Point(int pos, int type) {
    		this.pos = pos;
    		this.type = type;
    	}

		@Override
		public int compareTo(Point p) {
			return pos * 2 + type - p.pos * 2 - p.type;
		}
    }

    public List<Match> prefix(List<String> text, int offset) {
    	List<Match> matches = new ArrayList<Match>();
        int lo = 0, hi = suffixes.length - 1;
        Phrase p = new Phrase(text, offset);
        int len = 1;
        int lastMid = -1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int cmp = p.compareTo(suffixes[mid], len);
            if (cmp < 0) hi = mid - 1;
	        else if (cmp > 0) lo = mid + 1;
	        else {
	        	lastMid = mid;
	        	len++;
	        	if (len > maxLen) break;
	        }
        }
        if (lastMid != -1) {
        	int l = lcp(suffixes[lastMid], text, offset);
        	if (l > 1 || l == 1 && suffixes[lastMid].getWord().length() >= 3) matches.add(new Match(suffixes[lastMid].offset, suffixes[lastMid].offset + l));
        	int i = lastMid - 1;
        	int sl = l;
        	while (i >= 0) {
        		sl = Math.min(lcs[i], sl);
        		if (sl > 1 || sl == 1 && suffixes[i].getWord().length() >= 3) matches.add(new Match(suffixes[i].offset, suffixes[i].offset + sl));
        		else break;
        		i--;
        	}
        	i = lastMid + 1;
        	sl = l;
        	while (i < suffixes.length) {
        		sl = Math.min(lcs[i], sl);
        		if (sl > 1 || sl == 1 && suffixes[i].getWord().length() >= 3) matches.add(new Match(suffixes[i].offset, suffixes[i].offset + sl));
        		else break;
        		i++;
        	}
        }
        return matches;
    }
	
	private int lcp(Phrase phrase, List<String> text, int offset) {
		return phrase.maxLen(text, offset);
	}
	
	public static class Match {
		private int start;
		private int end;
		
		public Match(int start, int end) {
			this.start = start;
			this.end = end;
		}
		
		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		@Override
		public String toString() {
			return "[" + start + ", " + end + ")";
		}
	}
	
	public static void main(String[] args) {		
		TextSnippet suffix = new TextSnippet("PHP的优势 <-- PHP <-- IT技术博客大学习 -- 共学习 共进步！");
        String text = "PHP的优势  以前偶尔被人问到, 为什么你(和大部分互联网公司)做Web开发要选择PHP, PHP有什么好处. 简单的回答便是”PHP简单,开发快速”.";
        String best = suffix.searchBest(text);
        System.out.println(best);
	}
	
	public static List<String> split(String text) {
		List<String> words = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (isAlphaOrDigit(ch)) {
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
				String punc = String.valueOf(ch);
				words.add(punc);
			}
		}
		
		if (sb.length() > 0) words.add(sb.toString());
		return words;
	}
	
	public static boolean isAlphaOrDigit(char ch) {
		if (ch >= 'a' && ch <= 'z') return true;
		if (ch >= 'A' && ch <= 'Z') return true;
		if (ch >= '0' && ch <= '9') return true;
		return false;
	}
	
	private static boolean isSplit(char ch) {
		int type = Character.getType(ch);
		if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch)) return false;
		if (type == Character.START_PUNCTUATION || type == Character.END_PUNCTUATION || keepPuncs.contains(ch)) return false;
		return true;
	}
    
    private static class Phrase {
    	private List<String> words;
    	private int offset;
    	
    	public Phrase(List<String> words, int offset) {
    		this.words = words;
    		this.offset = offset;
    	}
    	
    	public String getWord() {
    		return words.get(offset);
    	}
    	
    	public int maxLen(List<String> text, int off) {
    		int i = 0;
    		for (; offset + i < words.size() && off + i < text.size(); i++) {
    			if (!words.get(offset + i).equals(text.get(off + i))) break;
    		}
    		return i;
    	}

    	@Override
		public String toString() {
			return StringUtils.join(words.subList(offset, words.size()), "");
		}
		
		public int compareTo(Phrase other, int maxLen) {
			int len = Math.min(words.size() - offset, other.words.size() - other.offset);
			int r = 0;
			for (int i = 0; i < len && i < maxLen; i++) {
				r = words.get(offset + i).compareTo(other.words.get(other.offset + i));
				if (r != 0) return r;
			}
			if (r == 0 && len < maxLen) r = new Integer(words.size() - offset).compareTo(other.words.size() - other.offset);
			return r;
		}
		
		public static class Comp implements Comparator<Phrase> {
			private int maxLen;
			
			public Comp(int maxLen) {
				this.maxLen = maxLen;
			}

			@Override
			public int compare(Phrase p1, Phrase p2) {
				return p1.compareTo(p2, maxLen);
			}
		}
    }
}