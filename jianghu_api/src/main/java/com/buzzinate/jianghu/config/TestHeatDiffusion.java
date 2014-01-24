package com.buzzinate.jianghu.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.math.function.LongDoubleProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenLongIntHashMap;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.PriorityQueue.Entry;
import com.buzzinate.common.util.SortUtils;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.mapping.Mapper;
import com.google.common.collect.HashMultimap;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestHeatDiffusion {

	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new JianghuModule());
		Datastore datastore = injector.getInstance(Datastore.class);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		PreferenceDao prefDao = new PreferenceDao(datastore);

		long[] userIds = new long[] { 12071L };
		for (long userId : userIds) {
			Bipartite g = new Bipartite();
			
			List<Long> items = prefDao.findLatestItems(userId, 200);
			if (items.isEmpty()) continue;
			g.add(userId, items);
			List<Article> as = articleDao.get(items, "prefSize");
			for (Article a: as) {
				if (a != null) g.setColSize(a.getId(), a.getPrefSize());
			}
			List<Preference> prefs = prefDao.createQuery().filter("pageId in", items).order("-" + Mapper.ID_KEY).limit(1000).asList();
			for (Preference pref : prefs) g.add(pref.getUserId(), pref.getPageId());
			
			OpenLongDoubleHashMap colScores = new OpenLongDoubleHashMap();
			for (long item : items) colScores.put(item, 1);
			final HashSet<Long> itemset = new HashSet<Long>(items);
			
			Diffusion[] ds = new Diffusion[] { g.forEnergy(), g.forHeat(), g.forPathCnt()};
			for (Diffusion d: ds) {
				OpenLongDoubleHashMap rowScores = d.col2row(colScores);
				System.out.println(rowScores.size() + " ==> 100");
				final PriorityQueue<Double, Long> upq = PriorityQueue.make(100, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
				rowScores.forEachPair(new LongDoubleProcedure() {
					@Override
					public boolean apply(long user, double score) {
						upq.add(score, user);
						return true;
					}
				});
				rowScores = new OpenLongDoubleHashMap();
				for (Entry<Double, Long> t: upq.entries()) rowScores.put(t.getValue(), t.getKey());
				
				List<Long> extusers = rowScores.keys().toList();
				List<Preference> extprefs = prefDao.createQuery().filter("userId in", extusers).order("-" + Mapper.ID_KEY).limit(1000).asList();
				List<Long> extItems = new ArrayList<Long>();
				for (Preference pref: extprefs) {
					g.add(pref.getUserId(), pref.getPageId());
					if (!itemset.contains(pref.getPageId())) extItems.add(pref.getPageId());
				}
				as = articleDao.get(extItems, "prefSize");
				for (Article a: as) {
					if (a != null) g.setColSize(a.getId(), a.getPrefSize());
				}
				
				OpenLongDoubleHashMap scores = d.row2col(rowScores);
				
				final PriorityQueue<Double, Long> pq = PriorityQueue.make(200, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
				scores.forEachPair(new LongDoubleProcedure() {
					@Override
					public boolean apply(long item, double score) {
						if (!itemset.contains(item)) pq.add(score, item);
						return true;
					}
				});
				List<Entry<Double, Long>> top = pq.entries();
				List<Long> ids = new ArrayList<Long>();
				for (Entry<Double, Long> t: top) ids.add(t.getValue());
				as = articleDao.get(ids, "title");
				HashMap<Long, String> titles = new HashMap<Long, String>();
				for (Article a: as) { 
					if (a != null) titles.put(a.getId(), a.getTitle()); 
				}
				System.out.println(userId + " " + d);
				for (Entry<Double, Long> t: top) {
					System.out.println(t.getValue() + "/" + titles.get(t.getValue()) + " ==> " + t.getKey());
				}
				System.out.println();
			}
		}

//		int[][] rv = new int[][] { { 0, 3 }, { 0, 1, 2, 3 }, { 0, 2 }, { 2, 4 } };
//		Bipartite g = new Bipartite();
//		for (int r = 0; r < rv.length; r++) {
//			for (int c = 0; c < rv[r].length; c++) g.add(r, rv[r][c]);
//		}
//		Diffusion ed = g.forHeat();
//		OpenLongDoubleHashMap colScores = new OpenLongDoubleHashMap();
//		colScores.put(0, 1);
//		colScores.put(3, 1);
//		OpenLongDoubleHashMap rowScores = ed.col2row(colScores);
//		colScores = ed.row2col(rowScores);
//		System.out.println("row score: " + colScores);
	}
	
}

interface Diffusion {
	OpenLongDoubleHashMap col2row(OpenLongDoubleHashMap colScores);
	OpenLongDoubleHashMap row2col(OpenLongDoubleHashMap rowScores);
}

