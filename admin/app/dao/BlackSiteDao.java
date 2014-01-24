package dao;

import java.util.List;

import play.modules.morphia.MorphiaPlugin;

import com.buzzinate.common.model.BlackSite;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class BlackSiteDao {
	public static List<BlackSite> getTopUnverified(int offset, int length) {
		Datastore ds = MorphiaPlugin.ds();
		return ds.createQuery(BlackSite.class)
				.filter("status", BlackSite.Status.UnVerified)
				.order("-score")
				.offset(offset).limit(length).asList();
	}

	public static void verify(List<String> blackSites) {
		Datastore ds = MorphiaPlugin.ds();
		Query<BlackSite> q = ds.createQuery(BlackSite.class).filter("site in", blackSites);
		UpdateOperations<BlackSite> uo = ds.createUpdateOperations(BlackSite.class).set("status", BlackSite.Status.Verified);
		ds.update(q, uo);
	}
}
