package com.buzzinate.nlp.matrix.scoring;

import java.util.List;

import com.buzzinate.nlp.util.MatrixUtils;
import com.buzzinate.nlp.util.PriorityQueue;
import com.buzzinate.nlp.util.SortUtils;

import edu.ucla.sspace.matrix.Matrix;
import edu.ucla.sspace.matrix.SparseMatrix;
import edu.ucla.sspace.matrix.TransposedMatrix;
import edu.ucla.sspace.matrix.factorization.SingularValueDecompositionLibJ;

public class HITSnSVDScorer implements MatrixScorer {
	private HITSScorer hits = new HITSScorer();

	@Override
	public ScoreResult score(SparseMatrix m) {
		ScoreResult hitsResult = hits.score(m);
		double[] colScore = hitsResult.getColScore();
		SingularValueDecompositionLibJ svd = new SingularValueDecompositionLibJ();		
		svd.factorize(m, Math.min(20, Math.min(m.rows(), m.columns()) * 7 / 11));
		
		Matrix u = svd.dataClasses();
		Matrix v = new TransposedMatrix(svd.classFeatures());
		
		if (hasNaN(u) || hasNaN(v)) return hitsResult;
		
		double[] svs = svd.singularValues();
		
		PriorityQueue<Double, Integer> cos2dim = PriorityQueue.make(Math.min(5, svs.length), SortUtils.reverse(Double.class), SortUtils.comp(Integer.class));
		for (int j = 0; j < v.columns(); j++) {
			double cos = MatrixUtils.cosine(colScore, v.getColumn(j));
			cos2dim.add(cos, j);
		}
		
		List<Integer> dims = cos2dim.values();
		
		double[] hub = new double[u.rows()];
		for (int k = 0; k < u.rows(); k++) {
			double sum = 0;
			for (int i: dims) sum += u.get(k, i) * u.get(k, i) * svs[i] * svs[i];
			hub[k] = Math.sqrt(sum);
		}
		MatrixUtils.normalize(hub);
		
		double[] authority = new double[v.rows()];
		for (int k = 0; k < v.rows(); k++) {
			double sum = 0;
			for (int i: dims) sum += v.get(k, i) * v.get(k, i) * svs[i] * svs[i];
			authority[k] = Math.sqrt(sum);
		}
		MatrixUtils.normalize(authority);
		
		return new ScoreResult(hub, authority);
	}

	private boolean hasNaN(Matrix m) {
		for (int i = 0; i < m.rows(); i++) {
			for (int j = 0; j < m.columns(); j++) {
				if (Double.isNaN(m.get(i, j))) return true;
			}
		}
		return false;
	}
}