package com.buzzinate.imexport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.buzzinate.common.dao.UserDao;
import com.buzzinate.common.model.User;
import com.buzzinate.dao.FetchInfoDao;
import com.buzzinate.main.MyModule;
import com.buzzinate.model.FetchInfo;
import com.google.code.morphia.Datastore;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * 导入名人堂初始数据
 * 
 * @author Brad Luo
 *
 */
public class ImportFetchInfos {
	
	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new MyModule());
		Datastore ds = injector.getInstance(Datastore.class);
		UserDao userDao = injector.getInstance(UserDao.class);
		FetchInfoDao fiDao = new FetchInfoDao(ds);
		
		ArrayList<FetchInfo> fis = new ArrayList<FetchInfo>();
		InputStream is = ClassLoader.getSystemResourceAsStream("sina_data.txt");
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		String line = r.readLine();
		while (line != null) {
			String[] fields = line.split("\\s+");
			if (fields.length < 4  || fields[3].startsWith("http")) {
				System.out.println("Error :" + line);
				line = r.readLine();
				continue;
			}
			
			try {
				FetchInfo fi = new FetchInfo();
				fi.name = fields[1];
				fi.uid = fields[3];
				fis.add(fi);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			line = r.readLine();
		}
		fiDao.batchUpdate(fis);
		
		List<User> leziUsers = userDao.createQuery().filter("isLeziUser", true).asList();
		fis = new ArrayList<FetchInfo>();
		for (User u: leziUsers) {
			FetchInfo fi = new FetchInfo();
			fi.uid = String.valueOf(u.getUid());
			fi.name = u.getScreenName();
			fi.score = 1;
			fis.add(fi);
		}
		fiDao.batchUpdate(fis);
	}
}
