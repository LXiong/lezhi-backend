package model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.modules.morphia.Model;
import play.modules.morphia.Model.MorphiaQuery;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.query.Query;

import exceptions.UpdateUserException;

@Entity(value = "user", noClassnameStored = true)
public class User extends Model {
	@Id
	public long id;
	public String name;
	public String screenName;
	public String profileImageUrl;
	
	@Indexed
	public String accessToken;
	public String secret;
	
	@Indexed(unique=true, dropDups=true)
	public long uid;
	
        @Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	protected void setId_(Object id) {
		this.id = (Long)id;
	}

	public static void deleteById(long userId) {
		Datastore ds = ds();
		Query query = ds.createQuery(User.class).filter("id", userId);
		ds.delete(query);
	}
	
	public User getByName(String username) {
		MorphiaQuery query = filter("name", username);
		return query.first();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id != other.id)
			return false;
		return true;
	}

	

	@Override
	public String toString() {
		return "User [accessToken=" + accessToken + ", id=" + id + ", name="
				+ name + ", profileImageUrl=" + profileImageUrl
				+ ", screenName=" + screenName + ", secret=" + secret
				+ ", uid=" + uid + "]";
	}

	public static void update(User user) {
		if (user.getId() == null) {
			throw new UpdateUserException("require userId");
		}
		User sourceUser = (User)findById(user.getId());
		if (sourceUser == null) {
			throw new UpdateUserException("no matched user, userId is [" + user.getId() + "]");
		}
		boolean needUpdate = false;
		if (!StringUtils.equals(user.name, sourceUser.name)) {
			needUpdate = true;
		}
		if (!needUpdate
				&& !StringUtils.equals(user.profileImageUrl, sourceUser.profileImageUrl)) {
			needUpdate = true;
		}
		if (!needUpdate
				&& !StringUtils.equals(user.screenName, sourceUser.screenName)) {
			needUpdate = true;
		}
		if (!needUpdate && user.uid != sourceUser.uid) {
			needUpdate = true;
		}
		if (needUpdate) {
			user.save();
		}
	}

	public static List<User> find(Long id, String name, String screenName,
			Long uid, String accessToken, int begin, int max) {
		if (max <= 0) {
			max = 50;
		}
		if (begin <= 1) {
			begin = 1;
		}
		MorphiaQuery query = createQuery();
		query.offset((begin - 1) * max);
		query.limit(max);
		if (id != null && id > 0) {
			query.field("_id").equal(id);
		}
		if (!StringUtils.isEmpty(name)) {
			if (!StringUtils.contains(name, "*")) {
				query.field("name").contains(StringUtils.remove(name, "*"));
			} else {
				query.field("name").equal(name);
			}
		}
		if (!StringUtils.isEmpty(screenName)) {
			if (!StringUtils.contains(screenName, "*")) {
				query.field("screenName").contains(
						StringUtils.remove(screenName, "*"));
			} else {
				query.field("screenName").equal(screenName);
			}
		}
		if (uid != null && uid > 0) {
			query.field("uid").equal(uid);
		}
		if (!StringUtils.isEmpty(accessToken)) {
			query.field("accessToken").equal(accessToken);
		}
		return query.asList();
	}
	
	
}
