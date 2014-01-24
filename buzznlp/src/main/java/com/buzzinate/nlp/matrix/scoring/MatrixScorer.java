package com.buzzinate.nlp.matrix.scoring;


import edu.ucla.sspace.matrix.SparseMatrix;

public interface MatrixScorer {
	public ScoreResult score(SparseMatrix m);
}