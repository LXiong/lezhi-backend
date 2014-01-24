package com.buzzinate.common.diffusion;

import java.util.Set;

import org.apache.mahout.math.function.LongDoubleProcedure;
import org.apache.mahout.math.function.ObjectDoubleProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;

public class PathCountDiffusion implements Diffusion {

	@Override
	public <Row> OpenObjectDoubleHashMap<Row> col2row(final Bipartite<Row> g, OpenLongDoubleHashMap colCnts) {
		final OpenObjectDoubleHashMap<Row> rowCnts = new OpenObjectDoubleHashMap<Row>();
		colCnts.forEachPair(new LongDoubleProcedure() {
			@Override
			public boolean apply(long col, double cnt) {
				Set<Row> rows = g.getColvector().get(col);
				for (Row row : rows) rowCnts.adjustOrPutValue(row, cnt, cnt);
				return true;
			}
		});
		return rowCnts;
	}

	@Override
	public <Row> OpenLongDoubleHashMap row2col(final Bipartite<Row> g, OpenObjectDoubleHashMap<Row> rowCnts) {
		final OpenLongDoubleHashMap colCnts = new OpenLongDoubleHashMap();
		rowCnts.forEachPair(new ObjectDoubleProcedure<Row>() {
			@Override
			public boolean apply(Row row, double cnt) {
				Set<Long> cols = g.getRowvector().get(row);
				for (long col : cols) colCnts.adjustOrPutValue(col, cnt, cnt);
				return true;
			}
		});
		return colCnts;
	}
}