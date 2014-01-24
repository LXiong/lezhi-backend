package com.buzzinate.jianghu.sr;

import com.buzzinate.common.dao.ItemMinhashDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.ItemMinhash;
import com.buzzinate.common.model.Minhash;
import com.buzzinate.common.model.MinhashQueue;
import com.buzzinate.common.model.User;
import com.buzzinate.common.util.Constants;
import com.buzzinate.common.util.MinhashUtil;

public class ItemMinhashUtil {
	public static void updateItemMinhash(ItemMinhashDao imhDao, UserDao userDao, long userid, long pageid) {
		long now = System.currentTimeMillis();
		ItemMinhash imh = imhDao.get(pageid);
		
		User user = userDao.get(userid);
		int prefsize = user.getPrefSize();
		int hash = MinhashUtil.hash(userid);
		
		if (imh == null) {
			imh = new ItemMinhash();
			imh.setId(userid);
			imh.setCreateAt(now);
		}
		
		MinhashQueue mq = new MinhashQueue(Constants.ITEM_MINHASH_NUM);
		for (Minhash mh: imh.getMinhashes()) mq.add(mh);
		mq.add(new Minhash(hash, prefsize));
		imh.setMinhashes(mq.values());
		imhDao.save(imh);
	}
}