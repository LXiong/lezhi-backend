package com.buzzinate.common.diffusion;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.math.function.ObjectIntProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenLongIntHashMap;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

import com.google.common.collect.HashMultimap;

public class Bipartite<Row> {
	private HashMultimap<Row, Long> rowvector = HashMultimap.create();
	private HashMultimap<Long, Row> colvector = HashMultimap.create();
	private OpenObjectIntHashMap<Row> rowsizes = new OpenObjectIntHashMap<Row>();
	private OpenLongIntHashMap colsizes = new OpenLongIntHashMap(100);
	
	public void add(Row row, List<Long> cols) {
		for (long col : cols) {
			rowvector.put(row, col);
			colvector.put(col, row);
		}
	}
	
	public void setRowSize(Row row, int rowsize) {
		rowsizes.put(row, rowsize);
	}
	
	public void setColSize(long col, int colsize) {
		colsizes.put(col, colsize);
	}

	public void add(Row row, long col) {
		rowvector.put(row, col);
		colvector.put(col, row);
	}
	
	public void clear() {
		rowvector.clear();
		colvector.clear();
		rowsizes = new OpenObjectIntHashMap<Row>(100);
		colsizes.clear();
	}
	
	public OpenLongIntHashMap countColPath(OpenObjectIntHashMap<Row> rowCnt) {
		final OpenLongIntHashMap colCnt = new OpenLongIntHashMap();
		rowCnt.forEachPair(new ObjectIntProcedure<Row>(){
			@Override
			public boolean apply(Row row, int cnt) {
				for (long col: rowvector.get(row)) colCnt.adjustOrPutValue(col, cnt, cnt);
				return true;
			}
			
		});
		return colCnt;
	}
	
	public void print(Map<Row, String> row2name, Map<Long, String> col2name) {
		System.out.println("==============================");
		System.out.println("graph:");
		for (Map.Entry<Row, Collection<Long>> e: rowvector.asMap().entrySet()) {
			Row r = e.getKey();
			System.out.print(r + "(" + row2name.get(r) + ")/" + rowsizes.get(r) + ": ");
			for (long c: e.getValue()) System.out.print(c + "(" + col2name.get(c) + ")/" + colsizes.get(c) + ",");
			System.out.println();
		}
		System.out.println("==============================");
	}

	public OpenObjectIntHashMap<Row> countRowPath(List<Long> initCols) {
		OpenObjectIntHashMap<Row> rowCnt = new OpenObjectIntHashMap<Row>();
		for (long col: initCols) {
			for (Row row: colvector.get(col)) rowCnt.adjustOrPutValue(row, 1, 1);
		}
		return rowCnt;
	}
	
	public Set<Long> getRow(Row r) {
		return rowvector.get(r);
	}
	
	public Set<Row> getCol(long c) {
		return colvector.get(c);
	}
	
	public static void main(String[] args) {
		int[][] rv = new int[][] { { 0, 3 }, { 0, 1, 2, 3 }, { 0, 2 }, { 2, 4 } };
		Bipartite<Integer> g = new Bipartite<Integer>();
		for (int r = 0; r < rv.length; r++) {
			for (int c = 0; c < rv[r].length; c++) g.add(r, rv[r][c]);
		}
		Diffusion ed = new MyDiffusion(0.3d, 0.7d);
		OpenLongDoubleHashMap colScores = new OpenLongDoubleHashMap();
		colScores.put(0, 1);
		colScores.put(3, 1);
		OpenObjectDoubleHashMap<Integer> rowScores = ed.col2row(g, colScores);
		colScores = ed.row2col(g, rowScores);
		for (int i = 0; i <= 4; i++) System.out.println(i + " => " + colScores.get(i));
		rowScores = ed.col2row(g, colScores);
		colScores = ed.row2col(g, rowScores);
		for (int i = 0; i <= 4; i++) System.out.println(i + " => " + colScores.get(i));
	}

	public HashMultimap<Row, Long> getRowvector() {
		return rowvector;
	}
	
	public int getRowsize(Row row) {
		return Math.max(rowsizes.get(row), rowvector.get(row).size());
	}

	public HashMultimap<Long, Row> getColvector() {
		return colvector;
	}

	public int getColsize(Long col) {
		return Math.max(colsizes.get(col), colvector.get(col).size());
	}
}