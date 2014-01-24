package com.buzzinate.jianghu.security;

import org.jboss.resteasy.spi.UnauthorizedException;

public interface UserService {
	long login(String accessToken, String secret) throws UnauthorizedException;
	long verifyUser(String accessToken) throws UnauthorizedException;
	long login2(String accessToken, long uid) throws UnauthorizedException;
}