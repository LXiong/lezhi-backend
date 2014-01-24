package com.buzzinate.jianghu.config;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.buzzinate.jianghu.util.ApiException;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
	
	@Override
	public Response toResponse(ApiException e) {
		return Response.status(Status.BAD_REQUEST).entity(e.toString()).build();
	}
}
