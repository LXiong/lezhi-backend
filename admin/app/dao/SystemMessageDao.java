package dao;

import com.buzzinate.common.model.Article;
import com.buzzinate.common.util.IdGenerator;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;

import play.modules.morphia.MorphiaPlugin;
import model.Mention;
import model.SystemMessage;

public class SystemMessageDao {
	public static void save(SystemMessage message) {
		Datastore ds = MorphiaPlugin.ds();
		long id = IdGenerator.generateLongId(ds, SystemMessage.class);
		try {
			//create message
			message.setId(id);
			ds.save(message);
			Mention mention = new Mention();
			//create mention
			mention.setId(IdGenerator.generateLongId(ds, Mention.class));
			mention.setUserId(1);
			mention.setCreateAt(System.currentTimeMillis());
			mention.setSourceId(id);
			mention.setType(3);
			ds.save(mention);
		} catch (Exception e) {
			try {
				Query<SystemMessage> query = ds
						.createQuery(SystemMessage.class);
				query.field("_id").equal(id);
				ds.delete(query);
			} catch (Exception ex) {
				// do nothing
			}
			throw new RuntimeException(e);
		}
	}
}
