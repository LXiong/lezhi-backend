package utils;

import play.Logger;
import play.modules.morphia.MorphiaPlugin;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public final class MongoDB {

	private static DB db;
	
	static {
		try {
			try {
				db = MorphiaPlugin.ds().getDB();
			} catch(Exception e) {
				Mongo mongo = new Mongo();
				db = mongo.getDB("recomm");
			}
//			if (!db.authenticate("recomm", "111111".toCharArray())) {
//				Logger.error("Failed to authenticate MongoDB.");
//			}
		} catch (Exception e) {
			Logger.error(e, "Failed to connect to MongoDB.");
		} 
	}
	
	/**
	 * Get a collection from MongoDB
	 * @param collectionName
	 * @return
	 */
	public static DBCollection getCollection(String collectionName) {
		if (db == null) {
			return null;
		}
		return db.getCollection(collectionName);
	}
	

}