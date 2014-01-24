package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mahout.clustering.minhash.HashFactory;
import org.apache.mahout.clustering.minhash.HashFactory.HashType;
import org.apache.mahout.clustering.minhash.HashFunction;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.mongo.ArticleDaoImpl;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.MinHash;
import com.buzzinate.common.model.Status;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class SimHash {
	
	
	/**
	 * 检查之前的内容重复的文章，如果有，把本文章标为重复
	 * 
	 * @param page
	 * @return 
	 */
	public static List<Article> findDuplicateContent(ArticleDao articleDao, Article page) {
		List<MinHash> minHashes = page.getMinHashes();
		if (!minHashes.isEmpty()) {
			List<Article> pages = articleDao.findByMinHash(minHashes, "id", "title", "url", "minHashes");
			List<Long> simIds = new ArrayList<Long>();
			for (Article p: pages) {
				if (SimHash.getJaccard(minHashes, p.getMinHashes()) >= 0.6) {
					simIds.add(p.getId());
				}
			}
			return articleDao.get(simIds);
		}
		return new ArrayList<Article>();
	}
	
	public static float getJaccard(List<MinHash> mh1, List<MinHash> mh2) {
		int common = 0;
		int total = 0;
		for (int i = 0; i < mh1.size() && i < mh2.size(); i++) {
			MinHash m1 = mh1.get(i);
			MinHash m2 = mh2.get(i);
			for (int k = 0; k < m1.getMinHashes().size() && k < m2.getMinHashes().size(); k++) {
				if (m1.getMinHashes().get(k).equals(m2.getMinHashes().get(k))) common++;
				total++;
			}
		}
		return common * 1f / total;
	}
}
