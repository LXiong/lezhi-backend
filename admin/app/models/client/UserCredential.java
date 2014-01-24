package models.client;

import oauth.signpost.OAuthConsumer;

public class UserCredential {

	private static final long serialVersionUID = 7041709385891554863L;
	public static enum AuthType { NA, BASIC, OAUTH, SESSION };

	private AuthType authType;
	private String username;
	private String password;
	private String oauthAccessToken;
	private String oauthAccessTokenSecret;
	private String info;
	private Integer platformUserID;
	private String sessionKey;
	
	public UserCredential() {
		this.authType = AuthType.NA;
	}
	
	public UserCredential(String a, String b, AuthType authType) {
		this.authType = authType;
		if (authType == AuthType.BASIC) {
			this.username = a;
			this.password = b;
		} else if (authType == AuthType.OAUTH) {
			this.oauthAccessToken = a;
			this.oauthAccessTokenSecret = b;
		}
	}
	
	public UserCredential(OAuthConsumer consumer) {
		this.oauthAccessToken = consumer.getToken();
		this.oauthAccessTokenSecret = consumer.getTokenSecret();
		this.authType = AuthType.OAUTH;
	}
	
	public UserCredential(String sessionKey) {
		this.sessionKey = sessionKey;
		this.authType = AuthType.SESSION;
	}
	
	public UserCredential(AuthType authType) { 
		this.authType = authType;
	}

	
	public AuthType getAuthType() {
		return authType;
	}

	public void setAuthType(AuthType authType) {
		this.authType = authType;
	}

	public String getOauthAccessToken() {
		return oauthAccessToken;
	}
	public void setOauthAccessToken(String oauthAccessToken) {
		this.oauthAccessToken = oauthAccessToken;
	}
	public String getOauthAccessTokenSecret() {
		return oauthAccessTokenSecret;
	}
	public void setOauthAccessTokenSecret(String oauthAccessTokenSecret) {
		this.oauthAccessTokenSecret = oauthAccessTokenSecret;
	}
	
	public void setPlatformUserID(Integer platformUserID) {
		this.platformUserID = platformUserID;
	}

	public Integer getPlatformUserID() {
		return platformUserID;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	@Override
	public String toString() {
		return "UserCredential [authType=" + authType
				+ ", oauthAccessToken=" + oauthAccessToken
				+ ", oauthAccessTokenSecret=" + oauthAccessTokenSecret
				+ ", password=" + password + ", platformUserID="
				+ platformUserID + ", sessionKey=" + sessionKey + ", username="
				+ username + "]";
	}

}
