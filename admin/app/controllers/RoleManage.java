package controllers;

import static utils.Constants.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import model.AdminUser;
import model.Role;
import model.User;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dao.RoleDao;

import play.data.validation.Required;
import play.modules.morphia.utils.IdGenerator;
import play.mvc.Controller;
import play.mvc.With;
import utils.SecurityUtils;

@Check(ROLE_TEST)
@With(Secure.class)
public class RoleManage extends Controller {
	private static Log log = LogFactory.getLog(RoleManage.class);
	
	private static void initRoles(Role role) {
		String roleIds = params.get("role.inheritedRoleIds");
		if (!StringUtils.isEmpty(roleIds)) {
			String[] roleIdsArr = roleIds.split(",");
			role.inheritedRoleIds = new HashSet<Long>(roleIdsArr.length);
			for (String id : roleIdsArr) {
				role.inheritedRoleIds.add(Long.parseLong(id));
			}
		}
	}
	
	public static void index() {
		render();
	}
	
	public static void create(@Required Role role) {
		Map<String, Object> result = getResult();
		try {
			initRoles(role);
			if (validate(role)) {
				RoleDao.create(role);
				result.put(JSON_RESULT, true);
				result.put("roleId", role.getId());

			} else {
				result.put(JSON_RESULT, false);
				result.put(JSON_CAUSE, "invalid role");
			}
		} catch (Exception e) {
			log.error(e);
			result.put(JSON_RESULT, false);
			result.put(JSON_CAUSE, e.getMessage());
		} finally {
			renderJSON(result);
		}
	}

	public static void update(@Required Role role) {
		Map<String, Object> result = getResult();
		try {
			initRoles(role);
			if (validate(role)) {
				RoleDao.update(role);
				result.put(JSON_RESULT, true);
				result.put("roleId", role.id);
			} else {
				result.put(JSON_RESULT, false);
				result.put(JSON_CAUSE, "invalid role");
			}
		} catch (Exception e) {
			log.error(e);
			result.put(JSON_RESULT, false);
			result.put(JSON_CAUSE, e.getMessage());
		} finally {
			renderJSON(result);
		}
	}

	public static void delete(@Required Long roleId) {
		Map<String, Object> result = getResult();
		try {
			if (roleId != null) {
				RoleDao.deleteById(roleId);
				result.put(JSON_RESULT, true);
				result.put("roleId", roleId);
			} else {
				result.put(JSON_RESULT, false);
				result.put(JSON_CAUSE, "require roleId");
			}
		} catch (Exception e) {
			log.error(e);
			result.put(JSON_RESULT, false);
			result.put(JSON_CAUSE, e.getMessage());
		} finally {
			renderJSON(result);
		}
	}
	
	public static void showAll() {
		try {
			List<Role> roles = RoleDao.getAll();
			renderJSON(roles);
		} catch (Exception e) {
			renderJSON("");
		}
	}

	private static Map<String, Object> getResult() {
		Map<String, Object> result = new HashMap<String, Object>(2);
		result.put(JSON_RESULT, false);
		return result;
	}
	
	public static void refresh() {
		Map<String, Object> result = getResult();
		try {
			SecurityUtils.refresh();
			result.put(JSON_RESULT, true);
		} catch (Exception e) {
			log.error(e);
		} finally {
			renderJSON(result);
		}
		
	}

	private static boolean validate(Role role) {
		if (StringUtils.isEmpty(role.name)) {
			return false;
		}
		return true;
	}
}
