package com.buzzinate.common.diffusion;

import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;

public interface Diffusion {
	<Row> OpenObjectDoubleHashMap<Row> col2row(Bipartite<Row> g, OpenLongDoubleHashMap colScores);
	<Row> OpenLongDoubleHashMap row2col(Bipartite<Row> g, OpenObjectDoubleHashMap<Row> rowScores);
}