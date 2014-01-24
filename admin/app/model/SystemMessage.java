package model;

import java.util.Date;

import play.modules.morphia.Model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity(value = "admin", noClassnameStored = true)
public class SystemMessage extends Model{
	@Id
	private long id;
	public String message;
	public Date createAt;
	
	@Override
	public Object getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
}
