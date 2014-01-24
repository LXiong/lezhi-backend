package org.knowceans.sandbox;

import java.util.Arrays;
import java.util.List;

import org.knowceans.lda.InferResult;
import org.knowceans.lda.LdaEstimate;
import org.knowceans.lda.LdaModel;
import org.knowceans.lda.corpus.Corpus;
import org.knowceans.lda.corpus.CorpusBuilder;
import org.knowceans.lda.corpus.Document;
import org.knowceans.lda.corpus.IKTextTokenizer;
import org.knowceans.lda.corpus.Vocabulary;

public class Test {
	public static void main(String[] args) {
		List<String> texts = Arrays.asList(
			"将文档看成是一组主题的混合，词有分配到每个主题的概率。",
			"LDA，就是将原来向量空间的词的维度转变为Topic的维度，这一点是十分有意义的。",
			"Lda的源代码，java c matlab python 等  ：",
			"22:20:53 www.cnblogs.cn"
		);
		CorpusBuilder cb = new CorpusBuilder();
		for (String text: texts) cb.add(text);
		Corpus c = cb.build();
		System.out.println(c.getNumDocs());
		
		String modelRoot = "D:/tmp/ldamodel";
		LdaModel model = LdaEstimate.runEm("seeded", c);
		model.save(modelRoot);
		cb.getVoc().save(modelRoot);
		
		LdaModel m = new LdaModel(modelRoot);
		Vocabulary voc = new Vocabulary(modelRoot);
		cb = new CorpusBuilder(new IKTextTokenizer(), voc);
		for (String text: texts) {
			Document doc = cb.createDoc(text);
			InferResult r = LdaEstimate.infer(m, doc);
			System.out.println(r.getVarGamma());
		}
	}
}