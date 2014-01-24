package controllers;

import static utils.Constants.JSON_CAUSE;
import static utils.Constants.JSON_RESULT;
import static utils.Constants.ROLE_ADMIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.AdminUser;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dao.AdminUserDao;

import play.data.validation.Required;
import play.modules.morphia.utils.IdGenerator;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Scope.Params;

@Check(ROLE_ADMIN)
@With(Secure.class)
public class AdminUserManage extends Controller {
	private static Log log = LogFactory.getLog(AdminUserManage.class);
	
	public static void index() {
		render();
	}
	
	public static void create(@Required AdminUser user) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			initRoles(user);
			if (validate(user)) {
				user.setId(IdGenerator.generateLongId(AdminUser.class));
				user.save();
				result.put(JSON_RESULT, true);
				result.put("userId", user.id);

			} else {
				result.put(JSON_RESULT, false);
				result.put(JSON_CAUSE, "invalid user");
			}
		} catch (Exception e) {
			result.put(JSON_RESULT, false);
			result.put(JSON_CAUSE, e.getMessage());
		} finally {
			renderJSON(result);
		}
	}

	private static void initRoles(AdminUser user) {
		String roleIds = params.get("user.roleIds");
		if (!StringUtils.isEmpty(roleIds)) {
			String[] roleIdsArr = roleIds.split(",");
			user.roleIds = new ArrayList<Long>(roleIdsArr.length);
			for (String id : roleIdsArr) {
				user.roleIds.add(Long.parseLong(id));
			}
		}
	}

	public static void update(@Required AdminUser user) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			user.setId(Long.parseLong(params.get("user.id")));
			initRoles(user);
			if (validate(user)) {
				AdminUserDao.update(user);
				result.put(JSON_RESULT, true);
				result.put("userId", user.id);
			} else {
				result.put(JSON_RESULT, false);
				result.put(JSON_CAUSE, "invalid user");
			}
		} catch (Exception e) {
			result.put(JSON_RESULT, false);
			result.put(JSON_CAUSE, e.getMessage());
		} finally {
			renderJSON(result);
		}
	}

	public static void delete(@Required Long userId) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			if (userId != null) {
				AdminUserDao.deleteById(userId);
				result.put(JSON_RESULT, true);
				result.put("userId", userId);
			} else {
				result.put(JSON_RESULT, false);
				result.put(JSON_CAUSE, "require userId");
			}
		} catch (Exception e) {
			result.put(JSON_RESULT, false);
			result.put(JSON_CAUSE, e.getMessage());
		} finally {
			renderJSON(result);
		}
	}

	public static void findUser(Long id, String name, int roleId,
			int begin, int max) {
		List<AdminUser> users = null;
		try {
			users = AdminUserDao.find(id, name, roleId, begin, max);
		} catch (Exception e) {
			log.error(e);
		} finally {
			renderJSON(users);
		}
	}

	private static boolean validate(AdminUser user) {
		if (StringUtils.isEmpty(user.name)) {
			return false;
		}
		return true;
	}
}
