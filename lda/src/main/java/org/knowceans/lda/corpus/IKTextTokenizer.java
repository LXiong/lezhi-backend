package org.knowceans.lda.corpus;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class IKTextTokenizer implements TextTokenizer {
	private static IKAnalyzer analyzer = new IKAnalyzer();

	@Override
	public List<String> tokenize(String text) {
		List<String> result = new ArrayList<String>();
		try {
			TokenStream ts = analyzer.tokenStream("body", new StringReader(text));
			CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
			while (ts.incrementToken()) {
			    char[] termBuffer = termAtt.buffer();
			    int termLen = termAtt.length();
			    String w = new String(termBuffer, 0, termLen);
			    if (w.length() > 1) result.add(w);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}