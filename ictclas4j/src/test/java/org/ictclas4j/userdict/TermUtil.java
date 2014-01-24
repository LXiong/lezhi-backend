package org.ictclas4j.userdict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

public class TermUtil {
	public static final HashSet<String> stopwords = loadStopwords(Thread.currentThread().getContextClassLoader().getResourceAsStream("stopword.txt"));
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