class Bipartite {
	private HashMultimap<Long, Long> rowvector = HashMultimap.create();
	private HashMultimap<Long, Long> colvector = HashMultimap.create();
	private OpenLongIntHashMap colsizes = new OpenLongIntHashMap();

	public Bipartite() { }

	public void add(long row, List<Long> cols) {
		for (long col : cols) {
			rowvector.put(row, col);
			colvector.put(col, row);
		}
	}
	
	public void setColSize(long col, int colsize) {
		colsizes.put(col, colsize);
	}

	public void add(long row, long col) {
		rowvector.put(row, col);
		colvector.put(col, row);
	}
	
	// Energy diffusion
	public class EnergyDiffusion implements Diffusion {

		@Override
		public OpenLongDoubleHashMap col2row(OpenLongDoubleHashMap colScores) {
			final OpenLongDoubleHashMap rowScores = new OpenLongDoubleHashMap();
			colScores.forEachPair(new LongDoubleProcedure() {
				@Override
				public boolean apply(long col, double score) {
					Set<Long> rows = colvector.get(col);
					if (rows.size() > 0) {
						int colsize = colsizes.get(col);
						if (colsize < rows.size()) colsize = rows.size();
						double avgE = score / colsize;
						for (long row : rows) rowScores.adjustOrPutValue(row, avgE, avgE);
					}
					return true;
				}
			});
			return rowScores;
		}

		@Override
		public OpenLongDoubleHashMap row2col(OpenLongDoubleHashMap rowScores) {
			final OpenLongDoubleHashMap colScores = new OpenLongDoubleHashMap();
			rowScores.forEachPair(new LongDoubleProcedure() {
				@Override
				public boolean apply(long row, double score) {
					Set<Long> cols = rowvector.get(row);
					if (cols.size() > 0) {
						double avgE = score / cols.size();
						for (long col : cols) colScores.adjustOrPutValue(col, avgE, avgE);
					}
					return true;
				}
			});
			return colScores;
		}
		
	}

	// Heat diffusion
	public class HeatDiffusion implements Diffusion {

		@Override
		public OpenLongDoubleHashMap col2row(OpenLongDoubleHashMap colScores) {
			OpenLongDoubleHashMap rowScores = new OpenLongDoubleHashMap();
			for (Map.Entry<Long, Collection<Long>> e: rowvector.asMap().entrySet()) {
				int size = e.getValue().size();
				if (size > 1) {
					double sum = 0;
					for (long col: e.getValue()) sum += colScores.get(col);
					rowScores.put(e.getKey(), sum / size);
				}
			}
			return rowScores;
		}

		@Override
		public OpenLongDoubleHashMap row2col(OpenLongDoubleHashMap rowScores) {
			OpenLongDoubleHashMap colScores = new OpenLongDoubleHashMap();
			for (Map.Entry<Long, Collection<Long>> e: colvector.asMap().entrySet()) {
				int size = e.getValue().size();
				if (size > 1) {
					double sum = 0;
					for (long row: e.getValue()) sum += rowScores.get(row);
					int colsize = colsizes.get(e.getKey());
					if (colsize < size) colsize = size;
					colScores.put(e.getKey(), sum / colsize);
				}
			}
			return colScores;
		}
		
	}

	public class PathCountDiffusion implements Diffusion {

		@Override
		public OpenLongDoubleHashMap col2row(OpenLongDoubleHashMap colCnts) {
			final OpenLongDoubleHashMap rowCnts = new OpenLongDoubleHashMap();
			colCnts.forEachPair(new LongDoubleProcedure() {
				@Override
				public boolean apply(long col, double cnt) {
					Set<Long> rows = colvector.get(col);
					for (long row : rows) rowCnts.adjustOrPutValue(row, cnt, cnt);
					return true;
				}
			});
			return rowCnts;
		}

		@Override
		public OpenLongDoubleHashMap row2col(OpenLongDoubleHashMap rowCnts) {
			final OpenLongDoubleHashMap colCnts = new OpenLongDoubleHashMap();
			rowCnts.forEachPair(new LongDoubleProcedure() {
				@Override
				public boolean apply(long row, double cnt) {
					Set<Long> cols = rowvector.get(row);
					for (long col : cols) colCnts.adjustOrPutValue(col, cnt, cnt);
					return true;
				}
			});
			return colCnts;
		}
		
	}
	
	public Diffusion forEnergy() {
		return new EnergyDiffusion();
	}
	
	public Diffusion forHeat() {
		return new HeatDiffusion();
	}
	
	public Diffusion forPathCnt() {
		return new PathCountDiffusion();
	}
}