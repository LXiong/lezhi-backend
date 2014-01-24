package model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.query.Query;

import exceptions.UpdateAdminUserException;
import exceptions.UpdateUserException;

import play.modules.morphia.Model;
import play.modules.morphia.MorphiaPlugin;
import play.modules.morphia.utils.IdGenerator;
import utils.SecurityUtils;

@Entity(value = "admin", noClassnameStored = true)
public class AdminUser extends Model {
	private static Log log = LogFactory.getLog(AdminUser.class);
	public static final String DEFAULT_ADMIN_USERNAME = "jianghu_admin";
	@Id
	public Long id;
	public String name;

	@Indexed(unique=true)
	public String username;
	public String password;
	
	@Indexed
	public List<Long> roleIds;

	public static AdminUser findByUsername(String username) {
		MorphiaQuery query = filter("username", username);
		AdminUser user = query.first();
		return user;
	}
	
	@Override
	protected void setId_(Object id) {
		this.id = (Long)id;
	}
	
	@Override
	public Object getId() {
		return this.id;
	}
	
	static {
		try {
			AdminUser user = filter("username", DEFAULT_ADMIN_USERNAME).first();
			if (user == null) {
				user = new AdminUser();
				long id = IdGenerator.generateLongId(AdminUser.class);
				System.out.println(id);
				user.setId(id);
				user.name = "super admin";
				user.password = "ceibs32073208";
				user.username = DEFAULT_ADMIN_USERNAME;
				user.roleIds = SecurityUtils.getRoleIds();
				user.save();
			}
		} catch (Exception e) {
			log.error("Error when initialize AdminUser", e);
		}
	}

}
