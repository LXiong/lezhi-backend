package com.buzzinate.nlp.matrix.scoring;

import java.util.Arrays;

import com.buzzinate.nlp.util.MatrixUtils;

import edu.ucla.sspace.matrix.SparseMatrix;
import edu.ucla.sspace.vector.SparseDoubleVector;

public class HITSScorer implements MatrixScorer {
	private static final double d = 0.85;
	
	private int nIter;
	
	public HITSScorer() {
		this(5);
	}
	
	public HITSScorer(int nIter) {
		this.nIter = nIter;
	}

	@Override
	public ScoreResult score(SparseMatrix m) {
		double[] hub = new double[m.rows()];
        Arrays.fill(hub, 1);
        double[] auth = new double[m.columns()];
        Arrays.fill(auth, 1);
		computeAuthorityHub(m, hub, auth);
		return new ScoreResult(hub, auth);
	}

	public void computeAuthorityHub(SparseMatrix matrix, double[] hub, double[] auth) {
    	for (int k = 0; k < nIter; k++) {
        	//I operation
        	for (int j = 0; j < matrix.columns(); j++) {
        		SparseDoubleVector col = matrix.getColumnVector(j);
        		double sum = 0;
        		int[] is = col.getNonZeroIndices();
        		for (int i: is) {
        			sum += matrix.get(i, j) * hub[i];
        		}
        		if (is.length > 0) sum = sum / is.length;
        		auth[j] = (1-d) * auth[j] + sum * d;
        	}
        	MatrixUtils.normalize(auth);

            //O operation
            for (int i = 0; i < matrix.rows(); i++) {
            	SparseDoubleVector row = matrix.getRowVector(i);
            	double sum = 0;
            	int[] js = row.getNonZeroIndices();
            	for (int j: js) {
            		double v = matrix.get(i, j);
            		sum += v * auth[j];
            	}
            	
            	if (js.length > 0) sum = sum / js.length;
            	hub[i] = (1-d) * hub[i] + sum * d;
            }
            MatrixUtils.normalize(hub);
        }
    }
}