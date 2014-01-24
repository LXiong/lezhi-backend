package org.knowceans.lda.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.knowceans.util.Counter;

public class CorpusBuilder {
	private TextTokenizer tokenizer;
	private List<List<String>> rawDocs = new ArrayList<List<String>>();
	private List<Document> docs = new ArrayList<Document>();
	private Vocabulary voc;
	
	public CorpusBuilder() {
		this(new IKTextTokenizer(), new Vocabulary());
	}
	
	public CorpusBuilder(TextTokenizer tokenizer) {
		this(tokenizer, new Vocabulary());
	}
	
	public CorpusBuilder(TextTokenizer tokenizer, Vocabulary voc) {
		this.tokenizer = tokenizer;
		this.voc = voc;
	}
	
	public Vocabulary getVoc() {
		return voc;
	}
		
	public void add(String text) {
		List<String> rawDoc = tokenizer.tokenize(text);
		rawDocs.add(rawDoc);
	}

	public Corpus build() {
		Counter<String> wordCnt = new Counter<String>();
		for (List<String> rawDoc: rawDocs) {
			HashSet<String> wordSet = new HashSet<String>(rawDoc);
			for (String w: wordSet) wordCnt.add(w);
		}
		
		HashMap<String, Integer> stopWords = new HashMap<String, Integer>();
		for (Map.Entry<String, Integer> e: wordCnt.toMap().entrySet()) {
			if (e.getValue() <= 2) stopWords.put(e.getKey(), e.getValue());
		}
		
		System.out.println("stop words: " + stopWords);
		
		for (List<String> rawDoc: rawDocs) {
			DocumentBuilder docBuilder = new DocumentBuilder();
			List<String> words = new ArrayList<String>();
			for (String w: rawDoc) {
				if (!stopWords.containsKey(w)) {
					words.add(w);
				}
			}
			docBuilder.addAll(voc.encode(words));
			docs.add(docBuilder.build());
		}
		
		Document[] ds = docs.toArray(new Document[0]);
		return new Corpus(ds, voc.getSize());
	}

	public Document createDoc(String text) {
		DocumentBuilder docBuilder = new DocumentBuilder();
		List<String> words = tokenizer.tokenize(text);
		docBuilder.addAll(voc.encodeOnly(words));
		return docBuilder.build();
	}
}