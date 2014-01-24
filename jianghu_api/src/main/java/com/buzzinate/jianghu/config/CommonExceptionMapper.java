package com.buzzinate.jianghu.config;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

@Provider
public class CommonExceptionMapper implements ExceptionMapper<Exception> {
	private static Logger log = Logger.getLogger(CommonExceptionMapper.class);
	
	@Override
	public Response toResponse(Exception e) {
		log.error("Unknown error", e);
		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
	}
}
