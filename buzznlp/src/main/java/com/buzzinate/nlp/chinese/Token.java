package com.buzzinate.nlp.chinese;


public class Token {
	public static final int UNKNOWN_POS = -100;
	
	private String word;
	private String tag;
	private int pos;
	
	public Token(String word) {
		this(word, null, UNKNOWN_POS);
	}
	
	public Token(String word, String tag, int pos) {
		this.word = word;
		this.tag = tag;
		this.pos = pos;
	}

	public String getWord() {
		return word;
	}

	public String getTag() {
		return tag;
	}

	public int getPos() {
		return pos;
	}
	
	@Override
	public String toString() {
		return word + "(tag=" + tag + ",pos=" + pos + ")";
	}
}