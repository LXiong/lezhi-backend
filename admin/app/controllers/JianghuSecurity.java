package controllers;

import org.apache.commons.lang.StringUtils;

import utils.SecurityUtils;
import model.AdminUser;
import controllers.Secure.Security;

public class JianghuSecurity extends Security {
	
	static boolean authenticate(String username, String password) {
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			return false;
		}

		AdminUser user = AdminUser.findByUsername(username);
		if (user != null && StringUtils.equals(user.password, password)) {
			return true;
		}
		return false;
	}
	
	static boolean authentify(String username, String password) {
		return authenticate(username, password);
	}
	
	static boolean check(String profile) {
		AdminUser user = AdminUser.findByUsername(connected());
		if (user != null && SecurityUtils.hasRole(user, profile)) {
			return true;
		}
        return false;
    }
	
	static void onAuthenticated() {
		//TODO modify it
		ArticleManage.index();
    }
	
	

}
