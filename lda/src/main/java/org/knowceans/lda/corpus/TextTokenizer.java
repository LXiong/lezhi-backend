package org.knowceans.lda.corpus;

import java.util.List;

public interface TextTokenizer {
	public List<String> tokenize(String text);
}