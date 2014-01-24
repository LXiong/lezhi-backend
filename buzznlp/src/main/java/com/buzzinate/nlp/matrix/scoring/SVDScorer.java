package com.buzzinate.nlp.matrix.scoring;

import com.buzzinate.nlp.util.MatrixUtils;

import edu.ucla.sspace.matrix.Matrix;
import edu.ucla.sspace.matrix.SparseMatrix;
import edu.ucla.sspace.matrix.TransposedMatrix;
import edu.ucla.sspace.matrix.factorization.SingularValueDecompositionLibJ;

public class SVDScorer implements MatrixScorer {

	@Override
	public ScoreResult score(SparseMatrix m) {
		SingularValueDecompositionLibJ svd = new SingularValueDecompositionLibJ();
		svd.factorize(m, Math.min(m.rows(), m.columns()) * 7 / 11);
		
		Matrix u = svd.dataClasses();
		Matrix v = new TransposedMatrix(svd.classFeatures());
		double[] svs = svd.singularValues();
		double[] hub = new double[u.rows()];
		for (int k = 0; k < u.rows(); k++) {
			double sum = 0;
			for (int i = 0; i < svs.length; i++) sum += u.get(k, i) * u.get(k, i) * svs[i] * svs[i];
			hub[k] = Math.sqrt(sum);
		}
		MatrixUtils.normalize(hub);
		
		double[] authority = new double[v.rows()];
		for (int k = 0; k < v.rows(); k++) {
			double sum = 0;
			for (int i = 0; i < svs.length; i++) sum += v.get(k, i) * v.get(k, i) * svs[i] * svs[i];
			authority[k] = Math.sqrt(sum);
		}
		MatrixUtils.normalize(authority);
		
		return new ScoreResult(hub, authority);
	}
}
