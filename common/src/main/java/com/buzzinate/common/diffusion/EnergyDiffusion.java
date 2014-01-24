package com.buzzinate.common.diffusion;

import java.util.Set;

import org.apache.mahout.math.function.LongDoubleProcedure;
import org.apache.mahout.math.function.ObjectDoubleProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;

import com.buzzinate.common.util.Constants;

public class EnergyDiffusion implements Diffusion {
	private double beta;
	
	public EnergyDiffusion() {
		this(Constants.BETA);
	}
	
	public EnergyDiffusion(double beta) {
		this.beta = beta;
	}

	@Override
	public <Row> OpenObjectDoubleHashMap<Row> col2row(final Bipartite<Row> g, OpenLongDoubleHashMap colScores) {
		final OpenObjectDoubleHashMap<Row> rowScores = new OpenObjectDoubleHashMap<Row>();
		colScores.forEachPair(new LongDoubleProcedure() {
			@Override
			public boolean apply(long col, double score) {
				Set<Row> rows = g.getColvector().get(col);
				if (rows.size() > 0) {
					int colsize = g.getColsize(col);
					double avgE = score / Math.pow(colsize, beta);
					for (Row row : rows) rowScores.adjustOrPutValue(row, avgE, avgE);
				}
				return true;
			}
		});
		return rowScores;
	}

	@Override
	public <Row> OpenLongDoubleHashMap row2col(final Bipartite<Row> g, OpenObjectDoubleHashMap<Row> rowScores) {
		final OpenLongDoubleHashMap colScores = new OpenLongDoubleHashMap();
		rowScores.forEachPair(new ObjectDoubleProcedure<Row>() {
			@Override
			public boolean apply(Row row, double score) {
				Set<Long> cols = g.getRowvector().get(row);
				if (cols.size() > 0) {
					int rowsize = g.getRowsize(row);
					double avgE = score / Math.pow(rowsize, beta);
					for (long col : cols) colScores.adjustOrPutValue(col, avgE, avgE);
				}
				return true;
			}
		});
		return colScores;
	}
}