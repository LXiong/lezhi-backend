package com.buzzinate.jianghu.security;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import com.google.inject.Inject;

@Provider
@ServerInterceptor
public class OAuthFilter implements PreProcessInterceptor, AcceptedByMethod {
	
	private final UserService userService;
	
	@Inject
	public OAuthFilter(UserService userService) {
		this.userService = userService;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean accept(Class declaring, Method method) {
		if (declaring == null || method == null) return false;
		return !method.isAnnotationPresent(PermitAll.class);
	}

	public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
		String accessToken = getHeader(request, "accessToken");
		// TODO:remove it after load test
		if (accessToken == null) accessToken = request.getUri().getQueryParameters().getFirst("accessToken");
		if (accessToken == null) throw new UnauthorizedException();
		
		long userId = userService.verifyUser(accessToken);
		request.getHttpHeaders().getRequestHeaders().put("userId", Arrays.asList(String.valueOf(userId)));
		return null;
	}
	
	private String getHeader(HttpRequest request, String name) {
		List<String> values = request.getHttpHeaders().getRequestHeader(name);
		if (values != null && values.size() > 0) return values.get(0);
		return null;
	}
}
