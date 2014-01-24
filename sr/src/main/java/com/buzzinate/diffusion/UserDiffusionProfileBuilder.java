package com.buzzinate.diffusion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.mahout.math.function.ObjectDoubleProcedure;
import org.apache.mahout.math.map.OpenLongDoubleHashMap;
import org.apache.mahout.math.map.OpenLongIntHashMap;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;
import org.apache.mahout.math.map.OpenObjectIntHashMap;
import org.arabidopsis.ahocorasick.WordFreqTree;
import org.bson.types.ObjectId;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ArticleProfileDao;
import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.dict.Dictionary;
import com.buzzinate.common.diffusion.Bipartite;
import com.buzzinate.common.diffusion.Diffusion;
import com.buzzinate.common.diffusion.Diffusions;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.ArticleProfile;
import com.buzzinate.common.model.Keyword;
import com.buzzinate.common.model.KeywordFeature;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.model.User;
import com.buzzinate.common.model.UserFeature;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.PriorityQueue;
import com.buzzinate.common.util.PriorityQueue.Entry;
import com.buzzinate.common.util.SortUtils;
import com.buzzinate.crawl.core.tag.SuffixArray;
import com.buzzinate.crawl.core.tag.WordFreq;
import com.buzzinate.crawl.core.tpl.TitleExtractor;
import com.buzzinate.model.Read;
import com.google.code.morphia.mapping.Mapper;

import edu.ucla.sspace.matrix.GrowingSparseMatrix;
import edu.ucla.sspace.matrix.Matrix;
import edu.ucla.sspace.matrix.TransposedMatrix;
import edu.ucla.sspace.matrix.factorization.NonNegativeMatrixFactorizationMultiplicative;

public class UserDiffusionProfileBuilder {
	private static Logger log = Logger.getLogger(UserDiffusionProfileBuilder.class);
	
	private static TitleExtractor te = new TitleExtractor();
	
	private static final int howManyItem = 200;
	private static final int CLUSTER_SIZE = 10;
	public static double FD = 3d;
	
