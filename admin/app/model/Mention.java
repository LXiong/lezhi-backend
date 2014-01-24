package model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import play.modules.morphia.Model;

@Entity(value="mention", noClassnameStored=true)
public class Mention extends Model{
	public static final int COMMENT_MENTION = 0;
	
	@Id private long id;
	
	@Indexed
	private long userId;
	
	private long sourceId;
	private int type;
	
	private long createAt;

        @Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getSourceId() {
		return sourceId;
	}

	public void setSourceId(long sourceId) {
		this.sourceId = sourceId;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}
}
