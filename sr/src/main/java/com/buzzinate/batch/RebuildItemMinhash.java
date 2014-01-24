package com.buzzinate.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.buzzinate.common.dao.ArticleDao;
import com.buzzinate.common.dao.ItemMinhashDao;
import com.buzzinate.common.dao.PreferenceDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.Article;
import com.buzzinate.common.model.ItemMinhash;
import com.buzzinate.common.model.Minhash;
import com.buzzinate.common.model.MinhashQueue;
import com.buzzinate.common.model.Preference;
import com.buzzinate.common.model.User;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.MinhashUtil;
import com.buzzinate.main.MyModule;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RebuildItemMinhash {
	
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		UserDao userDao = injector.getInstance(UserDao.class);
		PreferenceDao prefDao = new PreferenceDao(ds);
		ArticleDao articleDao = injector.getInstance(ArticleDao.class);
		
		ItemMinhashDao imDao = new ItemMinhashDao(ds);
		
		long sixmonthago = System.currentTimeMillis() - Constants.ONE_DAY * 30 * 2; 
		ObjectId sinceId = new ObjectId(new Date(sixmonthago), 0, 0);
		ObjectId maxId = prefDao.findMaxId();
		int batch = 2000;
		while (sinceId.compareTo(maxId) < 0) {
			System.out.println("processing " + sinceId);
			List<Preference> prefs = prefDao.createQuery().filter("_id >", sinceId).order("_id").limit(batch).asList();
			
			HashMap<Long, Integer> user2prefsize = new HashMap<Long, Integer>();
			HashMap<Long, Long> item2create = new HashMap<Long, Long>();
			for (Preference pref: prefs) {
				user2prefsize.put(pref.getUserId(), 1);
				item2create.put(pref.getPageId(), pref.getCreateAt());
			}
			
			for (User u: userDao.get(new ArrayList<Long>(user2prefsize.keySet()), "prefSize")) {
				if (u != null) user2prefsize.put(u.getId(), u.getPrefSize());
			}
			
			for (Article item: articleDao.get(new ArrayList<Long>(item2create.keySet()), "createAt")) {
				if (item != null) item2create.put(item.getId(), item.getCreateAt());
			}
			
			HashMap<Long, MinhashQueue> item2mq = new HashMap<Long, MinhashQueue>();
			for (long itemId: item2create.keySet()) item2mq.put(itemId, new MinhashQueue(Constants.ITEM_MINHASH_NUM));
			for (Preference pref: prefs) {
				long userid = pref.getUserId();
				Minhash mh = new Minhash(MinhashUtil.hash(userid), user2prefsize.get(userid));
				item2mq.get(pref.getPageId()).add(mh);
			}
			
			for (Map.Entry<Long, MinhashQueue> e: item2mq.entrySet()) {
				long itemId = e.getKey();
				MinhashQueue mq = e.getValue();
				ItemMinhash imh = imDao.get(itemId);
				if (imh == null) {
					imh = new ItemMinhash();
					imh.setId(itemId);
					imh.setCreateAt(item2create.get(itemId));
				}
				System.out.print(itemId + ": " + imh.getMinhashes().size() + " => ");
				for (Minhash mh: imh.getMinhashes()) mq.add(mh);
				imh.setMinhashes(mq.values());
				System.out.println(imh.getMinhashes().size());
				imDao.save(imh);
			}
			
			int last = prefs.size() - 1;
			if (last >= 0) sinceId = prefs.get(last).getId();
		}
	}
}