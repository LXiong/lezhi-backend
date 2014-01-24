package com.buzzinate.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserMinhashDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.Minhash;
import com.buzzinate.common.model.MinhashQueue;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.model.UserMinhash;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.MinhashUtil;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RebuildUserMinhash {
	
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		PreferenceDao prefDao = new PreferenceDao(ds);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		
		UserMinhashDao umhDao = new UserMinhashDao(ds);
		
		long sixmonthago = System.currentTimeMillis() - Constants.ONE_DAY * 30 * 6; 
		ObjectId sinceId = new ObjectId(new Date(sixmonthago), 0, 0);
		ObjectId maxId = prefDao.findMaxId();
		int batch = 10000;
		while (sinceId.compareTo(maxId) < 0) {
			System.out.println("processing " + sinceId);
			List<Preference> prefs = prefDao.createQuery().filter("_id >", sinceId).order("_id").limit(batch).asList();
			
			HashMap<Long, Integer> item2prefsize = new HashMap<Long, Integer>();
			HashMap<Long, Long> user2lastMod = new HashMap<Long, Long>();
			for (Preference pref: prefs) {
				item2prefsize.put(pref.getPageId(), 1);
				user2lastMod.put(pref.getUserId(), pref.getCreateAt());
			}
			
			for (Article a: articleDao.get(new ArrayList<Long>(item2prefsize.keySet()), "prefSize")) {
				if (a != null) item2prefsize.put(a.getId(), a.getPrefSize());
			}
			
			HashMap<Long, MinhashQueue> user2mq = new HashMap<Long, MinhashQueue>();
			for (long userId: user2lastMod.keySet()) user2mq.put(userId, new MinhashQueue(Constants.ITEM_MINHASH_NUM));
			for (Preference pref: prefs) {
				long itemId = pref.getPageId();
				Minhash mh = new Minhash(MinhashUtil.hash(itemId), item2prefsize.get(itemId));
				user2mq.get(pref.getUserId()).add(mh);
			}
			
			for (Map.Entry<Long, MinhashQueue> e: user2mq.entrySet()) {
				long userId = e.getKey();
				MinhashQueue mq = e.getValue();
				UserMinhash umh = umhDao.get(userId);
				if (umh == null) {
					umh = new UserMinhash();
					umh.setId(userId);
				}
				System.out.print(userId + ": " + umh.getMinhashes().size() + " => ");
				for (Minhash mh: umh.getMinhashes()) mq.add(mh);
				umh.setMinhashes(mq.values());
				umh.setLastModified(user2lastMod.get(userId));
				System.out.println(umh.getMinhashes().size());
				umhDao.save(umh);
			}
			
			int last = prefs.size() - 1;
			if (last >= 0) sinceId = prefs.get(last).getId();
		}
	}
}