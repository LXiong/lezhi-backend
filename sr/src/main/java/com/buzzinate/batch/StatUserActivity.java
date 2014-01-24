package com.buzzinate.batch;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.buzzinate.common.dao.BaseDaoDefault;
import com.buzzinate.common.dao.UserDailyStatDao;
import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.UserDailyStat;
import com.buzzinate.common.util.Constants;
import com.buzzinate.main.MyModule;
import com.buzzinate.model.Like;
import com.buzzinate.model.Read;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class StatUserActivity {
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		UserDao userDao = injector.getInstance(UserDao.class);
		BaseDaoDefault<Read, ObjectId> readDao = new BaseDaoDefault<Read, ObjectId>(Read.class, ds);
		BaseDaoDefault<Like, ObjectId> likeDao = new BaseDaoDefault<Like, ObjectId>(Like.class, ds);
		UserDailyStatDao udsDao = new UserDailyStatDao(ds);
		
		long today = round2date(System.currentTimeMillis());
		long yesterday = today - Constants.ONE_DAY;
		List<Long> leziUserIds = userDao.findLeziUserIds();
		for (long leziUserId: leziUserIds) {
			Query<Like> lq = likeDao.createQuery().filter("userId", leziUserId).filter("createAt >=", yesterday).filter("createAt <", today);
			int nLike = (int) likeDao.count(lq);
			Query<Read> rq = readDao.createQuery().filter("userId", leziUserId).filter("createAt >=", yesterday).filter("createAt <", today);
			int nRead = (int) readDao.count(rq);
			UserDailyStat uds = new UserDailyStat();
			uds.setDate(yesterday);
			uds.setUserId(leziUserId);
			uds.setnLike(nLike);
			uds.setnRead(nRead);
			udsDao.save(uds);
		}
	}
	
	@SuppressWarnings("deprecation")
	private static long round2date(long t) {
		Date d = new Date(t);
		d.setHours(0);
		d.setMinutes(0);
		d.setSeconds(0);
		return d.getTime();
	}
}