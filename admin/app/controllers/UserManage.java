package controllers;

import static utils.Constants.JSON_CAUSE;
import static utils.Constants.JSON_RESULT;
import static utils.Constants.ROLE_ADMIN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.User;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.modules.morphia.utils.IdGenerator;
import play.mvc.Controller;
import play.mvc.With;

@Check(ROLE_ADMIN)
@With(Secure.class)
public class UserManage extends Controller {
	public static void index() {
		render();
	}
	
	private static Map<String, Object> getResult() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(JSON_RESULT, false);
		return result;
	}
	
	public static void create(@Required User user) {
		Map<String, Object> result = getResult();
		try {
			if (validate(user)) {
				long id = IdGenerator.generateLongId(User.class);
				user.setId(id);
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

	public static void update(@Required User user) {
		Map<String, Object> result = getResult();
		try {
			if (validate(user)) {
				User.update(user);
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
		Map<String, Object> result = getResult();
		try {
			if (userId != null) {
				User.deleteById(userId.longValue());
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

	public static void findUser(Long id, String name, String screenName,
			Long uid, String accessToken, int begin, int max) {
		List<User> users = null;
		try {
			users = User.find(id, name, screenName, uid, accessToken, begin, max);
		} catch (Exception e) {
			//do nothing
		} finally {
			renderJSON(users);
		}
	}

	private static boolean validate(User user) {
		if (StringUtils.isEmpty(user.name)
				|| StringUtils.isEmpty(user.screenName)) {
			return false;
		}
		return true;
	}
}
