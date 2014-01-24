package dao;

import java.util.List;

import model.Role;

import org.apache.commons.lang.StringUtils;

import play.modules.morphia.MorphiaPlugin;

import com.buzzinate.common.util.IdGenerator;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;

import exceptions.UpdateRoleException;

public class RoleDao {
	public static void create(Role role) {
		Datastore ds = MorphiaPlugin.ds();
		role.id = IdGenerator.generateLongId(ds, Role.class);
		ds.save(role);
	}
	public static void update(Role role) {
		if (role.getId() == null) {
			throw new UpdateRoleException("require roleId");
		}
		Datastore ds = MorphiaPlugin.ds();
		Query<Role> q = ds.createQuery(Role.class).filter("id", role.getId());
		Role sourceRole = q.get();
		if (sourceRole == null) {
			throw new UpdateRoleException("no matched role, roleId is [" + role.getId() + "]");
		}
		boolean needUpdate = false;
		if (!StringUtils.equals(role.name, sourceRole.name)) {
			needUpdate = true;
		}
		if (!needUpdate
				&& !StringUtils.equals(role.description, sourceRole.description)) {
			needUpdate = true;
		}
		if (!needUpdate 
				&& !sourceRole.inheritedRoleIds.containsAll(role.inheritedRoleIds) ) {
			needUpdate = true;
		}
		if (needUpdate) {
			ds.save(role);
		}
	}

	public static void deleteById(Long roleId) {
		Datastore ds = MorphiaPlugin.ds();
		Query query = ds.createQuery(Role.class).filter("id", roleId);
		ds.delete(query);
	}
	
	public static List<Role> getAll() {
		Datastore ds = MorphiaPlugin.ds();
		return ds.createQuery(Role.class).asList();
	}
}
