package model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import play.modules.morphia.Model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

import dao.RoleDao;

@Entity(value = "role", noClassnameStored = true)
public class Role extends Model{
	private static Log log = LogFactory.getLog(Role.class);
	@Id
	public Long id;
	@Indexed(unique=true)
	public String name;
	public String description;
	public Set<Long> inheritedRoleIds = null;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	static {
		try {	
			List<Role> roles = RoleDao.getAll();
			if (roles == null || roles.isEmpty()) {
				Role role = new Role();
				role.name = "admin";
				role.description = "admin user";
				role.inheritedRoleIds = new HashSet<Long>(0);
				RoleDao.create(role);
			}
		} catch (Exception e) {
			log.error("Error when initialize Role");
		}
	}

}
