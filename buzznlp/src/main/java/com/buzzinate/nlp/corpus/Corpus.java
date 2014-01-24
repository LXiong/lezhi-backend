package com.buzzinate.nlp.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.buzzinate.nlp.util.TextUtility;

public class Corpus {
	private List<String> texts;
	private HashMap<String, Object> datas = new HashMap<String, Object>();
	
	public Corpus(List<String> texts) {
		this.texts = lowerFirstCase(texts);
	}

	public List<String> getTexts() {
		return texts;
	}
	
	public <T> void setData(String key, T data) {
		datas.put(key, data);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getData(String key) {
		return (T) datas.get(key);
	}
	
	private static List<String> lowerFirstCase(List<String> texts) {
		List<String> firstWords = new ArrayList<String>();
		for (String text: texts) {
			int pos = 0;
			while (pos != -1) {
				int end = text.indexOf(' ', pos);
				if (end == -1) end = text.length();
				String fw = text.substring(pos, end);
				if (TextUtility.isFirstAscii(fw)) firstWords.add(fw);
				
				pos = text.indexOf(". ", end);
				if (pos != -1) pos = pos + 2;
			}
		}
//		System.out.println(firstWords);
		List<String> needLowerFirstWords = new ArrayList<String>();
		for (String fw: firstWords) {
			String lfw = fw.substring(0, 1).toLowerCase() + fw.substring(1);
			for (String text: texts) {
				if (text.indexOf(lfw) != -1) {
					needLowerFirstWords.add(fw);
					break;
				}
			}
		}
//		System.out.println(needLowerFirstWords);
		
		List<String> result = new ArrayList<String>();
		for (String text: texts) {
			for (String fw: needLowerFirstWords) {
				String lfw = fw.substring(0, 1).toLowerCase() + fw.substring(1);
				text = StringUtils.replace(text, fw, lfw);
			}
			result.add(text);
		}
		return result;
	}
}