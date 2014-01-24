package org.knowceans.lda.corpus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Vocabulary {
	private HashMap<String, Integer> terms = new HashMap<String, Integer>();
	private List<String> words = new ArrayList<String>();
	
	public List<Integer> encode(List<String> words) {
		List<Integer> ts = new ArrayList<Integer>();
		for (String word: words) ts.add(encode(word));
		return ts;
	}
	
	public int getSize() {
		return words.size();
	}
	
	public List<String> getWords() {
		return words;
	}
	
	private int encode(String word) {
		Integer ti = terms.get(word);
		if (ti == null) {
			ti = words.size();
			terms.put(word, ti);
			words.add(word);
		}
		return ti;
	}
	
	public List<Integer> encodeOnly(List<String> words) {
		List<Integer> ts = new ArrayList<Integer>();
		for (String word: words) {
			Integer t = terms.get(word);
			if (t != null) ts.add(t);
		}
		return ts;
	}
	
	public Vocabulary() {
		
	}
	
	public Vocabulary(List<String> words) {
		this.words = words;
	}

    public Vocabulary(String modelRoot) {
        String filename = modelRoot + ".voc";
        System.out.println("loading vocabulary " + filename + "\n");

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            String[] ws = line.split(" ");
            for (int i = 0; i < ws.length; i++) {
            	words.add(ws[i]);
            	terms.put(ws[i], i);
            }            
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void save(String modelRoot) {
        String filename = modelRoot + ".voc";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            bw.write(StringUtils.join(words, " "));
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}