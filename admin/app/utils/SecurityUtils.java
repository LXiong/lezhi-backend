package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import model.AdminUser;
import model.Role;

import org.apache.commons.collections.list.SynchronizedList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ViolatedConstraintNameExtracter;

import dao.RoleDao;

public class SecurityUtils {
	private static Log log = LogFactory.getLog(SecurityUtils.class);
	private final static int MAX_RECURSION_LEVEL = 5;
	private static volatile Map<Long, Role> roleIds;
	private static volatile List<Role> roles;
	
	private SecurityUtils() {
	}
	

	
	public static boolean hasRole(AdminUser user, Long roleId) {
		for (Long id : user.roleIds) {
			if (id.equals(roleId)) return true;
			Role role = roleIds.get(id);
			if (role == null) continue;
			for (Long rid : role.inheritedRoleIds) {
				if (rid.equals(roleId)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean hasRole(AdminUser user, String roleName) {
		return hasRole(user, getRoleIdByName(roleName));
	}
	
	/**
	 * 
	 * @param roleName
	 * @return Long.MIN_VALUE if no matched role exists.
	 */
	public static Long getRoleIdByName(String roleName) {
		for (Role role : roles) {
			if (role.name.equals(roleName)) {
				return role.getId();
			}
		}
		return Long.MIN_VALUE;
	}

	public static void refresh() {
		init();
	}
	
	
	//********************Init methods********************//
	
	private static void init() {
		List<Role> newRoles = RoleDao.getAll();
		roles = SynchronizedList.decorate(newRoles);
		if (roles == null) {
			roles = SynchronizedList.decorate(new ArrayList<Role>(0));
		}
		
		
		Map<Long, Role> roleIdMap = new ConcurrentHashMap<Long, Role>(roles.size());
		for (Role role : roles) {
			roleIdMap.put(role.getId(), role);
		}
		for (int i = 0; i < roles.size(); i++) {
			try {
				initRoleHierarchy(roleIdMap, roles.get(i).getId(), roles.get(i), 1);
			} catch (Exception e) {
				log.error(e);
			}
		}
		roleIds = roleIdMap;
	}

	private static void initRoleHierarchy(Map<Long, Role> roleIdMap, Long rid, Role role, int recursionLevel) {
		if (role.inheritedRoleIds == null || role.inheritedRoleIds.isEmpty()
				|| recursionLevel > MAX_RECURSION_LEVEL) {
			Role r = roleIdMap.get(rid);
			if (r != null && r.inheritedRoleIds != null) {
				r.inheritedRoleIds.add(rid);
			}
			return;
		}
		Long[] ids = role.inheritedRoleIds.toArray(new Long[role.inheritedRoleIds.size()]);
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] == null) {
				continue;
			}
			Role r = roleIdMap.get(ids[i]);
			if (r != null) {
				initRoleHierarchy(roleIdMap, rid, r, recursionLevel + 1);
				roleIdMap.get(rid).inheritedRoleIds.add(ids[i]);
			}
		}
	}
	
	static {
		init();
	}

	public static List<Long> getRoleIds() {
		return new ArrayList(roleIds.keySet());
	}

}