	public static DiffusionProfile build(ArticleDao articleDao, PreferenceDao prefDao, UserDao userDao, Dictionary dict, long userId) {
		System.out.println("processing user: " + userId);
		
		List<Long> items = prefDao.getItemsFromUser(userId);
		if (items.size() <= 2) {
			List<Long> extPageIds = new ArrayList<Long>();
			extPageIds.addAll(items);
			
			BaseDaoDefault<Read, ObjectId> readDao = new BaseDaoDefault<Read, ObjectId>(Read.class, prefDao.getDatastore());
			List<Read> reads = readDao.createQuery().filter("userId", userId).asList();
			for (Read read: reads) extPageIds.add(read.getArticleId());
			items = extPageIds;
		}
		if (items.isEmpty()) return null;
		
		ArticleProfileDao apDao = new ArticleProfileDao(prefDao.getDatastore());
//		long since = System.currentTimeMillis() - Constants.ONE_DAY * 10;
		
		List<Article> as = articleDao.get(items, "prefSize", "title");
		Bipartite<Long> g = new Bipartite<Long>();
		for (Article a: as) {
			if (a != null) g.setColSize(a.getId(), a.getPrefSize());
		}
		
		WordFreqTree wft = buildWft(as);
		Map<Long, List<String>> item2words = new HashMap<Long, List<String>>();
		for (Article a: as) {
			if (a != null && a.getTitle() != null) {
				List<String> words = wft.search(te.extract(a.getTitle()));
				if (words.size() > 0) item2words.put(a.getId(), words);
			}
		}
		
		PriorityQueue<Double, Long> topq = PriorityQueue.make(howManyItem, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		for (Article a: as) {
			if (a != null && a.getTitle() != null) {
				List<String> words = item2words.get(a.getId());
				int kwsize = 0;
				if (words != null) kwsize = words.size();
			    int prefsize = a.getPrefSize();
			    if (prefsize < 3) prefsize = Integer.MAX_VALUE;
			    topq.add(kwsize + 1d / prefsize, a.getId());
			}
		}
		
		List<Preference> prefs = new ArrayList<Preference>();
		for (long item: topq.size() < 10? items : topq.values()) {
			prefs.addAll(prefDao.createQuery().filter("pageId", item).order("-" + Mapper.ID_KEY).limit(20).asList());
		}
		List<Long> users = new ArrayList<Long>();
		
		Map<Long, Long> user2lastTime = new HashMap<Long, Long>();
		for (Preference pref : prefs) {
			Long lt = user2lastTime.get(pref.getUserId());
			if (lt == null || lt < pref.getCreateAt()) user2lastTime.put(pref.getUserId(), pref.getCreateAt());
			
			if (pref.getUserId() == userId) continue;
			g.add(pref.getUserId(), pref.getPageId());
			users.add(pref.getUserId());
		}
		
		Map<Long, Integer> userPrefs = userDao.mapField(users, "prefSize");
		for (Map.Entry<Long, Integer> e: userPrefs.entrySet()) {
			g.setRowSize(e.getKey(), e.getValue());
		}
		
		Map<Long, List<Keyword>> item2keywords = new HashMap<Long, List<Keyword>>();
		for (Map.Entry<Long, ArticleProfile> e: apDao.map2(topq.values(), "keywords").entrySet()) {
			List<Keyword> kws = e.getValue().getKeywords();
			List<String> words = item2words.get(e.getKey());
			if (words != null) {
				for (String word: words) kws.add(new Keyword(word, 2));
			}
			item2keywords.put(e.getKey(), kws);
		}
		
		Bipartite<String> wg = new Bipartite<String>();
		for (Map.Entry<Long, List<Keyword>> e: item2keywords.entrySet()) {
			List<Keyword> kws = e.getValue();
			for (Keyword kw: kws) {
				String word = kw.getWord();
				wg.add(word, e.getKey());
				wg.setRowSize(word, dict.docFreq(word));
			}
		}
		
		Map<Long, String> item2title = articleDao.mapField(topq.values(), "title");
		List<Cluster> clusters = cluster(dict, prefs, item2title, item2keywords);
//		g.print(user2name, item2title);
		
		List<UserFeature> topusers = new ArrayList<UserFeature>();
		List<KeywordFeature> topwords = new ArrayList<KeywordFeature>();
		for (int c = 0; c < clusters.size(); c++) {
			Cluster cluster = clusters.get(c);
			List<Long> ids = cluster.getIds();
			if (ids.isEmpty()) continue;
			
			OpenLongDoubleHashMap initColScores = new OpenLongDoubleHashMap();
			for (long id: ids) initColScores.put(id, CLUSTER_SIZE * 1d / ids.size());
			
			for (Diffusions.Algo algo: Diffusions.algos) {
				System.out.println(userId + " ==> " + algo.name + "-" + c);
				topusers.addAll(buildUserFeatures(g, initColScores, algo.diffusion, c, algo.name, user2lastTime, userDao));
				topwords.addAll(buildKeywordFeatures(wg, initColScores, algo.diffusion, c, algo.name));
			}
		}
		return new DiffusionProfile(topusers, topwords);
	}

	private static WordFreqTree buildWft(List<Article> as) {
		List<String> titles = new ArrayList<String>();
		for (Article a: as) {
			if (a != null && a.getTitle() != null) titles.add(te.extract(a.getTitle()));
		}
		
		WordFreqTree wft = new WordFreqTree();
		for (WordFreq kw: SuffixArray.extractKeywords(titles, 3, 0.75, 0.5)) {
			System.out.println(kw);
			wft.add(kw.getWord());
		}
		wft.build();
		
		return wft;
	}

	private static List<KeywordFeature> buildKeywordFeatures(Bipartite<String> wg, OpenLongDoubleHashMap colScores, Diffusion d, int c, String algo) {
		final PriorityQueue<Double, String> pq = PriorityQueue.make(CLUSTER_SIZE * 2, SortUtils.reverse(Double.class), SortUtils.reverse(String.class));
		
		OpenObjectDoubleHashMap<String> rowScores = d.col2row(wg, colScores);
		
		rowScores.forEachPair(new ObjectDoubleProcedure<String>(){
			@Override
			public boolean apply(String word, double score) {
				pq.add(score, word);
				return true;
			}
		});
		
		List<KeywordFeature> kfs = new ArrayList<KeywordFeature>();
		for (PriorityQueue.Entry<Double, String> e: pq.entries()) {
			System.out.println(e.getValue() + " ==> " + e.getKey());
			kfs.add(new KeywordFeature(e.getValue(), c, algo, e.getKey()));
		}
		return kfs;
	}

	private static List<UserFeature> buildUserFeatures(Bipartite<Long> g, OpenLongDoubleHashMap colScores, Diffusion d, int c, String algo, final Map<Long, Long> user2lastTime, UserDao userDao) {
		final PriorityQueue<Double, Long> pq = PriorityQueue.make(CLUSTER_SIZE * 2, SortUtils.reverse(Double.class), SortUtils.reverse(Long.class));
		
		OpenObjectDoubleHashMap<Long> rowScores = d.col2row(g, colScores);
		
		final long now = System.currentTimeMillis();
		rowScores.forEachPair(new ObjectDoubleProcedure<Long>(){
			@Override
			public boolean apply(Long userId, double score) {
				long lt = user2lastTime.get(userId);
				double factor = FD / (FD + Math.log(1 + (now - lt) / Constants.ONE_DAY));
				pq.add(score * factor, userId);
				return true;
			}
		});
		
		List<UserFeature> ufs = new ArrayList<UserFeature>();
		for (PriorityQueue.Entry<Double, Long> e: pq.entries()) {
			long userId = e.getValue();
			User u = userDao.get(userId);
			if (u != null) System.out.println(userId + "(" + u.getScreenName() + ")/" + g.getRowsize(userId) + " ==> " + e.getKey());
			ufs.add(new UserFeature(userId, c, algo, e.getKey(), g.getRowsize(userId)));
		}
		return ufs;
	}

	private static List<Cluster> cluster(Dictionary dict, List<Preference> prefs, Map<Long, String> item2title, Map<Long, List<Keyword>> item2keywords) {
		GrowingSparseMatrix feaDocMatrix = new GrowingSparseMatrix();
		OpenLongIntHashMap doc2idx = new OpenLongIntHashMap(item2title.size());
		OpenObjectIntHashMap<String> f2idx = new OpenObjectIntHashMap<String>();
		long now = System.currentTimeMillis();
		for (Preference pref: prefs) {
			long userId = pref.getUserId();
			long pageId = pref.getPageId();
			int docidx = doc2idx.adjustOrPutValue(pageId, doc2idx.size(), 0);
			int fidx = f2idx.adjustOrPutValue("u-" + userId, f2idx.size(), 0);
			double factor = FD / (FD + Math.min(30, Math.log(1 + (now - pref.getCreateAt()) / Constants.ONE_DAY)));
			feaDocMatrix.set(fidx, docidx, factor);
		}
		for (Map.Entry<Long, List<Keyword>> e: item2keywords.entrySet()) {
			long pageId = e.getKey();
			int docidx = doc2idx.adjustOrPutValue(pageId, doc2idx.size(), 0);
			for (Keyword kw: e.getValue()) {
				String word = kw.getWord();
				int fidx = f2idx.adjustOrPutValue(word, f2idx.size(), 0);
				feaDocMatrix.set(fidx, docidx, kw.getFreq() * dict.idf(kw.getWord()));
			}
		}
		
		Properties prop = new Properties();
		prop.setProperty(NonNegativeMatrixFactorizationMultiplicative.INNER_ITERATIONS, "4");
		prop.setProperty(NonNegativeMatrixFactorizationMultiplicative.OUTER_ITERATIONS, "100");
		NonNegativeMatrixFactorizationMultiplicative nmf = new NonNegativeMatrixFactorizationMultiplicative(prop);
		log.info("Term Doc matrix size: " + feaDocMatrix.rows() + " * " + feaDocMatrix.columns());
		nmf.factorize(feaDocMatrix, 8);
		
		Matrix h = new TransposedMatrix(nmf.classFeatures());
		
		for (int i = 0; i < h.rows(); i++) {
			double m = h.getRowVector(i).magnitude();
			for (int j = 0; j < h.columns(); j++) {
				h.set(i, j, h.get(i, j) / m);
			}
		}

		int[] dc = new int[h.rows()];
		for (int i = 0; i < h.rows(); i++) {
			int maxj = -1;
			double maxv = 0;
			for (int j = 0; j < h.columns(); j++) {
				if (maxv < h.get(i, j)) {
					maxj = j;
					maxv = h.get(i, j);
				}
			}
			dc[i] = maxj;
		}
		
		long[] items = new long[doc2idx.size()];
		for (Map.Entry<Long, String> e: item2title.entrySet()) items[doc2idx.get(e.getKey())] = e.getKey();
		
		List<Cluster> clusters = new ArrayList<Cluster>();
		boolean[] mark = new boolean[doc2idx.size()];
		Arrays.fill(mark, false);
		for (int j = 0; j < h.columns(); j++) {
			PriorityQueue<Double, Integer> pq = PriorityQueue.make(CLUSTER_SIZE, SortUtils.reverse(Double.class), SortUtils.comp(Integer.class));
			for (int i = 0; i < h.rows(); i++) {
				if (dc[i] == j) pq.add(h.get(i, j), i);
			}
			
			if (pq.size() >= CLUSTER_SIZE / 5) {
				System.out.println("*****************************");
				List<Long> ids = new ArrayList<Long>();
				List<Entry<Double, Integer>> es = pq.entries();
				for (Entry<Double, Integer> e: es) {
					mark[e.getValue()] = true;
					ids.add(items[e.getValue()]);
					System.out.println(e.getKey() + " ==> " + item2title.get(items[e.getValue()]));
				}
				clusters.add(new Cluster(ids));
			}
		}
		
		List<Long> ids = new ArrayList<Long>();
		for (int i = 0; i < mark.length; i++) {
			if (mark[i] == false) ids.add(items[i]);
		}
		
		if (ids.size() >= CLUSTER_SIZE / 5) clusters.add(new Cluster(ids));
		
		return clusters;
	}
}