package com.buzzinate.jianghu.security;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.UnauthorizedException;

@Provider
public class SecurityExceptionMapper implements ExceptionMapper<UnauthorizedException> {

	@Override
	public Response toResponse(UnauthorizedException e) {
		return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
	}
}
