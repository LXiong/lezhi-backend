package dao;

import java.util.ArrayList;
import java.util.List;

import model.AdminUser;

import org.apache.commons.lang.StringUtils;

import play.modules.morphia.MorphiaPlugin;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.mongodb.WriteResult;

import exceptions.UpdateAdminUserException;

public class AdminUserDao {
	private static Datastore getDS() {
		return MorphiaPlugin.ds();
	}
	
	public static void update(AdminUser user) {
		Datastore ds = getDS();
		if (user.getId() == null) {
			throw new UpdateAdminUserException("require userId");
		}
		AdminUser sourceUser = ds.createQuery(AdminUser.class).filter("_id", user.getId()).get();
		if (sourceUser == null) {
			throw new UpdateAdminUserException("no matched user, userId is [" + user.getId() + "]");
		}
		boolean needUpdate = false;
		if (!StringUtils.equals(user.name, sourceUser.name)) {
			needUpdate = true;
		}
		if (!StringUtils.equals(user.password, sourceUser.password)) {
			needUpdate = true;
		}
		if (!user.roleIds.equals(sourceUser.roleIds)) {
			needUpdate = true;
		}
		if (needUpdate) {
			user.save();
		}
		
	}

	public static void deleteById(Long userId) {
		Datastore ds = getDS();
		ds.delete(ds.createQuery(AdminUser.class).filter("_id", userId));
	}

	public static List<AdminUser> find(Long id, String name, Integer roleId, int begin, int max) {
  		Datastore ds = MorphiaPlugin.ds();
		if (max <= 0) {
			max = 50;
		}
		if (begin <= 1) {
			begin = 1;
		}
		Query<AdminUser> query = ds.createQuery(AdminUser.class);
		query.offset((begin - 1) * max);
		query.limit(max);
		if (id != null && id > 0) {
			query.field("_id").equal(id);
		}
		if (!StringUtils.isEmpty(name)) {
			if (StringUtils.contains(name, "*")) {
				query.field("name").contains(StringUtils.remove(name, "*"));
			} else {
				query.field("name").equal(name);
			}
		}
		if (roleId != null && roleId > 0) {
			List<Integer> rid = new ArrayList<Integer>(1);
			rid.add(roleId);
			query.field("roleIds").hasAnyOf(rid);
		}
		
		return query.asList();
	}
}
