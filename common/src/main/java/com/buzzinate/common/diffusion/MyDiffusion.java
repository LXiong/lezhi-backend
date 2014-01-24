package com.buzzinate.common.diffusion;

import java.util.Set;

import org.apache.mahout.math.function.LongDoubleProcedure;
import org.apache.mahout.math.function.ObjectDoubleProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;

public class MyDiffusion implements Diffusion {
	private double alpha = 0.5d;
	private double beta = 0.5d;
	
	public MyDiffusion(double alpha, double beta) {
		this.alpha = alpha;
		this.beta = beta;
	}
	
	@Override
	public <Row> OpenObjectDoubleHashMap<Row> col2row(final Bipartite<Row> g, OpenLongDoubleHashMap colScores) {
		final OpenObjectDoubleHashMap<Row> sumRowScores = new OpenObjectDoubleHashMap<Row>();
		colScores.forEachPair(new LongDoubleProcedure() {
			@Override
			public boolean apply(long col, double score) {
				Set<Row> rows = g.getColvector().get(col);
				if (rows.size() > 0) {
					int colsize = g.getColsize(col);
					double avgE = score / Math.pow(colsize, alpha);
					for (Row row : rows) sumRowScores.adjustOrPutValue(row, avgE, avgE);
				}
				return true;
			}
		});
		
		final OpenObjectDoubleHashMap<Row> rowScores = new OpenObjectDoubleHashMap<Row>();
		sumRowScores.forEachPair(new ObjectDoubleProcedure<Row>(){
			@Override
			public boolean apply(Row row, double sum) {
				int rowsize = g.getRowsize(row);
				rowScores.put(row, sum / Math.pow(rowsize, beta));
				return true;
			}
		});
		
		return rowScores;
	}

	@Override
	public <Row> OpenLongDoubleHashMap row2col(final Bipartite<Row> g, OpenObjectDoubleHashMap<Row> rowScores) {
		final OpenLongDoubleHashMap sumColScores = new OpenLongDoubleHashMap();
		rowScores.forEachPair(new ObjectDoubleProcedure<Row>() {
			@Override
			public boolean apply(Row row, double score) {
				Set<Long> cols = g.getRowvector().get(row);
				if (cols.size() > 0) {
					int rowsize = g.getRowsize(row);
					double avgE = score / Math.pow(rowsize, alpha);
					for (long col : cols) sumColScores.adjustOrPutValue(col, avgE, avgE);
				}
				return true;
			}
		});
		
		final OpenLongDoubleHashMap colScores = new OpenLongDoubleHashMap();
		sumColScores.forEachPair(new LongDoubleProcedure(){
			@Override
			public boolean apply(long col, double sum) {
				int colsize = g.getColsize(col);
				colScores.put(col, sum / Math.pow(colsize, beta));
				return true;
			}
		});
		
		return colScores;
	}
}