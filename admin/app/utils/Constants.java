package utils;

import models.client.UserCredential;
import models.client.UserCredential.AuthType;

public class Constants {
	public static final String JSON_RESULT = "result";
	public static final String JSON_CAUSE = "cause";
	
	public static final String LINK_QUEUE = "link";
	public static final String PAGE_QUEUE = "page";
	public static final String CLASSIFY_QUEUE = "classify";
	public static final String SR_QUEUE = "sr";
	public static final String USER_QUEUE = "user";
	public static final String MODEL_QUEUE = "model";
	
	public static final String KEY_MODEL_PATH = "rec.modelPath";
	//******ROLES*********//
	public static final String ROLE_ADMIN = "admin";
	public static final String ROLE_EDITOR = "editor";
	public static final String ROLE_TEST = "test";
	
	private Constants(){}
}
