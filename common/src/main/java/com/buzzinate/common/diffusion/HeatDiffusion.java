package com.buzzinate.common.diffusion;

import java.util.Collection;
import java.util.Map;

import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;

import com.buzzinate.common.util.Constants;

public class HeatDiffusion implements Diffusion {
	private double beta;
	
	public HeatDiffusion() {
		this(Constants.BETA);
	}
	
	public HeatDiffusion(double beta) {
		this.beta = beta;
	}

	@Override
	public <Row> OpenObjectDoubleHashMap<Row> col2row(Bipartite<Row> g, OpenLongDoubleHashMap colScores) {
		OpenObjectDoubleHashMap<Row> rowScores = new OpenObjectDoubleHashMap<Row>();
		for (Map.Entry<Row, Collection<Long>> e: g.getRowvector().asMap().entrySet()) {
			int size = e.getValue().size();
			if (size > 0) {
				double sum = 0;
				for (long col: e.getValue()) sum += colScores.get(col);
				int rowsize = g.getRowsize(e.getKey());
				double rs = sum / Math.pow(rowsize, beta);
				rowScores.put(e.getKey(), rs);
			}
		}
		return rowScores;
	}

	@Override
	public <Row> OpenLongDoubleHashMap row2col(Bipartite<Row> g, OpenObjectDoubleHashMap<Row> rowScores) {
		OpenLongDoubleHashMap colScores = new OpenLongDoubleHashMap();
		for (Map.Entry<Long, Collection<Row>> e: g.getColvector().asMap().entrySet()) {
			int size = e.getValue().size();
			if (size > 0) {
				double sum = 0;
				for (Row row: e.getValue()) sum += rowScores.get(row);
				int colsize = g.getColsize(e.getKey());
				double cs = sum / Math.pow(colsize, beta);
				colScores.put(e.getKey(), cs);
			}
		}
		return colScores;
	}
}