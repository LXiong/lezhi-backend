package com.buzzinate.common.util;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.utils.LongIdEntity.StoredId;
import com.mongodb.Mongo;
import com.mongodb.ReadPreference;

public class IdGenerator {
	public static long generateLongId(Datastore ds, Class<?> clazz){
        String collName = ds.getCollection(clazz).getName();
        Query<StoredId> q = ds.find(StoredId.class, "_id", collName).queryPrimaryOnly();
        UpdateOperations<StoredId> uOps = ds.createUpdateOperations(StoredId.class).inc("value").isolated();
        
        Mongo mongo = ds.getMongo();
        ReadPreference oldPref = mongo.getReadPreference();
        mongo.setReadPreference(ReadPreference.PRIMARY);
        StoredId newId = ds.findAndModify(q, uOps);
        if (newId == null) {
            newId = new StoredId(collName);
            ds.save(newId);
        }
        mongo.setReadPreference(oldPref);
        return newId.getValue();
    }
}
